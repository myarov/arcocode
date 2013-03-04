package ru.sp.dystopia.arcocode;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.sp.dystopia.arcocode.examiner.JavaExaminer;
import ru.sp.dystopia.arcocode.repoman.GitRepoMan;
import ru.sp.dystopia.arcocode.repoman.RepoMan;

public class App 
{
    public static void main( String[] args )
    {
        if (args.length < 2) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Too few arguments.");
        }
        
        //RepoMan rm = new GitRepoMan();
        //rm.collect(args[0], args[1]);
        //System.out.println(rm.getLastRevision());
        JavaExaminer je = new JavaExaminer();
        je.walk(new File(args[1]));
    }
}
