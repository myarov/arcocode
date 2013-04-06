package ru.sp.dystopia.arcocode.api;

/**
 * Класс, реализующий RESTful API системы.
 * 
 * @author Maxim Yarov
 */
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.index.OIndexException;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class REST
{
    private final static String ODB_CLASS_NAME = "Project";
    private final static String ODB_ID_FIELD_NAME = "name";
    private final static String ODB_ID_INDEX_NAME = "nameIdx";
    
    private final static String JSON_EMPTY = "{}";
    private final static String JSON_SERVER_ERROR = "{\"error\": \"internal\"}";
    private final static String JSON_MALFORMED_ERROR = "{\"error\": \"malformed\"}";
    private final static String JSON_DUPLICATE_ERROR = "{\"error\": \"duplicate\"}";
    
    private static Response rAccepted() {
        return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON).entity(JSON_EMPTY).build();
    }
    
    private static Response rServerError() {
        return Response.serverError().type(MediaType.APPLICATION_JSON).entity(JSON_SERVER_ERROR).build();
    }
    
    private static Response rMalformedError() {
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(JSON_MALFORMED_ERROR).build();
    }
    
    private static Response rDuplicateError() {
        return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON).entity(JSON_DUPLICATE_ERROR).build();
    }
    
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
    
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        String res;
        
        ODatabaseDocumentTx db = getDbObj();
        
        res = String.format("N: %d", db.countClass(ODB_CLASS_NAME));
        
        db.close();
        
        return res;
    }
    
    @PUT
    @Path("/{project}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("project") String project, String message) {
        // Connect to the database
        ODatabaseDocumentTx db;
        try {
            db = getDbObj();
        } catch (OException ex) {
            Logger.getLogger(REST.class.getName()).log(Level.SEVERE, null, ex);
            return rServerError();
        }
            
        try {
            // Form a new entry
            ODocument doc = new ODocument(ODB_CLASS_NAME);
            try {
                doc.fromJSON(message);
                doc.field(ODB_ID_FIELD_NAME, project);   
            } catch (OSerializationException ex) {
                Logger.getLogger(REST.class.getName()).log(Level.INFO, null, ex);
                return rMalformedError();
            } catch (OException ex) {
                Logger.getLogger(REST.class.getName()).log(Level.SEVERE, null, ex);
                return rServerError();
            }

            // Commit the entry and disconnect
            try {
                doc.save();
            } catch (OIndexException ex) {
                // Project with this name already exists
                return rDuplicateError();
            } catch (OException ex) {
                Logger.getLogger(REST.class.getName()).log(Level.SEVERE, null, ex);
                return rServerError();
            }
        } finally {
            db.close();
        }
        
        return rAccepted();
    }
}