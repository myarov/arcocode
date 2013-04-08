package ru.sp.dystopia.arcocode.metrics;

/**
 * Интерфейс класса, который предпринимает действия с собранными во время
 * обхода синтаксического дерева метриками.
 * @author Maxim Yarov
 */
public interface MetricsWriter {
    public void reset();
    
    public void addPackage(String name);
    public void addConnection(String importerPackage, String importeePackage);
    public void addClass(String name, String toPackage, String parent);
    public void addMethod(String name, String toClass, String toPackage);
    public void setMethodSize(int size, String name, String inClass, String inPackage);
    public void setMethodComplexity(int complexity, String name, String inClass, String inPackage);
}
