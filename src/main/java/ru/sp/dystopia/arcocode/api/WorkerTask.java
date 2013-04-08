package ru.sp.dystopia.arcocode.api;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
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
public class WorkerTask extends Thread {
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
     * Результат разбора запроса — URI репозитория.
     */
    private String uri;
    /**
     * Результат разбора запроса — имя для доступа к репозиторию.
     */
    private String user;
    /**
     * Результат разбора запроса — пароль для доступа к репозиторию.
     */
    private String pass;
    
    /**
     * Директория для временных файлов, предоставляемая сервером приложений.
     */
    File tmpDir;
    
    private final static String REQUEST_URI_FIELD = "uri";
    private final static String REQUEST_USER_FIELD = "login";
    private final static String REQUEST_PASS_FIELD = "password";

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
    }
    
    /**
     * Производит непосредственно обработку проекта — в три указанных выше шага.
     * 
     * После каждого шага проверяется, не был ли установлен флаг прерывания
     * методом ExecutorService.shutdownNow().
     */
    @Override
    public void run() {
        boolean res;
        
        res = parse();
        if (isInterrupted() || !res) { return; }
        
        res = collect();
        if (isInterrupted() || !res) { return; }
        
        res = examine();
        if (isInterrupted() || !res) { return; }
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
        JSON initial;
        JSONObject data;
        ODBService.Result res;
        
        try {
            initial = JSONSerializer.toJSON(jsonData);
        } catch (JSONException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, null, ex);
            ODBService.projectErrorMalformed(project);
            return false;
        }
        
        if (initial == null || initial.isEmpty() || initial.isArray()) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Request is empty or an array");
            ODBService.projectErrorMalformed(project);
            return false;
        }
        
        data = (JSONObject)initial;
        
        try {
            uri = data.getString(REQUEST_URI_FIELD);
        } catch (JSONException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, null, ex);
            ODBService.projectErrorMalformed(project);
            return false;
        }
        
        try {
            user = data.getString(REQUEST_USER_FIELD);
        } catch (JSONException ex) {
            user = null;
        }
        
        try {
            pass = data.getString(REQUEST_PASS_FIELD);
        } catch (JSONException ex) {
            pass = null;
        }
        
        res = ODBService.projectParseDone(project, uri);
        
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
        
        if (project != null) {
            dirName = project.replaceAll("[^a-zA-Z0-9-_]", "") + String.valueOf(this.getId());
        } else {
            dirName = String.valueOf(this.getId());
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
        
        repoman.setRemoteRepo(uri, user, pass);
        repoman.setLocalDir(tmpDir);
        
        bRes = repoman.collect();
        if (!bRes) {
            ODBService.projectErrorCollectFailed(project);
            return false;
        }
        
        oRes = ODBService.projectCollectDone(project, repoman.getLastRevision());
        
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
        
        if (isInterrupted()) {
            return false;
        }
        
        if (children != null) {
            for (File child: children) {
                if (child.isHidden()) {
                    continue;
                }
                
                if (child.isFile() && child.getName().endsWith(".java")) {
                    res = JavaExaminer.examine(child, writer);       
                    if (!res) {
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
        
        return (oRes == ODBService.Result.ODB_OK);
    }
}
