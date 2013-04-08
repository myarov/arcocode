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
    
    public final static String ODB_PROJECT_CLASS = "Project";
    public final static String ODB_METRICS_CLASS = "Metrics";
    public final static String ODB_ID_INDEX = "nameIdx";
    
    public final static String ODB_ID_FIELD = "name";
    public final static String ODB_ADDED_ON_FIELD = "addedOn";
    public final static String ODB_STATUS_FIELD = "status";
    public final static String ODB_STAGE_FIELD = "stage";
    public final static String ODB_REASON_FIELD = "reason";
    public final static String ODB_URI_FIELD = "uri";
    public final static String ODB_REVISION_FIELD = "revision";
    public final static String ODB_METRICS_FIELD = "metrics";
    
    public final static String ODB_WORKING_STATUS = "processing";
    public final static String ODB_ERROR_STATUS = "failed";
    public final static String ODB_DONE_STATUS = "done";
    
    public final static String ODB_PARSE_STAGE = "preparing";
    public final static String ODB_COLLECT_STAGE = "downloading";
    public final static String ODB_EXAMINE_STAGE = "parsing";
    public final static String ODB_DONE_STAGE = "done";
    
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
        
        OClass projectClass = db.getMetadata().getSchema().createClass(ODB_PROJECT_CLASS);
        projectClass.createProperty(ODB_ID_FIELD, OType.STRING).setMandatory(true).setNotNull(true);
        projectClass.createIndex(ODB_ID_INDEX, OClass.INDEX_TYPE.UNIQUE, ODB_ID_FIELD);
        
        db.close();
    }
    
    private static Result performAction(DBActionInterface action) {
        ODatabaseDocumentTx db = null;
        Result res;
        
        try {
            db = getDbObj();
            res = action.act(db);
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
    
    private static Result findAndModify(String name, DocumentModificationInterface modifier) {
        ODatabaseDocumentTx db = null;
        OIndex index;
        OIdentifiable match;
        ODocument doc;
        
        try {
            db = getDbObj();
            index = db.getMetadata().getIndexManager().getIndex(ODB_ID_INDEX);
            match = (OIdentifiable)index.get(name);
            if (match != null) {
                doc = (ODocument)match.getRecord();
                modifier.modify(doc);
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
    
    
    public static Result projectExists(final String name) {
        return performAction(new DBActionInterface() {
            @Override
            public ODBService.Result act(ODatabaseDocumentTx db) {
                OIndex index;
                boolean found;

                index = db.getMetadata().getIndexManager().getIndex(ODBService.ODB_ID_INDEX);
                found = index.contains(name);

                return (found ? ODBService.Result.ODB_TRUE : ODBService.Result.ODB_FALSE);
            }
        });
    }
    
    public static Result addProject(final String name) {
        return performAction(new DBActionInterface() {
            @Override
            public ODBService.Result act(ODatabaseDocumentTx db) {
                ODocument doc;
                DateFormat dateFrmt;

                doc = new ODocument(ODBService.ODB_PROJECT_CLASS);

                dateFrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                doc.field(ODBService.ODB_ID_FIELD, name);
                doc.field(ODBService.ODB_ADDED_ON_FIELD, dateFrmt.format(new Date()));
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_WORKING_STATUS);
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_PARSE_STAGE);

                doc.save();

                return ODBService.Result.ODB_OK;
            }
        });
    }
    
    public static Result projectParseDone(String name, final String uri) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_COLLECT_STAGE);
                doc.field(ODBService.ODB_URI_FIELD, uri);
            }
        });
    }
    
    public static Result projectCollectDone(String name, final String revision) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_EXAMINE_STAGE);
                doc.field(ODBService.ODB_REVISION_FIELD, revision);
            }
        });
    }
    
    public static Result projectComplete(String name, final String metricsJSON) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify (ODocument doc) {
                ODocument metrics = new ODocument(ODBService.ODB_METRICS_CLASS);
                metrics.fromJSON(metricsJSON);

                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_DONE_STATUS);
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_DONE_STAGE);
                doc.field(ODBService.ODB_METRICS_FIELD, metrics);
            }
        });
    }
    
    public static Result projectErrorMalformed(String name) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_ERROR_STATUS);
                doc.field(ODBService.ODB_REASON_FIELD, ODBService.ODB_MALFORMED_REASON);
            }
        });
    }
    
    public static Result projectErrorInternal(String name) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_ERROR_STATUS);
                doc.field(ODBService.ODB_REASON_FIELD, ODBService.ODB_INTERNAL_REASON);
            }
        });
    }
    
    public static Result projectErrorCollectFailed(String name) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_ERROR_STATUS);
                doc.field(ODBService.ODB_REASON_FIELD, ODBService.ODB_COLLECT_REASON);
            }
        });
    }
    
    public static String testFunction() {
        ODatabaseDocumentTx db = null;
        String res = "{}";
        
        try {
            db = getDbObj();
            // Actual work -->
            for (ODocument doc: db.browseClass(ODB_PROJECT_CLASS)) {
                res = doc.toJSON();
                ODocument metrics = doc.field(ODB_METRICS_FIELD);
                if (metrics != null) {
                    res = res + metrics.toJSON();
                }
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
    public ODBService.Result act(ODatabaseDocumentTx db);
}

interface DocumentModificationInterface {
    public void modify(ODocument doc);
}