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
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ODBService.createDbIfNeeded();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ODatabaseDocumentPool.global().close();
    }
}
