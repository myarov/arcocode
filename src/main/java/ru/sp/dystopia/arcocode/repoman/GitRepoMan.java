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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Менеджер git-репозитория.
 * @author Maxim Yarov
 */
public class GitRepoMan implements RepoMan {
    private FileRepository repo;
    
    private String URI;
    private String user;
    private String pass;
    private File dir;
    
    /**
     * Выбор удаленного репозитория.
     * 
     * Данная функция позволяет выбрать удаленный репозиторий, 
     * с которого будет производится клонирование файлов с целью последующей обработки.
     * 
     * @param URI - адрес локального репозитория.
     * @param user - имя пользователя.
     * @param pass - пароль.
     */
    @Override
    public void setRemoteRepo(String URI, String user, String pass) {
        this.URI = URI;
        this.user = user;
        this.pass = pass;
    }
    
    /**
     * Выбор локального репозитория.
     * 
     * Данная функция позволяет выбрать каталог, 
     * в котором будет располагаться локальная копия репозитория.
     * 
     * @param dir каталог, в котором будет расположен репозиторий.
     */
    @Override
    public void setLocalDir(File dir) {
        this.dir = dir;
    }
    
    /**
     * Получение данных из репозитория.
     */
    @Override
    public boolean collect() {
        CloneCommand cmd;
        
        cmd = new CloneCommand().setURI(URI).setDirectory(dir);
        
        if (user != null && pass != null) {
            cmd = cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass));
        }
        
        try {
            cmd.call();
        } catch (GitAPIException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
            return false;
        }
        
        try {
            repo = new FileRepository(new File(dir, ".git"));
        } catch (IOException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
            return false;
        }
        
        return true;
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
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
        } catch (IncorrectObjectTypeException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
        } catch (RevisionSyntaxException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
        }
        //</editor-fold>
        
        if (head == null) {
            return null;
        }
        
        StringWriter buf = new StringWriter();
        try {
            head.copyTo(buf);
        } catch (IOException ex) {
            Logger.getLogger(GitRepoMan.class.getName()).log(Level.INFO, null, ex);
        }
        return buf.toString();
    }
    
}
