package ru.sp.dystopia.arcocode.api;

/**
 * Класс, реализующий RESTful API системы.
 * 
 * @author Maxim Yarov
 */
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class REST
{
    private static File getDbFile() {
        File file = new File(System.getProperty("com.sun.aas.instanceRoot"));
        file = new File(file, "lib");
        file = new File(file, "databases");
        file = new File(file, "arcocode");
        return file;
    }
    
    private static ODatabaseDocumentTx getDbObj() {
        File dbFile = getDbFile();
        if (dbFile.exists()) {
            return new ODatabaseDocumentTx("local:" + dbFile).open("admin", "admin");
        } else {
            return new ODatabaseDocumentTx("local:" + dbFile).create();
        }
    }
    
    @GET
    @Produces("text/plain")
    public String get() {
        String res;
        
        ODatabaseDocumentTx db = getDbObj();
        
        res = String.format("N: %d", db.countClass("Repo"));
        
        db.close();
        
        return res;
    }
    
    @PUT
    @Consumes("application/json")
    public void put(String message) {
        ODatabaseDocumentTx db = getDbObj();
        
        ODocument doc = new ODocument("Repo");
        doc.fromJSON(message);
        
        doc.save();
        
        db.close();
    }
}