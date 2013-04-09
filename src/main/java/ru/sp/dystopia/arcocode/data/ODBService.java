package ru.sp.dystopia.arcocode.data;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, определяющий методы для работы с базой данных OrientDB. Содержит
 * только статические методы (при этом — соединения не открываются и закрываются
 * заново в каждой функции, а берутся из пула ODatabaseDocumentPool.global()).
 * 
 * @author Maxim Yarov
 */
public class ODBService {
    /**
     * Возможные результаты выполнения функций, манипулирующих базой данных.
     */
    public enum Result {
        /**
         * Функция завершилась успешно — аналог return в функции void f()
         */
        ODB_OK,
        /**
         * Функция вернула истину
         */
        ODB_TRUE,
        /**
         * Функция вернула ложь
         */
        ODB_FALSE,
        /**
         * Произошла ошибка при работе с базой данных
         */
        ODB_DB_ERROR,
        /**
         * Была произведена попытка модифицировать некий проект, но объект
         * класса «проект» с заданным именем не нашелся
         */
        ODB_NOT_FOUND};
    
    private final static String ODB_PROJECT_CLASS = "Project";
    private final static String ODB_METRICS_CLASS = "Metrics";
    private final static String ODB_ID_INDEX = "nameIdx";
    
    private final static String ODB_ID_FIELD = "name";
    private final static String ODB_ADDED_ON_FIELD = "addedOn";
    private final static String ODB_STATUS_FIELD = "status";
    private final static String ODB_STAGE_FIELD = "stage";
    private final static String ODB_REASON_FIELD = "reason";
    private final static String ODB_URI_FIELD = "uri";
    private final static String ODB_REVISION_FIELD = "revision";
    private final static String ODB_METRICS_FIELD = "metrics";
    
    private final static String ODB_WORKING_STATUS = "processing";
    private final static String ODB_ERROR_STATUS = "failed";
    private final static String ODB_DONE_STATUS = "done";
    
    private final static String ODB_PARSE_STAGE = "preparing";
    private final static String ODB_COLLECT_STAGE = "downloading";
    private final static String ODB_EXAMINE_STAGE = "parsing";
    private final static String ODB_DONE_STAGE = "done";
    
    private final static String ODB_MALFORMED_REASON = "malformed";
    private final static String ODB_INTERNAL_REASON = "internal";
    private final static String ODB_COLLECT_REASON = "download problem";
    
    /**
     * Получает глобальный путь (от корня) к директории, где хранится база
     * данных.
     * 
     * @return Объект java.io.File, указывающий на директорию БД
     */
    private static File getDbFile() {
        File file = new File(System.getProperty("com.sun.aas.instanceRoot"));
        file = new File(file, "lib");
        file = new File(file, "databases");
        file = new File(file, "arcocode");
        return file;
    }
    
    /**
     * Получает объект документной базы данных, создавая необходимые файлы
     * при их отсутствии.
     * 
     * @return Экземпляр базы данных
     */
    private static ODatabaseDocumentTx getDbObj() {
        File dbFile = getDbFile();
        
        createDbIfNeeded();
        
        return ODatabaseDocumentPool.global().acquire("local:" + dbFile, "admin", "admin");
    }
    
    /**
     * Проверяет существование директории с базой данных; если она не найдена —
     * создает БД.
     * 
     * Кроме непосредственно создания базы — инициализирует минималистичную
     * схему (один класс — проект — с одним обязательным полем, именем, и
     * индексом по этому полю).
     */
    public static void createDbIfNeeded() {
        File dbFile = getDbFile();
        if (dbFile.exists()) {
            return;
        }
        
        ODatabaseDocumentTx db = new ODatabaseDocumentTx("local:" + dbFile).create();
        
        OClass projectClass = db.getMetadata().getSchema().createClass(ODB_PROJECT_CLASS);
        projectClass.createProperty(ODB_ID_FIELD, OType.STRING).setMandatory(true).setNotNull(true);
        projectClass.createIndex(ODB_ID_INDEX, OClass.INDEX_TYPE.UNIQUE, ODB_ID_FIELD);
        
        db.close();
    }
    
    /**
     * Совершает действие над базой данных, заданное как метод передаваемого
     * объекта.
     * 
     * Функция оборачивает действие получением соединения с базой данных,
     * блоками catch и finally для обработки ошибки и окончания работы
     * с соединением соответственно.
     * 
     * @param action Объект, метод act(ODatabaseDocumentTx) которого определяет
     * совершаемое действие
     * @return Результат выполнения действия или указание на то, что произошла
     * ошибка, если она произошла
     */
    private static Result performAction(DBActionInterface action) {
        ODatabaseDocumentTx db = null;
        Result res;
        
        try {
            db = getDbObj();
            res = action.act(db);
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return Result.ODB_DB_ERROR;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return res;
    }
    
    /**
     * Находит в БД проект с заданными именем и модифицирует его при помощи
     * метода передаваемого объекта.
     * 
     * Кроме вызова модифицирующего метода — функция служит для получения
     * соединения с базой из пула, поиском проекта через индекс, обработку
     * ошибок и возвращения соединения в пул в блоке finally.
     * 
     * @param name Название модифицируемого проекта
     * @param modifier Объект, метод modify(ODocument) которого определяет
     * требующиеся действия по модификации
     * @return Результат модифицирующего метода или произошедшую в обертке
     * ошибку (ошибка более приоритетна по понятным причинам)
     */
    private static Result findAndModify(String name, DocumentModificationInterface modifier) {
        ODatabaseDocumentTx db = null;
        OIndex index;
        OIdentifiable match;
        ODocument doc;
        
        try {
            db = getDbObj();
            index = db.getMetadata().getIndexManager().getIndex(ODB_ID_INDEX);
            match = (OIdentifiable)index.get(name);
            if (match != null) {
                doc = (ODocument)match.getRecord();
                modifier.modify(doc);
                doc.save();
            } else {
                Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, "Could not find project {0}", name);
                return Result.ODB_NOT_FOUND;
            }
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return Result.ODB_DB_ERROR;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return Result.ODB_OK;
    }
    
    
    /**
     * Проверяет, содержится ли в БД проект с заданным именем.
     * 
     * @param name Название проекта
     * @return В случае отсутствия проблем при работе — Result.ODB_TRUE/FALSE.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectExists(final String name) {
        return performAction(new DBActionInterface() {
            @Override
            public ODBService.Result act(ODatabaseDocumentTx db) {
                OIndex index;
                boolean found;

                index = db.getMetadata().getIndexManager().getIndex(ODBService.ODB_ID_INDEX);
                found = index.contains(name);

                return (found ? ODBService.Result.ODB_TRUE : ODBService.Result.ODB_FALSE);
            }
        });
    }
    
    /**
     * Добавляет в базу новый проект с переданным названием.
     * 
     * Кроме названия, в новодобавленную запись заносится время создания и
     * изначальное значение о состоянии и стадии обработки.
     * 
     * @param name Название добавляемого проекта
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result addProject(final String name) {
        return performAction(new DBActionInterface() {
            @Override
            public ODBService.Result act(ODatabaseDocumentTx db) {
                ODocument doc;
                DateFormat dateFrmt;

                doc = new ODocument(ODBService.ODB_PROJECT_CLASS);

                dateFrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                doc.field(ODBService.ODB_ID_FIELD, name);
                doc.field(ODBService.ODB_ADDED_ON_FIELD, dateFrmt.format(new Date()));
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_WORKING_STATUS);
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_PARSE_STAGE);

                doc.save();

                return ODBService.Result.ODB_OK;
            }
        });
    }
    
    /**
     * Обновляет информацию о проекте в базе, указывая, что закончился разбор
     * пользовательского запроса.
     * 
     * Изменяется стадия обработки проекта и добавляется поле URI.
     * 
     * @param name Имя проекта
     * @param uri URI репозитория, полученный в результате разбора
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectParseDone(String name, final String uri) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_COLLECT_STAGE);
                doc.field(ODBService.ODB_URI_FIELD, uri);
            }
        });
    }
    
    /**
     * Обновляет запись проекта, указывая, что закончилась выгрузка исходного
     * кода из удаленного репозитория.
     * 
     * Устанавливает новую стадию обработки проекта и поле ревизии.
     * 
     * @param name Имя проекта
     * @param revision Строка с ревизией выгруженного исходного кода
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectCollectDone(String name, final String revision) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_EXAMINE_STAGE);
                doc.field(ODBService.ODB_REVISION_FIELD, revision);
            }
        });
    }
    
    /**
     * Обновляет информацию проекта, для которого завершилась обработка.
     * 
     * Задается новый статус и новая стадия, в базе создается запись с метриками
     * проекта (отдельный класс от класса записи проекта) и связь вносится
     * в соответствующее поле проекта.
     * 
     * @param name Название проекта
     * @param metricsJSON JSON-представление записи с метриками
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectComplete(String name, final String metricsJSON) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify (ODocument doc) {
                ODocument metrics = new ODocument(ODBService.ODB_METRICS_CLASS);
                metrics.fromJSON(metricsJSON);

                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_DONE_STATUS);
                doc.field(ODBService.ODB_STAGE_FIELD, ODBService.ODB_DONE_STAGE);
                doc.field(ODBService.ODB_METRICS_FIELD, metrics);
            }
        });
    }
    
    /**
     * Устанавливает состояние проекта в положение «произошла ошибка — запрос
     * некорректен».
     * 
     * @param name Имя проекта
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectErrorMalformed(String name) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_ERROR_STATUS);
                doc.field(ODBService.ODB_REASON_FIELD, ODBService.ODB_MALFORMED_REASON);
            }
        });
    }
    
    /**
     * Устанавливает состояние проекта в положение «произошла ошибка — 
     * внутренняя ошибка сервера».
     * 
     * @param name Имя проекта
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectErrorInternal(String name) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_ERROR_STATUS);
                doc.field(ODBService.ODB_REASON_FIELD, ODBService.ODB_INTERNAL_REASON);
            }
        });
    }
    
    /**
     * Устанавливает состояние проекта в положение «произошла ошибка — выгрузка
     * не удалась».
     * 
     * @param name Имя проекта
     * @return В случае отсутствия проблем при работе — Result.ODB_OK.
     * Иначе — один из результатов-ошибок.
     */
    public static Result projectErrorCollectFailed(String name) {
        return findAndModify(name, new DocumentModificationInterface() {
            @Override
            public void modify(ODocument doc) {
                doc.field(ODBService.ODB_STATUS_FIELD, ODBService.ODB_ERROR_STATUS);
                doc.field(ODBService.ODB_REASON_FIELD, ODBService.ODB_COLLECT_REASON);
            }
        });
    }
    
    /**
     * Функция получения данных о проекте по его названию.
     * 
     * @return Строка с данными проекта в форме JSON; или null, если произошла
     * ошибка или проект не найден.
     */
    public static String getProjectData(String name) {
        ODatabaseDocumentTx db = null;
        OIndex index;
        OIdentifiable match;
        ODocument doc, metrics;
        String res = "";
        
        try {
            db = getDbObj();
            index = db.getMetadata().getIndexManager().getIndex(ODB_ID_INDEX);
            match = (OIdentifiable)index.get(name);
            if (match != null) {
                doc = (ODocument)match.getRecord();
                
                // Шаманская строка формата — про такой параметр в javadoc
                // от библиотеки сказано не было, нашлось случайно:
                // http://groups.google.com/group/orient-database/browse_thread/thread/67583e918b2627f9
                // Смысл такого формата заключается в том, что вложенный
                // документ класса metrics (или любого другого — *) включается
                // в результирующую строку. В противном случае — получили бы
                // голый идентификатор вида «#9:18» вместо самих метрик.
                res = doc.toJSON("fetchPlan:*:1");
            } else {
                Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, "Could not find project {0}", name);
                return null;
            }
        } catch (OException ex) {
            Logger.getLogger(ODBService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return res;
    }
}

/**
 * Интерфейс объекта, производящего действие над базой данных.
 */
interface DBActionInterface {
    /**
     * Метод, который будет вызываться из функции-обертки.
     * 
     * @param db Соединение с интересующей базой
     * @return Результат выполнения действия
     */
    public ODBService.Result act(ODatabaseDocumentTx db);
}

/**
 * Интерфейс объекта, модифицирующего некий документ в базе.
 */
interface DocumentModificationInterface {
    /**
     * Метод, который будет вызываться из функции-обертки.
     * 
     * @param doc Модифицируемый объект
     */
    public void modify(ODocument doc);
}