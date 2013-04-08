package ru.sp.dystopia.arcocode.metrics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, записывающий собранные метрики в СУБД PostgreSQL.
 * @author Maxim Yarov
 */
@Deprecated
public class PostgresWriter implements MetricsWriter {
    Connection sqlConnection;
    
    final String strAddPackage = "INSERT INTO ac_packages (codebase, name) VALUES (?, ?)";
    PreparedStatement sqlAddPackage;
    
    final String url = "jdbc:postgresql://localhost/arcodb";
    final String user = "arcouser";
    final String password = "arcopassword";
    
    /**
     * todo
     */
    public PostgresWriter() {
        try {
            sqlConnection = DriverManager.getConnection(url, user, password);
            sqlAddPackage = sqlConnection.prepareStatement(strAddPackage);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * todo
     */
    @Override
    public void reset() {
        
    }
    /**
     * Деинициализация.
     */
    public void deinit() {
        try {
            if (sqlAddPackage != null) {
                sqlAddPackage.close();
            }
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PostgresWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Добавление пакета в структуру результата.
     * @param strPackage - имя пакета.
     */
    @Override
    public void addPackage(String strPackage) {
        try {
            sqlAddPackage.setInt(1, 1); // FIXME
            sqlAddPackage.setString(2, strPackage);
            sqlAddPackage.execute();
        } catch (SQLException ex) {
            Logger.getLogger(PostgresWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Добавления связей между пакетами.
     * @param importerPackage - "что".
     * @param importeePackage - "кого".
     */
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
    }
    /**
     * Добавление класса в некоторый пакет.
     * @param strClass - класс.
     * @param strPackage - пакет.
     * @param strParent - "родитель".
     */
    @Override
    public void addClass(String strClass, String strPackage, String strParent) {
    }
    /**
     * Добавление метода в класс.
     * @param strMethod - метод.
     * @param strClass - класс.
     * @param strPackage - пакет.
     */
    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
    }
    /**
     * Установка размера (количество выражений) метода.
     * @param size - размер.
     * @param strMethod - метод.
     * @param strClass - класс.
     * @param strPackage - пакет.
     */
    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
    }
    /**
     * Установка сложности метода (количество ветвлений).
     * @param complexity - сложность метода.
     * @param strMethod - метод.
     * @param strClass - класс.
     * @param strPackage - пакет.
     */
    @Override
    public void setMethodComplexity(int complexity, String strMethod, String strClass, String strPackage) {
    }
}
