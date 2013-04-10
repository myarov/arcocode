package ru.sp.dystopia.arcocode.api;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сторожевой класс: устанавливает в базе status: failed для тех проектов,
 * обработки которых завершились с ошибкой или тихо умерли по исключению.
 * 
 * @author Maxim Yarov
 */
public class WorkerWatchdog implements Runnable {
    /**
     * Таймаут переключения между запущенными обработками для попытки получить
     * их результаты.
     */
    private final int POLL_INTERVAL_MSEC = 1000;
    
    /**
     * Список отслеживаемых обработок.
     */
    private ConcurrentHashMap<String, Future> watchedWorkers;
    
    /**
     * Конструктор, инициализирует ConcurrentHashMap для обрабатываемых проектов.
     */
    public WorkerWatchdog() {
        watchedWorkers = new ConcurrentHashMap<String, Future>();
    }
    
    /**
     * Добавление обработки проекта.
     * 
     * @param project Имя проекта, по которому его при необходимости можно
     * будет найти в базе
     * @param result Future, от которого можно будет получить результат (или
     * исключение) обработки
     */
    public void addTarget(String project, Future result) {
        watchedWorkers.put(project, result);
    }
    
    /**
     * Основная функция, циклически пытающаяся получить результаты от обработок
     * из хранимого списка.
     * 
     * Если результат получен и он — не NO_ERROR, то в базу записывается, что
     * работа над проектом закончилась провалом. Аналогично — если в обработчике
     * произошло неотловленное исключение.
     */
    @Override
    public void run() {
        Iterator it;
        Future curFuture;
        WorkerTask.WorkerError curRes;
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(POLL_INTERVAL_MSEC);
            } catch (InterruptedException ex) {
                Logger.getLogger(WorkerWatchdog.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            it = watchedWorkers.entrySet().iterator();
            
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                
                // Попытка получить результат некоторого обработчика
                try {
                    curFuture = (Future)pair.getValue();
                    curRes = (WorkerTask.WorkerError)curFuture.get(POLL_INTERVAL_MSEC, TimeUnit.MILLISECONDS);
                } catch (ExecutionException ex) {
                    // Во время обработки произошло исключение — основная ситуация,
                    // ради которой создан настоящий класс. Записываем в базу,
                    // что произошла ошибка.
                    Logger.getLogger(WorkerWatchdog.class.getName()).log(Level.INFO, null, ex);
                    curRes = WorkerTask.WorkerError.INTERNAL_ERROR;
                } catch (CancellationException ex) {
                    // Обработчик был отменен. Записываем в базу.
                    Logger.getLogger(WorkerWatchdog.class.getName()).log(Level.INFO, null, ex);
                    curRes = WorkerTask.WorkerError.INTERNAL_ERROR;
                } catch (InterruptedException ex) {
                    // Поступило прерывание для Watchdog. Завершаем работу.
                    Logger.getLogger(WorkerWatchdog.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                } catch (TimeoutException ex) {
                    // За период таймаута обработка не была закончена. Переходим
                    // дальше.
                    continue;
                } 
                
                // Что записать в базу — определяет не сторожевой класс,
                // а статические метод в WorkerTask.
                WorkerTask.actOnError((String)pair.getKey(), curRes);
                
                // Обработка чем-то завершилась, или поймано Execution- или
                // CancellationException. Удаляем из списка.
                it.remove();
            }
        }
    }
    
}
