package ru.sp.dystopia.arcocode.data;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Класс, служащий для инициализации и закрытия базы данных при запуске и
 * остановке сервлета.
 * 
 * @author Maxim Yarov
 */
public class ODBListener implements ServletContextListener {
    /**
     * Производит при необходимости создание базы данных при инициализации
     * сервлета, чтобы не притормаживать первый полученный запрос.
     * 
     * @param sce Аргумент, требующийся интерфейсу
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ODBService.createDbIfNeeded();
    }

    /**
     * Закрывает пул соединений с базой данных при остановке сервлета, закрывая
     * этим сами соединения.
     * 
     * @param sce Аргумент, требующийся интерфейсу
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ODatabaseDocumentPool.global().close();
    }
}
