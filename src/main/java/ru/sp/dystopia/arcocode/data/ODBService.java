package ru.sp.dystopia.arcocode.data;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Набор функция для работы с базой данных OrientDB.
 * 
 * @author Maxim Yarov
 */
public class ODBService {
    public enum Result {ODB_OK, ODB_DB_ERROR, ODB_DUPLICATE, ODB_TRUE, ODB_FALSE};
    
    private final static String ODB_CLASS_NAME = "Project";
    private final static String ODB_ID_FIELD_NAME = "name";
    private final static String ODB_ID_INDEX_NAME = "nameIdx";
    private final static String ODB_ADDED_ON_FIELD_NAME = "addedOn";
    private final static String ODB_STATUS_FIELD_NAME = "status";
    private final static String ODB_STAGE_FIELD_NAME = "stage";
    
    private final static String ODB_WORKING_STATUS = "processing";
    
    private final static String ODB_PARSE_STAGE = "preparing";
    private final static String ODB_COLLECT_STAGE = "downloading";
    private final static String ODB_EXAMINE_STAGE = "parsing";
    
    private static File getDbFile() {
        File file = new File(System.getProperty("com.sun.aas.instanceRoot"));
        file = new File(file, "lib");
        file = new File(file, "databases");
        file = new File(file, "arcocode");
        return file;
    }
    
    private static ODatabaseDocumentTx getDbObj() {
        File dbFile = getDbFile();
        
        createDbIfNeeded();
        
        return ODatabaseDocumentPool.global().acquire("local:" + dbFile, "admin", "admin");
    }
    
    public static void createDbIfNeeded() {
        File dbFile = getDbFile();
        if (dbFile.exists()) {
            return;
        }
        
        ODatabaseDocumentTx db = new ODatabaseDocumentTx("local:" + dbFile).create();
        
        OClass projectClass = db.getMetadata().getSchema().createClass(ODB_CLASS_NAME);
        projectClass.createProperty(ODB_ID_FIELD_NAME, OType.STRING).setMandatory(true).setNotNull(true);
        projectClass.createIndex(ODB_ID_INDEX_NAME, OClass.INDEX_TYPE.UNIQUE, ODB_ID_FIELD_NAME);
        
        db.close();
    }
    
    public static Result projectExists(String name) {
        ODatabaseDocumentTx db = null;
        OIndex index;
        boolean found;
        
        try {
            db = getDbObj();
            index = db.getMetadata().getIndexManager().getIndex(ODB_ID_INDEX_NAME);
            found = index.contains(name);
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return Result.ODB_DB_ERROR;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return (found ? Result.ODB_TRUE : Result.ODB_FALSE);
    }
    
    public static Result addProject(String name) {
        ODatabaseDocumentTx db = null;
        ODocument doc;
        
        try {
            db = getDbObj();
            doc = new ODocument(ODB_CLASS_NAME);
            projectSetup(doc, name);
            doc.save();
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return Result.ODB_DB_ERROR;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return Result.ODB_OK;
    }
    
    private static void projectSetup(ODocument doc, String name) {
        DateFormat dateFrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        doc.field(ODB_ID_FIELD_NAME, name);
        doc.field(ODB_ADDED_ON_FIELD_NAME, dateFrmt.format(new Date()));
        doc.field(ODB_STATUS_FIELD_NAME, ODB_WORKING_STATUS);
        doc.field(ODB_STAGE_FIELD_NAME, ODB_PARSE_STAGE);
    }
    
    public static String testFunction() {
        ODatabaseDocumentTx db = null;
        String res = "{}";
        
        try {
            db = getDbObj();
            for (ODocument doc: db.browseClass(ODB_CLASS_NAME)) {
                res = doc.toJSON();
            }
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return res;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return res;
    }
}
