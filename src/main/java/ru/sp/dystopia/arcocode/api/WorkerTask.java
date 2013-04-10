package ru.sp.dystopia.arcocode.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import ru.sp.dystopia.arcocode.repoman.HgRepoMan;
import ru.sp.dystopia.arcocode.repoman.RepoMan;
import ru.sp.dystopia.arcocode.repoman.SVNRepoMan;

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
     * Ошибка, с которой завершилась обработка
     */
    public enum WorkerError {
        /**
         * Обработка завершилась без ошибки
         */
        NO_ERROR,
        /**
         * Внутренняя ошибка
         */
        INTERNAL_ERROR,
        /**
         * Неправильно составленный запрос
         */
        MALFORMED_REQUEST,
        /**
         * Ошибка при выгрузке удаленного репозитория
         */
        COLLECT_FAILED
    };
    
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
     * Менеджер репозитория, который будет осуществлять выгрузку кода.
     */
    RepoMan repoMan;
    
    private final String GIT_TYPE = "git";
    private final String MERCURIAL_TYPE = "hg";
    private final String SVN_TYPE = "svn";
    
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
     * @return Ошибка или ее отсутствие для всей обработки в целом
     */
    @Override
    public WorkerError call() {
        WorkerError res;
        
        res = parse();
        if (res != WorkerError.NO_ERROR) { return res;}
        if (Thread.currentThread().isInterrupted()) { return WorkerError.INTERNAL_ERROR; }
        
        res = collect();
        if (res != WorkerError.NO_ERROR) { return res;}
        if (Thread.currentThread().isInterrupted()) { return WorkerError.INTERNAL_ERROR; }
        
        res = examine();
        if (res != WorkerError.NO_ERROR) { return res;}
        if (Thread.currentThread().isInterrupted()) { return WorkerError.INTERNAL_ERROR; }
        
        return WorkerError.NO_ERROR;
    }
    
    /**
     * Осуществляет первую стадию обработки — разбор поступившего запроса
     * для получения данных об удаленном репозитории.
     * 
     * После самого разбора вызывается функция, которая обновляет состояние
     * проекта в БД.
     * 
     * @return Ошибка стадии или ее отсутствие
     */
    private WorkerError parse() {
        Gson gson;
        ODBService.Result res;
        
        gson = new Gson();
        
        try {
            req = gson.fromJson(jsonData, RequestData.class);
        } catch (JsonSyntaxException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Was not able to parse JSON: {0}", jsonData);
            return WorkerError.MALFORMED_REQUEST;
        }
        
        if (req.uri == null) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Missing URI field in request");
            return WorkerError.MALFORMED_REQUEST;
        }
        
        if (req.type == null) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Missing type field in request");
            return WorkerError.MALFORMED_REQUEST;
        } else if (req.type.equals(GIT_TYPE)) {
            repoMan = new GitRepoMan();
        } else if (req.type.equals(MERCURIAL_TYPE)) {
            repoMan = new HgRepoMan();
        } else if (req.type.equals(SVN_TYPE)) {
            repoMan = new SVNRepoMan();
        } else {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Type field invalid in request");
            return WorkerError.MALFORMED_REQUEST;
        }
        
        res = ODBService.projectParseDone(project, req.uri);
        
        return (res == ODBService.Result.ODB_OK ?
                WorkerError.NO_ERROR : WorkerError.INTERNAL_ERROR);
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
     * @return Ошибка стадии или ее отсутствие
     */
    private WorkerError collect() {
        boolean bRes;
        ODBService.Result oRes;
        
        bRes = mkTmpDir();
        if (!bRes) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.SEVERE, "Failed to create directory {0}", tmpDir);
            return WorkerError.INTERNAL_ERROR;
        }
        
        repoMan.setRemoteRepo(req.uri, req.login, req.password);
        repoMan.setLocalDir(tmpDir);
        
        bRes = repoMan.collect();
        if (!bRes) {
            return WorkerError.COLLECT_FAILED;
        }
        
        oRes = ODBService.projectCollectDone(project, repoMan.getLastRevision());
        
        return (oRes == ODBService.Result.ODB_OK ?
                WorkerError.NO_ERROR : WorkerError.INTERNAL_ERROR);
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
     * @return Ошибка стадии или ее отсутствие
     */
    private WorkerError examine() {
        JSONWriter writer;
        boolean bRes;
        ODBService.Result oRes;
        
        if (tmpDir == null) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.SEVERE, "No tmpDir object at examine() stage");
            return WorkerError.INTERNAL_ERROR;
        }
        
        writer = new JSONWriter();
        
        bRes = examineRecursor(tmpDir, writer);
        if (!bRes) {
            return WorkerError.INTERNAL_ERROR;
        }
        
        oRes = ODBService.projectComplete(project, writer.getJSON());
        
        return (oRes == ODBService.Result.ODB_OK ?
                WorkerError.NO_ERROR : WorkerError.INTERNAL_ERROR);
    }
    
    /**
     * Статический метод, который отвечает за вызов нужной функции из набора
     * ODBService.projectError* в зависимости от некоего WorkerError.
     * 
     * @param source Проект, при обработке которого произошла ошибка
     * @param error Вид ошибки
     */
    public static void actOnError(String source, WorkerError error) {
        switch (error) {
            case NO_ERROR:
                break;
            case INTERNAL_ERROR:
                ODBService.projectErrorInternal(source);
                break;
            case MALFORMED_REQUEST:
                ODBService.projectErrorMalformed(source);
                break;
            case COLLECT_FAILED:
                ODBService.projectErrorCollectFailed(source);
                break;
            default:
                ODBService.projectErrorInternal(source);
        }
    }
}

/**
 * Класс, в которой десериализуется пользовательский запрос.
 */
class RequestData {
    String uri;
    String login;
    String password;
    String type;
}