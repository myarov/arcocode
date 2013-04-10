package ru.sp.dystopia.arcocode.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
    private final static String JSON_NOT_FOUND_ERROR = "{\"error\": \"not found\"}";
    
    /**
     * Возвращает HTTP-ответ, соответствующий успешно возвращаемой по запросу
     * информации.
     * 
     * @param jsonData Информация, которую надо отправить клиенту
     * @return Объект, содержащий код статуса HTTP (200) и JSON-объект (аргумент)
     */
    private static Response rData(String jsonData) {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(jsonData).build();
    }
    
    /**
     * Возвращает HTTP-ответ, соответствующий успешно выполненному действию.
     * 
     * @return Объект, содержащий код статуса HTTP (200) и JSON-объект (пустой)
     */
    private static Response rOk() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(JSON_EMPTY).build();
    }
    
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
     * Возвращает HTTP-ответ, соответствующий не найденному проекту.
     * 
     * @return Объект, содержащий код статуса HTTP (404) и JSON-объект
     * с словесным указанием на ошибку не найденного объекта
     */
    private static Response rNotFoundError() {
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(JSON_NOT_FOUND_ERROR).build();
    }
    
    /**
     * Обрабатывает GET-запросы к корню API («GET /»). 
     * 
     * @return HTTP-ответ: код статуса и JSON-объект со списком проектов
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        String jsonData;
        
        jsonData = ODBService.getAllProjects();
        
        if (jsonData != null) {
            return rData(jsonData);
        } else {
            return rServerError();
        }
    }
    
    /**
     * Обрабатывает GET-запросы вида /{название проекта}. 
     * 
     * @param project Идентификатор проекта
     * @return HTTP-ответ: код статуса и сведения о проекте в виде JSON
     */
    @GET
    @Path("/{project}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("project") String project) {
        String jsonData;
        ODBService.Result exists;
        
        exists = ODBService.projectExists(project);
        
        if (exists == ODBService.Result.ODB_TRUE) {
            jsonData = ODBService.getProjectData(project);
            
            if (jsonData != null) {
                return rData(jsonData);
            } else {
                return rServerError();
            }
        } else if (exists == ODBService.Result.ODB_FALSE) {
            return rNotFoundError();
        } else if (exists == ODBService.Result.ODB_DB_ERROR) {
            return rServerError();
        } else {
            Logger.getLogger(REST.class.getName()).log(Level.SEVERE, "Unexpected return value: {0}", exists);
            return rServerError();
        }
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
            
            WorkerLauncher.addTask(project, new WorkerTask(context, project, message));
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
    
    /**
     * Обрабатывает DELETE-запросы вида /{название проекта}.
     * 
     * @param project Название удаляемого проекта
     * @return HTTP-ответ: код статуса и краткая информация в JSON-виде
     */
    @DELETE
    @Path("/{project}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("project") String project) {
        ODBService.Result res;
        
        res = ODBService.deleteProject(project);
        
        if (res == ODBService.Result.ODB_OK) {
            return rOk();
        } else if (res == ODBService.Result.ODB_DB_ERROR) {
            return rServerError();
        } else {
            Logger.getLogger(REST.class.getName()).log(Level.SEVERE, "Unexpected return value: {0}", res);
            return rServerError();
        }
    }
}