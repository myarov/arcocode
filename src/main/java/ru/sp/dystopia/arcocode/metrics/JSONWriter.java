package ru.sp.dystopia.arcocode.metrics;

import net.sf.json.JSONObject;

/**
 * Класс, представляющий информацию о метриках в виде JSON.
 * 
 * @author Maxim Yarov
 */
public class JSONWriter implements MetricsWriter {
    private final static String JW_PACKAGES_KEY = "packages";
    private final static String JW_PNAME_KEY = "name";
    
    JSONObject root;
    /**
     * todo
     */
    public JSONWriter() {
        root = new JSONObject();
    }
    /**
     * todo
     */
    @Override
    public void reset() {}
    /**
     * todo
     * @return 
     */
    public String getJSON() {
        return root.toString();
    }
    /**
     * Добавление пакета в структуру результата.
     * @param strPackage - имя пакета.
     */
    @Override
    public void addPackage(String strPackage) {
        JSONObject pkg = new JSONObject();
        pkg.put(JW_PNAME_KEY, strPackage);
        
        root.accumulate(JW_PACKAGES_KEY, pkg);
    }
    /**
     * Добавления связей между пакетами.
     * @param importerPackage - "что".
     * @param importeePackage - "кого".
     */
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
        
    }
    /**
     * Добавление класса в некоторый пакет.
     * @param strClass - класс.
     * @param strPackage - пакет.
     * @param strParent - "родитель".
     */
    @Override
    public void addClass(String strClass, String strPackage, String strParent) {
        
    }
    /**
     * Добавление метода в класс.
     * @param strMethod - метод.
     * @param strClass - класс.
     * @param strPackage - пакет.
     */
    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
        
    }
    /**
     * Установка размера (количество выражений) метода.
     * @param size - размер.
     * @param strMethod - метод.
     * @param strClass - класс.
     * @param strPackage - пакет.
     */
    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
        
    }
    /**
     * Установка сложности метода (количество ветвлений).
     * @param complexity - сложность метода.
     * @param strMethod - метод.
     * @param strClass - класс.
     * @param strPackage - пакет.
     */
    @Override
    public void setMethodComplexity(int complexity, String strMethod, String strClass, String strPackage) {
        
    }
    
}
