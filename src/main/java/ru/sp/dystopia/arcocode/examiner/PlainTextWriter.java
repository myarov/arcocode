package ru.sp.dystopia.arcocode.examiner;

/**
 * Класс, выводящий собранные метрики в stdout.
 * @author Maxim Yarov
 */
public class PlainTextWriter implements MetricsWriter {
    @Override
    public void deinit() {
    }

    @Override
    public void addPackage(String strPackage) {
    }
    
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
        System.out.println("Package " + importerPackage + " imports package " +
                importeePackage);
    }

    @Override
    public void addClass(String strClass, String strPackage, String strParent) {
    }
    
    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
    }

    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
        System.out.println("Package: " + strPackage + "; class: " + strClass +
                "; method: " + strMethod + "(); size: " + size);
    }
    
    @Override
    public void setMethodComplexity(int complexity, String strMethod, String strClass, String strPackage) {
        System.out.println("Package: " + strPackage + "; class: " + strClass +
                "; method: " + strMethod + "(); complexity: " + complexity);
    }
}
