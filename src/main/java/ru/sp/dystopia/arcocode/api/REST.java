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

/**
 * Класс, реализующий RESTful API сервера.
 * 
 * Использует интерфейс JAX-RS для достижения этой цели. За реализацию
 * интерфейса отвечает сервер приложений (в частности, Glassfish).
 * 
 * @author Maxim Yarov
 */
@Path("/")
public class REST
{
    /**
     * Контекст сервлета, требуется для получения пути к директории временных
     * файлов при обработке проекта.
     */
    @Context ServletContext context;
    
    private final static String JSON_EMPTY = "{}";
    private final static String JSON_SERVER_ERROR = "{\"error\": \"internal\"}";
    private final static String JSON_DUPLICATE_ERROR = "{\"error\": \"duplicate\"}";
    
    /**
     * Возвращает HTTP-ответ, соответствующий принятому в обработку проекту.
     * 
     * @return Объект, содержащий код статуса HTTP (202) и JSON-объект (пустой)
     */
    private static Response rAccepted() {
        return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON).entity(JSON_EMPTY).build();
    }
    
    /**
     * Возвращает HTTP-ответ, соответствующий произошедшей при работе внутренней
     * ошибке сервера.
     * 
     * @return Объект, содержащий код статуса HTTP (500) и JSON-объект
     * с словесным указанием на внутреннюю ошибку
     */
    private static Response rServerError() {
        return Response.serverError().type(MediaType.APPLICATION_JSON).entity(JSON_SERVER_ERROR).build();
    }
    
    /**
     * Возвращает HTTP-ответ, соответствующий попытке добавить проект с таким
     * именем, какое уже существует.
     * 
     * @return Объект, содержащий код статуса HTTP (409) и JSON-объект
     * с словесным указанием на ошибку дублирования
     */
    private static Response rDuplicateError() {
        return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON).entity(JSON_DUPLICATE_ERROR).build();
    }
    
    /**
     * Обрабатывает GET-запросы к корневому URI API. На данный момент еще
     * не реализован в соответствии со спецификацией.
     * 
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get() {
        return ODBService.testFunction();
    }
    
    /**
     * Обрабатывает PUT-запросы вида /{название проекта}.
     * 
     * Пытается создать новый проект с заданным в URI названием, и если
     * не существует другого с таким именем и не произошло ошибки, то передает
     * через {@link WorkerLauncher} пользовательский запрос на обработку в другой
     * поток, а сам возвращает HTTP 202 (с тем, чтобы не блокировать пользователя
     * до завершения длительной обработки).
     * 
     * @param project Название проекта (передается в URI)
     * @param message Содержимое запроса, должно представлять собой JSON
     * с определенными полями
     * @return HTTP-ответ: код статуса и краткая информация в JSON-виде
     */
    @PUT
    @Path("/{project}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("project") String project, String message) {
        ODBService.Result alreadyExists;
        
        alreadyExists = ODBService.projectExists(project);
        
        if (alreadyExists == ODBService.Result.ODB_FALSE) {
            ODBService.Result res;
            
            res = ODBService.addProject(project);
            if (res != ODBService.Result.ODB_OK) {
                return rServerError();
            }
            
            WorkerLauncher.addTask(new WorkerTask(context, project, message));
            return rAccepted();
        } else if (alreadyExists == ODBService.Result.ODB_TRUE) {
            return rDuplicateError();
        } else if (alreadyExists == ODBService.Result.ODB_DB_ERROR) {
            return rServerError();
        } else {
            Logger.getLogger(REST.class.getName()).log(Level.SEVERE, "Unexpected return value: {0}", alreadyExists);
            return rServerError();
        }
    }
}