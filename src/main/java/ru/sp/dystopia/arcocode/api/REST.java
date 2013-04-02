package ru.sp.dystopia.arcocode.api;

/**
 * Класс, реализующий RESTful API системы.
 * 
 * @author Maxim Yarov
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import ru.sp.dystopia.arcocode.examiner.JavaExaminer;
import ru.sp.dystopia.arcocode.repoman.GitRepoMan;

@Path("/")
public class REST
{
    JavaExaminer examiner;
    GitRepoMan repoMan;
    
    @GET
    @Produces("text/plain")
    public String get() {
        return "REST works";
    }
}