package ru.sp.dystopia.arcocode.examiner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Класс разбора исходных файлов Java.
 * @author Maxim Yarov
 */
public class JavaExaminer {
    public void parse(char[] src) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(src);
        
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        
        cu.accept(new JavaVisitor(new PlainTextWriter()));
    }
    
    public void walk(File root) {
        walkRecursor(root);
    }
    
    private void walkRecursor(File parent) {
        File[] children = parent.listFiles();
        if (children != null) {
            for (File child: children) {
                if (child.getName().endsWith(".java")) {
                    try {
                        // If your .java files are larger than MAX_INT, you are
                        // doing something seriously wrong.
                        char buf[] = new char[(int)child.length()];
                        
                        FileReader fr = new FileReader(child);
                        fr.read(buf);
                                
                        parse(buf);        
                                
                    } catch (IOException ex) {
                        Logger.getLogger(JavaExaminer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                walkRecursor(child);
            }
        }
    }
}

/**
 * Класс, который непосредственно обходит синтаксическое дерево AST.
 * @author Maxim Yarov
 */
class JavaVisitor extends ASTVisitor
{
    String currentPackage;
    String currentType;
    String currentMethod;
    int currentStmtCount;
    
    MetricsWriter writer;

    public JavaVisitor(MetricsWriter writer) {
        this.writer = writer;
    }
    
    // ---
    
    @Override
    public boolean visit(PackageDeclaration node) {
        currentPackage = node.getName().toString();
        writer.addPackage(currentPackage);
        return true;
    }
    
    // ---

    @Override
    public boolean visit(TypeDeclaration node) {
        currentType = node.getName().toString();
        writer.addClass(currentType, currentPackage);
        return true;
    }
    
    // ---

    @Override
    public boolean visit(MethodDeclaration node) {
        currentMethod = node.getName().toString();
        currentStmtCount = 0;
        writer.addMethod(currentMethod, currentType, currentPackage);
        return true;
    }
    
    @Override
    public void endVisit(MethodDeclaration node) {
        writer.setMethodSize(currentStmtCount, currentMethod, currentType, currentPackage);
    }
    
    // ---
    
    private boolean statementVisit(Statement node) {
        currentStmtCount++;
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
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(IfStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(TryStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        return statementVisit(node);
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        return statementVisit(node);
    }
    //</editor-fold>
    
    
}