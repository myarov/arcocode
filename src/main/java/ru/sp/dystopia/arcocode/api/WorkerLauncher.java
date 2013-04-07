package ru.sp.dystopia.arcocode.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Служебный класс для запуска задачи обработки репозитория.
 * 
 * @author Maxim Yarov
 */
public class WorkerLauncher implements ServletContextListener {
    private static ExecutorService executor;
    
    public static void addTask(Runnable task) {
        // No check for "executor != null", as if it is not initialized -
        // something went wrong and the application should crash (and burn)
        executor.submit(task);
    }
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        executor.shutdownNow();
    }
}