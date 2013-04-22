package ru.sp.dystopia.arcocode.repoman;

import java.io.File;

/**
 * Менеджер SVN-репозитория.
 * 
 * @author ????????? ??????
 */
public class SVNRepoMan implements RepoMan {
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
