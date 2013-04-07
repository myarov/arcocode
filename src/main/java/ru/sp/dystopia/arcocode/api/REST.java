package ru.sp.dystopia.arcocode.api;

/**
 * Класс, реализующий RESTful API системы.
 * 
 * @author Maxim Yarov
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import ru.sp.dystopia.arcocode.data.ODBService;

@Path("/")
public class REST
{
    @Context ServletContext context;
    
    private final static String JSON_EMPTY = "{}";
    private final static String JSON_SERVER_ERROR = "{\"error\": \"internal\"}";
    private final static String JSON_DUPLICATE_ERROR = "{\"error\": \"duplicate\"}";
    
    private static Response rAccepted() {
        return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON).entity(JSON_EMPTY).build();
    }
    
    private static Response rServerError() {
        return Response.serverError().type(MediaType.APPLICATION_JSON).entity(JSON_SERVER_ERROR).build();
    }
    
    private static Response rDuplicateError() {
        return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON).entity(JSON_DUPLICATE_ERROR).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get() {
        return ODBService.testFunction();
    }
    
    @PUT
    @Path("/{project}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("project") String project, String message) {
        ODBService.Result res = ODBService.projectExists(project);
        
        if (res == ODBService.Result.ODB_FALSE) {
            WorkerLauncher.addTask(new WorkerTask(context, project, message));
            return rAccepted();
        } else if (res == ODBService.Result.ODB_TRUE) {
            return rDuplicateError();
        } else if (res == ODBService.Result.ODB_DB_ERROR) {
            return rServerError();
        } else {
            Logger.getLogger(REST.class.getName()).log(Level.SEVERE, "Unexpected return value: {0}", res);
            return rServerError();
        }
    }
}