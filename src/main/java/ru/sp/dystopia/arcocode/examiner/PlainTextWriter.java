package ru.sp.dystopia.arcocode.examiner;

/**
 * Класс, выводящий собранные метрики в stdout.
 * @author Maxim Yarov
 */
public class PlainTextWriter implements MetricsWriter {
    /**
     * 
     */
    @Override
    public void deinit() {
    }
    /**
     * Добавление пакета.
     * @param strPackage - имя пакета.
     */
    @Override
    public void addPackage(String strPackage) {
    }
    /**
     * 
     * @param importerPackage
     * @param importeePackage 
     */
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
        System.out.println("Package " + importerPackage + " imports package " +
                importeePackage);
    }
    /**
     * 
     * @param strClass
     * @param strPackage
     * @param strParent 
     */
    @Override
    public void addClass(String strClass, String strPackage, String strParent) {
    }
    /**
     * 
     * @param strMethod
     * @param strClass
     * @param strPackage 
     */
    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
    }
    /**
     * 
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
     * 
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
