package ru.sp.dystopia.arcocode.examiner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.dom.*;
import ru.sp.dystopia.arcocode.metrics.MetricsWriter;

/**
 * Класс разбора исходных файлов Java.
 * @author Maxim Yarov
 */
public class JavaExaminer {
    /**
     * Статическая основная и единственная функция анализа .java-файла.
     * 
     * Файл считывается в память, библиотекой JDT строится абстрактное
     * синтаксическое дерево AST, по нему совершается проход экземпляром
     * класса JavaVisitor.
     * 
     * @param file Объект, указывающий на обрабатываемый файл
     * @param writer Обработчик получыемых результатов для передачи в JavaVisitor
     * @return Успешность выполнения
     */
    public static boolean examine(File file, MetricsWriter writer) {
        // If your .java files are larger than MAX_INT, you are doing something
        // seriously wrong.
        char buf[] = new char[(int)(file.length())];

        try {
            FileReader fr = new FileReader(file);
            fr.read(buf);
        } catch (IOException ex) {
            Logger.getLogger(JavaExaminer.class.getName()).log(Level.INFO, null, ex);
            return false;
        }
        
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(buf);
        
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        
        cu.accept(new JavaVisitor(writer));
        writer.reset();
        
        return true;
    }
}

/**
 * Класс, который непосредственно обходит синтаксическое дерево AST.
 * @author Maxim Yarov
 */
class JavaVisitor extends ASTVisitor
{
    /**
     * Название пакета, в котором в данный момент находится visitor.
     */
    private String curPackage;
    /**
     * Название класса или интерфейса, в котором в данный момент находится
     * visitor.
     */
    private String curType;
    /**
     * Название метода, в котором в данный момент находится visitor.
     */
    private String curMethod;
    /**
     * Счетчик выражений текущего метода.
     */
    private int curStmtCount;
    /**
     * Счетчик ветвлений текущего метода.
     */
    private int curControlCount;
    /**
     * Маскирование определения новых методов.
     * Применяется, если в данный
     * момент visitor находится внутри определения анонимного класса;
     * размер и сложность этого анонимного класса логически относятся к тому
     * методу, где он определен.
     */
    private boolean maskMethods;
    
    /**
     * Объект, записывающий результаты анализа.
     */
    private MetricsWriter writer;

    /**
     * Конструктор, инициализирует this.writer.
     * 
     * @param writer Объект, записывающий результаты анализа
     */
    public JavaVisitor(MetricsWriter writer) {
        this.writer = writer;
    }
    
    // ---
    
    /**
     * Действия при входе в объявление пакета.
     * 
     * Устаналивается текущее имя пакета, в результаты заносится существование
     * такого пакета.
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(PackageDeclaration node) {
        curPackage = node.getName().toString();
        writer.addPackage(curPackage);
        return true;
    }
    
    // ---
    
    /**
     * Действия при входе в объявление импорта.
     * 
     * Правила получения имени пакета, классы которого импортируются, взяты
     * из документации к JDT.
     * 
     * В результаты записывается из какого пакета импортируется какой.
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ImportDeclaration node) {
        String importee = "";
        
        if        (!node.isOnDemand() && !node.isStatic()) {
            importee = ((QualifiedName)node.getName()).getQualifier().getFullyQualifiedName();
        } else if (!node.isOnDemand() &&  node.isStatic()) {
            importee = ((QualifiedName)((QualifiedName)node.getName()).getQualifier()).getQualifier().getFullyQualifiedName();
        } else if ( node.isOnDemand() && !node.isStatic()) {
            importee = node.getName().getFullyQualifiedName();
        } else if ( node.isOnDemand() &&  node.isStatic()) {
            importee = ((QualifiedName)node.getName()).getQualifier().getFullyQualifiedName();
        }
        
        writer.addConnection(curPackage, importee);
        return true;
    }
    
    // ---

    /**
     * Действия при входе в объявление именованного типа — класса или интерфейса.
     * 
     * Устанавливается текущее название класса, сбрасывается флаг маскирования
     * методов, в результаты записывается существование определенного класса
     * в некотором пакете с некоторым суперклассом (или нулевым).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(TypeDeclaration node) {
        curType = node.getName().toString();
        maskMethods = false;
        
        if (node.getSuperclassType() != null) {
            if (node.getSuperclassType().isSimpleType()) {
                writer.addClass(curType, curPackage, ((SimpleType)node.getSuperclassType()).getName().getFullyQualifiedName());
            } else if (node.getSuperclassType().isQualifiedType()) {
                writer.addClass(curType, curPackage, ((QualifiedType)node.getSuperclassType()).getName().getFullyQualifiedName());
            }
        } else {
            writer.addClass(curType, curPackage, null);
        }
        
        return true;
    }
    
    /**
     * Действия при входе в объявление анонимного класса.
     * 
     * Устанавливается маскирование методов — для того, чтобы объем и сложность
     * считались в пользу метода, где определяется анонимный класс.
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        maskMethods = true;
        return true;
    }
    
    /**
     * Действия при выходе из объявления анонимного класса.
     * 
     * Снимается маскирование методов.
     * 
     * @param node Узел синтаксического дерева
     */
    @Override
    public void endVisit(AnonymousClassDeclaration node) {
        maskMethods = false;
    }
    
    // ---

    /**
     * Действия при входе в объявление метода.
     * 
     * Производятся только если не установлено маскирование. Устанавливается
     * текущее название метода, сбрасываются счетчики выражений и ветвлений,
     * в результаты заносится существование метода.
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(MethodDeclaration node) {
        if (maskMethods) {
            return true;
        }
        
        curMethod = node.getName().toString();
        curStmtCount = 0;
        curControlCount = 0;
        writer.addMethod(curMethod, curType, curPackage);
        return true;
    }
    
    /**
     * Действия при выходе из объявления метода.
     * 
     * В результаты записывается длина и сложность метода (количество выражений
     * и количество ветвлений).
     * 
     * @param node 
     */
    @Override
    public void endVisit(MethodDeclaration node) {
        writer.setMethodSize(curStmtCount, curMethod, curType, curPackage);
        writer.setMethodComplexity(curControlCount, curMethod, curType, curPackage);
    }
    
    // ---
    
    /**
     * Универсальная функция входа в узлы дерева, являющиеся выражениями. 
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево 
     */
    private boolean statementVisit(Statement node) {
        curStmtCount++;
        return true;
    }
    
    /**
     * Универсальная функция входа в узлы дерева, являющиеся вносящими ветвления
     * выражениями. 
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево 
     */
    private boolean controlFlowStmtVisit(Statement node) {
        curControlCount++;
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="Более или менее произвольный набор выражений, обрабатываемых через statementVisit()">
    /**
     * Действия при входе в assert.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(AssertStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в break.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(BreakStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в вызов конструктора.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ConstructorInvocation node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в continue.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ContinueStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в do.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(DoStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    /**
     * Действия при входе в выражение.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ExpressionStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в for.
     * 
     * Вызывает statementVisit(node) && controlFlowStmtVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ForStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    /**
     * Действия при входе в if.
     * 
     * Вызывает statementVisit(node) && controlFlowStmtVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(IfStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    /**
     * Действия при входе в return.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ReturnStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в switch.
     * 
     * Вызывает statementVisit(node) && controlFlowStmtVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(SwitchStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    /**
     * Действия при входе в throw.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(ThrowStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в try.
     * 
     * Вызывает statementVisit(node) && controlFlowStmt(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(TryStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    /**
     * Действия при входе в объявление переменной.
     * 
     * Вызывает statementVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        return statementVisit(node);
    }
    
    /**
     * Действия при входе в while.
     * 
     * Вызывает statementVisit(node) && controlFlowStmtVisit(node).
     * 
     * @param node Узел синтаксического дерева
     * @return Входить ли в поддерево
     */
    @Override
    public boolean visit(WhileStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    //</editor-fold>
    
    
}