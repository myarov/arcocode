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
    String curPackage;
    String curType;
    String curMethod;
    int curStmtCount;
    int curControlCount;
    
    MetricsWriter writer;

    /**
     * 
     * @param writer 
     */
    public JavaVisitor(MetricsWriter writer) {
        this.writer = writer;
    }
    
    // ---
    
    /**
     * 
     * @param node
     * @return 
     */
    @Override
    public boolean visit(PackageDeclaration node) {
        curPackage = node.getName().toString();
        writer.addPackage(curPackage);
        return true;
    }
    
    // ---
    
    /**
     * 
     * @param node
     * @return 
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

    @Override
    public boolean visit(TypeDeclaration node) {
        curType = node.getName().toString();
        
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
    
    // ---

    @Override
    public boolean visit(MethodDeclaration node) {
        curMethod = node.getName().toString();
        curStmtCount = 0;
        curControlCount = 0;
        writer.addMethod(curMethod, curType, curPackage);
        return true;
    }
    
    @Override
    public void endVisit(MethodDeclaration node) {
        writer.setMethodSize(curStmtCount, curMethod, curType, curPackage);
        writer.setMethodComplexity(curControlCount, curMethod, curType, curPackage);
    }
    
    // ---
    
    private boolean statementVisit(Statement node) {
        curStmtCount++;
        return true;
    }
    
    private boolean controlFlowStmtVisit(Statement node) {
        curControlCount++;
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="Более или менее произвольный набор выражений, обрабатываемых через statementVisit()">
    @Override
    public boolean visit(AssertStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(ConstructorInvocation node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(DoStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(ExpressionStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(ForStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    @Override
    public boolean visit(IfStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(TryStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        return statementVisit(node) && controlFlowStmtVisit(node);
    }
    //</editor-fold>
    
    
}