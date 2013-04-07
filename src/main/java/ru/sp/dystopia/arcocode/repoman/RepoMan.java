package ru.sp.dystopia.arcocode.repoman;

import java.io.File;

/**
 * Интерфейс менеджера репозитория.
 * @author Maxim Yarov
 */
public interface RepoMan {
    void setRemoteRepo(String URI, String user, String pass);
    void setLocalDir(File dir);
    boolean collect();
    String getLastRevision();
}
