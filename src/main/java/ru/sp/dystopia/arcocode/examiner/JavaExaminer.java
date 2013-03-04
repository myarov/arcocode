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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Класс разбора исходных файлов Java.
 * @author Maxim Yarov
 */
public class JavaExaminer {
    public void parse(char[] src) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(src);
        
        CompilationUnit cu = (CompilationUnit)parser.createAST(null);
        
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                System.out.println("Method " + node.getName() + " declared.");
                return true;
            }
        });
    }
    
    public void walk(File root) {
        walkRecursor(root, new ArrayList<File>());
    }
    
    private void walkRecursor(File parent, ArrayList<File> accumulator) {
        File[] children = parent.listFiles();
        if (children != null) {
            for (File child: children) {
                accumulator.add(child);
                
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
                
                walkRecursor(child, accumulator);
            }
        }
    }
}
