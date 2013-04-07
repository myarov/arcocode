package ru.sp.dystopia.arcocode.data;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexException;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Набор функция для работы с базой данных OrientDB.
 * 
 * @author Maxim Yarov
 */
public class ODBService {
    public enum Result {ODB_OK, ODB_DB_ERROR, ODB_DUPLICATE};
    
    private final static String ODB_CLASS_NAME = "Project";
    private final static String ODB_ID_FIELD_NAME = "name";
    private final static String ODB_ID_INDEX_NAME = "nameIdx";
    
    private static File getDbFile() {
        File file = new File(System.getProperty("com.sun.aas.instanceRoot"));
        file = new File(file, "lib");
        file = new File(file, "databases");
        file = new File(file, "arcocode");
        return file;
    }
    
    private static ODatabaseDocumentTx getDbObj() {
        File dbFile = getDbFile();
        if (!dbFile.exists()) {
            createDb(dbFile);
        }
        
        return ODatabaseDocumentPool.global().acquire("local:" + dbFile, "admin", "admin");
    }
    
    private static void createDb(File dbFile) {
        ODatabaseDocumentTx db = new ODatabaseDocumentTx("local:" + dbFile).create();
        
        OClass projectClass = db.getMetadata().getSchema().createClass(ODB_CLASS_NAME);
        projectClass.createProperty(ODB_ID_FIELD_NAME, OType.STRING).setMandatory(true).setNotNull(true);
        projectClass.createIndex(ODB_ID_INDEX_NAME, OClass.INDEX_TYPE.UNIQUE, ODB_ID_FIELD_NAME);
        
        db.close();
    }
    
    public static long countProjects() {
        ODatabaseDocumentTx db = getDbObj();
        return db.countClass(ODB_CLASS_NAME);
    }
    
    public static boolean projectExists(String name) {
        ODatabaseDocumentTx db = getDbObj();
        
        OIndex index = db.getMetadata().getIndexManager().getIndex(ODB_ID_INDEX_NAME);
        
        return index.contains(name);
    }
    
    public static Result addProject(String name, String data) {
        // Connect to the database
        ODatabaseDocumentTx db;
        try {
            db = getDbObj();
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return Result.ODB_DB_ERROR;
        }
        
        try {
            // Form a new entry
            ODocument doc = new ODocument(ODB_CLASS_NAME);
            try {
                doc.fromJSON(data);
                doc.field(ODB_ID_FIELD_NAME, name);   
            } catch (OException ex) {
                Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
                return Result.ODB_DB_ERROR;
            }

            // Commit the entry and disconnect
            try {
                doc.save();
            } catch (OIndexException ex) {
                // Project with this name already exists
                //return Result.ODB_DUPLICATE;
                return Result.ODB_DB_ERROR;
            } catch (OException ex) {
                Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
                return Result.ODB_DB_ERROR;
            }
        } finally {
            db.close();
        }
        
        return Result.ODB_OK;
    }
}
