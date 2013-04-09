package ru.sp.dystopia.arcocode.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Служебный класс для запуска задачи обработки репозитория.
 * 
 * Требуется главным образом для того, чтобы при завершении работы сервлета
 * не оставалось подвисших потоков. Кроме того, из-за использования
 * Executors.newSingleThreadExecutor() потоки обработки запускаются поочередно.
 * В будущем это поведение, скорее всего, должно быть скорректировано в сторону
 * увеличения числа одновременно работающих потоков.
 * 
 * @author Maxim Yarov
 */
public class WorkerLauncher implements ServletContextListener {
    private static ExecutorService executor;
    
    /**
     * Запуск некоего Runnable в работу.
     * 
     * @param task Запускаемый объект
     */
    public static void addTask(Callable task) {
        // Не проверяется, что executor != null, потому, что нулевой executor
        // означает, что что-то капитально пошло не так, и лучшее, что может
        // сделать программа — упасть с исключением прямо сейчас (и с большой
        // высоты).
        executor.submit(task);
    }
    
    private int getNThreadsProperty() {
        InputStream stream = null;
        int res = 0;
        
        try {
            URL url = this.getClass().getResource("/arcocode.properties");
            Properties prop = new Properties();
            
            stream = url.openStream();
            prop.load(stream);
            
            res = Integer.parseInt(prop.getProperty("launcher.nThreads"));
        } catch (IOException ex) {
            Logger.getLogger(WorkerLauncher.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(WorkerLauncher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return res;
    }
    
    /**
     * Создает новый ExecutorService при инициализации сервлета.
     * 
     * @param event Аргумент, требующийся интерфейсу
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        
        executor = Executors.newFixedThreadPool(getNThreadsProperty());
    }

    /**
     * Останавливает ExecutorService при уничтожении сервлета.
     * 
     * @param event Аргумент, требующийся интерфейсу
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // Разница между executor.shutdown() и executor.shutdownNow() — в том,
        // что последний завершает и ожидающие в очереди потоки тоже,
        // а не пытается их выполнить.
        executor.shutdownNow();
    }
}