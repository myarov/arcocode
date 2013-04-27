package ru.sp.dystopia.arcocode.repoman;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tmatesoft.hg.core.*;
import org.tmatesoft.hg.repo.*;
import org.tmatesoft.hg.util.CancelledException;
import org.tmatesoft.hg.util.Path;

/**
 * Менеджер Mercurial-репозитория.
 * 
 * @author ???? ?????????
 */
public class HgRepoMan implements RepoMan {

     /**
     * Адрес удаленного репозитория
     */
    private String URI;
    /**
     * Имя пользователя для выгрузки из удаленного репозитория
     */
    private String user;
    /**
     * Пароль для выгрузки из удаленного репозитория
     */
    private String pass;
    /**
     * Объект java.io.File, указывающий на место для локальной копии репозитория
     */
    private File dir;
    
    
    HgRemoteRepository hgRemote=null;
    HgRepoFacade hgRepo = null;
    
    @Override
    public void setRemoteRepo(String URI, String user, String pass) {
        this.URI = URI;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public void setLocalDir(File dir) {
        this.dir = dir;
    }

    @Override
    public boolean collect() {
        
        try {
            hgRemote=new HgLookup().detect(new URL(URI));
        } catch (HgBadArgumentException ex) {
            Logger.getLogger(HgRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HgRepoMan.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(hgRemote==null) 
        {
            return false;
        }

        HgCloneCommand cmd = new HgCloneCommand();
        cmd.source(hgRemote);
        cmd.destination(this.dir);
        try {
            cmd.execute();
        } catch (HgException ex) {
            Logger.getLogger(HgRepoMan.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (CancelledException ex) {
            Logger.getLogger(HgRepoMan.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        hgRepo = new HgRepoFacade();
        try {
            hgRepo.initFrom(this.dir);
        } catch (HgRepositoryNotFoundException ex) {
            Logger.getLogger(HgRepoMan.class.getName()).log(Level.SEVERE, null, ex);
            System.err.printf("Can't find repository in: %s\n", hgRepo.getRepository().getLocation());
            return false;
        }

        return true;
    }

    @Override
    public String getLastRevision() {
        if(hgRepo==null)
        {
            return "";
        }
        
        int lastRevision = hgRepo.getRepository().getChangelog().getLastRevision();
        Nodeid lastRevisionNode=hgRepo.getRepository().getChangelog().getRevision(lastRevision);
        Integer lastRevisionIndex=hgRepo.getRepository().getChangelog().getRevisionIndex(lastRevisionNode);
        return lastRevisionIndex.toString();
    }
    
}
