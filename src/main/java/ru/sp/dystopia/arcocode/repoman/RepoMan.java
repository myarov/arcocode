package ru.sp.dystopia.arcocode.repoman;

import java.io.File;

/**
 * Интерфейс менеджера репозитория.
 * @author Maxim Yarov
 */
public interface RepoMan {
    /**
     * Задание параметров удаленного репозитория.
     * 
     * @param URI адрес
     * @param user имя пользователя
     * @param pass пароль
     */
    void setRemoteRepo(String URI, String user, String pass);
    /**
     * Задание места в локальной файловой системе для выгрузки удаленного кода.
     * 
     * @param dir директория под копию
     */
    void setLocalDir(File dir);
    /**
     * Выгрузка репозитория.
     * 
     * @return Успешность выполнения
     */
    boolean collect();
    /**
     * Получение строкового представления идетификатора наиболее актуальной
     * ревизии.
     * 
     * Предполагается, что функция работает на основе данных, содержащихся
     * в локальной копии.
     * 
     * @return Идентификатор последней ревизии
     */
    String getLastRevision();
}
