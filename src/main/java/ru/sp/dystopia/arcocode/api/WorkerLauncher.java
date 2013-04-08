package ru.sp.dystopia.arcocode.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public static void addTask(Runnable task) {
        // Не проверяется, что executor != null, потому, что нулевой executor
        // означает, что что-то капитально пошло не так, и лучшее, что может
        // сделать программа — упасть с исключением прямо сейчас (и с большой
        // высоты).
        executor.submit(task);
    }
    
    /**
     * Создает новый ExecutorService при инициализации сервлета.
     * 
     * @param event Аргумент, требующийся интерфейсу
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        executor = Executors.newSingleThreadExecutor();
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