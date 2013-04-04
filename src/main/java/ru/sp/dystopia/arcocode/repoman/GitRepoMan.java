package ru.sp.dystopia.arcocode.repoman;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.storage.file.FileRepository;

/**
 * Менеджер git-репозитория.
 * @author Maxim Yarov
 */
public class GitRepoMan implements RepoMan {
    private FileRepository repo;
    /**
     * Получение данных из репозитория.
     * @param URI - адрес репозитория.
     * @param localPath - путь на локальной машине.
     */
    @Override
    public void collect(String URI, String localPath) {
        CloneCommand cmd;
        
        cmd = new CloneCommand().setURI(URI).setDirectory(new File(localPath));
        try {
            cmd.call();
        } catch (GitAPIException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            repo = new FileRepository(new File(localPath, ".git"));
        } catch (IOException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Получение идентификатора последней ревизии.
     *
     * @return  идентификатор последней ревизии
     *          null, если не указан репозиторий или возникла проблема подключения
     */
    @Override
    public String getLastRevision() {
        if (repo == null) {
            return null;
        }
        
        ObjectId head = null;
        try {
            head = repo.resolve("HEAD");
        } //<editor-fold defaultstate="collapsed" desc="exception handlers">
        catch (AmbiguousObjectException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IncorrectObjectTypeException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RevisionSyntaxException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        if (head == null) {
            return null;
        }
        
        StringWriter buf = new StringWriter();
        try {
            head.copyTo(buf);
        } catch (IOException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        }
        return buf.toString();
    }
    
}
