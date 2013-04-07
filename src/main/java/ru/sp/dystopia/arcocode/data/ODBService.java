package ru.sp.dystopia.arcocode.data;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
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
    
    public final static String ODB_CLASS_NAME = "Project";
    public final static String ODB_ID_FIELD_NAME = "name";
    public final static String ODB_ID_INDEX_NAME = "nameIdx";
    public final static String ODB_ADDED_ON_FIELD_NAME = "addedOn";
    public final static String ODB_STATUS_FIELD_NAME = "status";
    public final static String ODB_STAGE_FIELD_NAME = "stage";
    public final static String ODB_REASON_FIELD_NAME = "reason";
    public final static String ODB_URI_FIELD_NAME = "uri";
    public final static String ODB_REVISION_FIELD_NAME = "revision";
    
    public final static String ODB_WORKING_STATUS = "processing";
    public final static String ODB_ERROR_STATUS = "failed";
    
    public final static String ODB_PARSE_STAGE = "preparing";
    public final static String ODB_COLLECT_STAGE = "downloading";
    public final static String ODB_EXAMINE_STAGE = "parsing";
    
    public final static String ODB_MALFORMED_REASON = "malformed";
    public final static String ODB_INTERNAL_REASON = "internal";
    public final static String ODB_COLLECT_REASON = "download problem";
    
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
    
    private static Result performAction(DBActionInterface action, String param1) {
        ODatabaseDocumentTx db = null;
        Result res;
        
        try {
            db = getDbObj();
            res = action.act(db, param1);
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return Result.ODB_DB_ERROR;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return res;
    }
    
    private static Result findAndModify(String name, DocumentModificationInterface modifier, String param1) {
        ODatabaseDocumentTx db = null;
        OIndex index;
        OIdentifiable match;
        ODocument doc;
        
        try {
            db = getDbObj();
            index = db.getMetadata().getIndexManager().getIndex(ODB_ID_INDEX_NAME);
            match = (OIdentifiable)index.get(name);
            if (match != null) {
                doc = (ODocument)match.getRecord();
                modifier.modify(doc, param1);
                doc.save();
            } else {
                Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, "Could not find project {0}", name);
                return Result.ODB_DB_ERROR;
            }
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
    
    public static Result projectExists(String name) {
        return performAction(new ProjectExistsAction(), name);
    }
    
    public static Result addProject(String name) {
        return performAction(new AddProjectAction(), name);
    }
    
    public static Result projectParseDone(String name, String uri) {
        return findAndModify(name, new ProjectParseDoneAction(), uri);
    }
    
    public static Result projectCollectDone(String name, String revision) {
        return findAndModify(name, new ProjectCollectDoneAction(), revision);
    }
    
    public static Result projectErrorMalformed(String name) {
        return findAndModify(name, new ProjectErrorMalformedAction(), null);
    }
    
    public static Result projectErrorInternal(String name) {
        return findAndModify(name, new ProjectErrorInternalAction(), null);
    }
    
    public static Result projectErrorCollectFailed(String name) {
        return findAndModify(name, new ProjectErrorCollectFailedAction(), null);
    }
    
    public static String testFunction() {
        ODatabaseDocumentTx db = null;
        String res = "{}";
        
        try {
            db = getDbObj();
            // Actual work -->
            for (ODocument doc: db.browseClass(ODB_CLASS_NAME)) {
                res = doc.toJSON();
            }
            // <--
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

interface DBActionInterface {
    public ODBService.Result act(ODatabaseDocumentTx db, String param1);
}

class ProjectExistsAction implements DBActionInterface {
    public ODBService.Result act(ODatabaseDocumentTx db, String name) {
        OIndex index;
        boolean found;
        
        index = db.getMetadata().getIndexManager().getIndex(ODBService.ODB_ID_INDEX_NAME);
        found = index.contains(name);
        
        return (found ? ODBService.Result.ODB_TRUE : ODBService.Result.ODB_FALSE);
    }
}

class AddProjectAction implements DBActionInterface {
    @Override
    public ODBService.Result act(ODatabaseDocumentTx db, String name) {
        ODocument doc;
        DateFormat dateFrmt;
        
        doc = new ODocument(ODBService.ODB_CLASS_NAME);
        
        dateFrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        doc.field(ODBService.ODB_ID_FIELD_NAME, name);
        doc.field(ODBService.ODB_ADDED_ON_FIELD_NAME, dateFrmt.format(new Date()));
        doc.field(ODBService.ODB_STATUS_FIELD_NAME, ODBService.ODB_WORKING_STATUS);
        doc.field(ODBService.ODB_STAGE_FIELD_NAME, ODBService.ODB_PARSE_STAGE);
        
        doc.save();
        
        return ODBService.Result.ODB_OK;
    }
}

interface DocumentModificationInterface {
    public void modify(ODocument doc, String param1);
}

class ProjectParseDoneAction implements DocumentModificationInterface {
    @Override
    public void modify(ODocument doc, String uri) {
        doc.field(ODBService.ODB_STAGE_FIELD_NAME, ODBService.ODB_COLLECT_STAGE);
        doc.field(ODBService.ODB_URI_FIELD_NAME, uri);
    }
}

class ProjectCollectDoneAction implements DocumentModificationInterface {
    @Override
    public void modify(ODocument doc, String revision) {
        doc.field(ODBService.ODB_STAGE_FIELD_NAME, ODBService.ODB_EXAMINE_STAGE);
        doc.field(ODBService.ODB_REVISION_FIELD_NAME, revision);
    }
}

class ProjectErrorMalformedAction implements DocumentModificationInterface {
    @Override
    public void modify(ODocument doc, String _) {
        doc.field(ODBService.ODB_STATUS_FIELD_NAME, ODBService.ODB_ERROR_STATUS);
        doc.field(ODBService.ODB_REASON_FIELD_NAME, ODBService.ODB_MALFORMED_REASON);
    }
}

class ProjectErrorInternalAction implements DocumentModificationInterface {
    @Override
    public void modify(ODocument doc, String _) {
        doc.field(ODBService.ODB_STATUS_FIELD_NAME, ODBService.ODB_ERROR_STATUS);
        doc.field(ODBService.ODB_REASON_FIELD_NAME, ODBService.ODB_INTERNAL_REASON);
    }
}

class ProjectErrorCollectFailedAction implements DocumentModificationInterface {
    @Override
    public void modify(ODocument doc, String _) {
        doc.field(ODBService.ODB_STATUS_FIELD_NAME, ODBService.ODB_ERROR_STATUS);
        doc.field(ODBService.ODB_REASON_FIELD_NAME, ODBService.ODB_COLLECT_REASON);
    }
}