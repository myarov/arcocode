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
    
    public PostgresWriter() {
        try {
            sqlConnection = DriverManager.getConnection(url, user, password);
            sqlAddPackage = sqlConnection.prepareStatement(strAddPackage);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void reset() {
        
    }
    
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
    
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
    }

    @Override
    public void addClass(String strClass, String strPackage, String strParent) {
    }
    
    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
    }

    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
    }
    
    @Override
    public void setMethodComplexity(int complexity, String strMethod, String strClass, String strPackage) {
    }
}
