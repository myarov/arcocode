/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sp.dystopia.arcocode.examiner;

/**
 * Интерфейс класса, который предпринимает действия с собранными во время
 * обхода синтаксического дерева метриками.
 * @author Maxim Yarov
 */
public interface MetricsWriter {
    public void addPackage(String strPackage);
    public void addClass(String strClass, String strPackage);
    public void addMethod(String strMethod, String strClass, String strPackage);
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage);
}
