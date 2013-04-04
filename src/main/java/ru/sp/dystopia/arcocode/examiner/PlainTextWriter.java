package ru.sp.dystopia.arcocode.examiner;

/**
 * Класс, выводящий отладочную информацию.
 * @author Maxim Yarov
 */
public class PlainTextWriter implements MetricsWriter {
    /**
     * Деинициализация(заглушка).
     */
    @Override
    public void deinit() {
    }
    /**
     * Добавление пакета в структуру результата (заглушка).
     * @param strPackage - имя пакета.
     */
    @Override
    public void addPackage(String strPackage) {
    }
    /**
     * Добавления связей между пакетами.
     * @param importerPackage - "что".
     * @param importeePackage - "кого".
     */
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
        System.out.println("Package " + importerPackage + " imports package " +
                importeePackage);
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
     * @param strMethod
     * @param strClass
     * @param strPackage 
     */
    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
    }
    /**
     * Установка размера (количество выражений) метода.
     * @param size
     * @param strMethod
     * @param strClass
     * @param strPackage 
     */
    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
        System.out.println("Package: " + strPackage + "; class: " + strClass +
                "; method: " + strMethod + "(); size: " + size);
    }
    /**
     * Установка сложности метода (количество ветвлений).
     * @param complexity
     * @param strMethod
     * @param strClass
     * @param strPackage 
     */
    @Override
    public void setMethodComplexity(int complexity, String strMethod, String strClass, String strPackage) {
        System.out.println("Package: " + strPackage + "; class: " + strClass +
                "; method: " + strMethod + "(); complexity: " + complexity);
    }
}
