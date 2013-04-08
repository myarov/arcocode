package ru.sp.dystopia.arcocode.api;

import com.google.gson.Gson;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import ru.sp.dystopia.arcocode.data.ODBService;
import ru.sp.dystopia.arcocode.examiner.JavaExaminer;
import ru.sp.dystopia.arcocode.metrics.JSONWriter;
import ru.sp.dystopia.arcocode.metrics.MetricsWriter;
import ru.sp.dystopia.arcocode.repoman.GitRepoMan;
import ru.sp.dystopia.arcocode.repoman.RepoMan;

/**
 * Класс, непосредственно осуществляющий обработку репозитория.
 * 
 * Обработка происходит в три стадии. «Parse» — разбор запроса пользователя.
 * «Collect» — выгрузка удаленного репозитория во временную директорию.
 * «Examine» — вычисление метрик по выгруженному коду.
 * 
 * @author Maxim Yarov
 */
public class WorkerTask implements Callable {
    /**
     * Контекст сервлета нужен для получения пути к временной директории.
     */
    private ServletContext context;
    
    /**
     * Название проекта.
     */
    private String project;
    /**
     * JSON-запрос пользователя.
     */
    private String jsonData;
    
    /**
     * Результат разбора запроса.
     */
    private RequestData req;
    
    /**
     * Директория для временных файлов, предоставляемая сервером приложений.
     */
    File tmpDir;
    
    /**
     * Конструктор. Инициализирует поля объекта.
     * 
     * @param context Контекст сервлета
     * @param project Имя проекта
     * @param jsonData Запрос пользователя
     */
    public WorkerTask(ServletContext context, String project, String jsonData) {
        this.context = context;
        this.project = project;
        this.jsonData = jsonData;
        this.req = new RequestData();
    }
    
    /**
     * Производит непосредственно обработку проекта — в три указанных выше шага.
     * 
     * После каждого шага проверяется, не был ли установлен флаг прерывания
     * методом ExecutorService.shutdownNow().
     */
    @Override
    public Boolean call() {
        boolean res;
        
        res = parse();
        if (Thread.currentThread().isInterrupted() || !res) { return false; }
        
        res = collect();
        if (Thread.currentThread().isInterrupted() || !res) { return false; }
        
        res = examine();
        if (Thread.currentThread().isInterrupted() || !res) { return false; }
        
        return true;
    }
    
    /**
     * Осуществляет первую стадию обработки — разбор поступившего запроса
     * для получения данных об удаленном репозитории.
     * 
     * После самого разбора вызывается функция, которая обновляет состояние
     * проекта в БД.
     * 
     * @return Успешность выполнения стадии
     */
    private boolean parse() {
        Gson gson;
        ODBService.Result res;
        
        gson = new Gson();
        req = gson.fromJson(jsonData, RequestData.class);
        if (req.uri == null) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Missing URI field in request");
            ODBService.projectErrorMalformed(project);
            return false;
        }
        
        res = ODBService.projectParseDone(project, req.uri);
        
        if (res != ODBService.Result.ODB_OK) {
            ODBService.projectErrorInternal(project);
        }
        
        return (res == ODBService.Result.ODB_OK);
    }
    
    /**
     * Создает временную директорию для выгрузки репозитория.
     * 
     * Родительская директория — полученная от контекста сервлета директория
     * для временных файлов, само название директории формируется
     * из латинских букв и цифр названия проекта и id этого потока.
     * 
     * @return Успешность создания
     */
    private boolean mkTmpDir() {
        File tmpRoot;
        String dirName;
        boolean res;
        
        tmpRoot = (File) context.getAttribute("javax.servlet.context.tempdir");
        
        dirName = String.valueOf(Thread.currentThread().getId());
        if (project != null) {
            dirName = project.replaceAll("[^a-zA-Z0-9-_]", "") + dirName;
        }
        
        tmpDir = new File(tmpRoot, dirName);
        res = tmpDir.mkdirs();
        
        return res;
    }
    
    /**
     * Производит вторую стадию обработки — выгрузку проекта из заданного в URI
     * репозитория внутрь локальной временной директории.
     * 
     * В конце обновляется информация о проекте в базе данных.
     * 
     * @return Успешность выгрузки
     */
    private boolean collect() {
        RepoMan repoman = new GitRepoMan();
        boolean bRes;
        ODBService.Result oRes;
        
        bRes = mkTmpDir();
        if (!bRes) {
            ODBService.projectErrorInternal(project);
            return false;
        }
        
        repoman.setRemoteRepo(req.uri, req.login, req.password);
        repoman.setLocalDir(tmpDir);
        
        bRes = repoman.collect();
        if (!bRes) {
            ODBService.projectErrorCollectFailed(project);
            return false;
        }
        
        oRes = ODBService.projectCollectDone(project, repoman.getLastRevision());
        
        if (oRes != ODBService.Result.ODB_OK) {
            ODBService.projectErrorInternal(project);
        }
        
        return (oRes == ODBService.Result.ODB_OK);
    }
    
    /**
     * Рекурсивная функция, проходящая по всем файлам в директории и ее
     * поддиректориях — оканчивающиеся на «.java» отдаются для изучения
     * в соответствующий метод с целью получить их метрики.
     * 
     * При спуске на каждый следующий уровень проверяется, не был ли установлен
     * флаг завершения потока — это произойдет, если будет вызвана функция
     * ExecutorService.shutdownNow().
     * 
     * @param parent Директория для обхода
     * @param writer Объект, который записывает полученные из файлов метрики
     * @return Ложь: в случае проблем с функцией изучения или включения флага
     * остановки — такое значение передается вверх по уровням рекурсии.
     * Истина: все файлы в директории пройдены, вызывающая функция может
     * спокойно продолжать свою работу.
     */
    private boolean examineRecursor(File parent, MetricsWriter writer) {
        boolean res;
        File[] children = parent.listFiles();
        
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }
        
        if (children != null) {
            for (File child: children) {
                if (child.isHidden()) {
                    continue;
                }
                
                if (child.isFile() && child.getName().endsWith(".java")) {
                    try {
                        res = JavaExaminer.examine(child, writer);       
                        if (!res) {
                            return false;
                        }
                    } catch (RuntimeException ex) {
                        Logger.getLogger(WorkerTask.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                }
                
                res = examineRecursor(child, writer);
                if (!res) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Третий шаг обработки — непосредственно изучение файлов с целью сбора
     * метрик.
     * 
     * Обходит рекурсивно все содержимое директории, в которую было скопировано
     * содержимое репозитория. После этого записывает метрики в базу данных и 
     * устанавливает для проекта ссылку на них и соответствующий статус (done).
     * 
     * @return Успешность выполнения
     */
    private boolean examine() {
        JSONWriter writer;
        boolean bRes;
        ODBService.Result oRes;
        
        if (tmpDir == null) {
            ODBService.projectErrorInternal(project);
            return false;
        }
        
        writer = new JSONWriter();
        
        bRes = examineRecursor(tmpDir, writer);
        if (!bRes) {
            ODBService.projectErrorInternal(project);
            return false;
        }
        
        oRes = ODBService.projectComplete(project, writer.getJSON());
        
        if (oRes != ODBService.Result.ODB_OK) {
            ODBService.projectErrorInternal(project);
        }
        
        return (oRes == ODBService.Result.ODB_OK);
    }
}

class RequestData {
    String uri;
    String login;
    String password;
}