package ru.sp.dystopia.arcocode.repoman;

import java.io.File;

/**
 * Менеджер Mercurial-репозитория.
 * 
 * @author ???? ?????????
 */
public class HgRepoMan implements RepoMan {

    @Override
    public void setRemoteRepo(String URI, String user, String pass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocalDir(File dir) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean collect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLastRevision() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
