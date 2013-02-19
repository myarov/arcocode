package ru.sp.dystopia.arcocode.repoman;

import java.io.IOException;

/**
 * Интерфейс менеджера репозитория.
 * @author Maxim Yarov
 */
public interface RepoMan {
    void collect(String URI, String localPath);
    String getLastRevision();
}
