package ru.sp.dystopia.arcocode;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

public class App 
{
    public static void main( String[] args )
    {
        Repository repo = null;
        ObjectId head = null;
        
        try {
            repo = new FileRepository(
                new File(System.getProperty("user.dir"), ".git"));
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        try {
            if (repo != null) {
                head = repo.resolve("HEAD");
            }
        } //<editor-fold defaultstate="collapsed" desc="exception handlers">
        catch (AmbiguousObjectException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IncorrectObjectTypeException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RevisionSyntaxException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        System.out.println("HEAD object id: " + head);
    }
}
