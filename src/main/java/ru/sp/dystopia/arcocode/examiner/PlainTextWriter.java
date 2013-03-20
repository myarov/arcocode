/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sp.dystopia.arcocode.examiner;

/**
 *
 * @author max
 */
public class PlainTextWriter implements MetricsWriter {

    @Override
    public void addPackage(String strPackage) {
    }

    @Override
    public void addClass(String strClass, String strPackage) {
    }

    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
    }

    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
        System.out.println("Package: " + strPackage + "; class: " + strClass +
                "; method: " + strMethod + "(); size: " + size);
    }
    
}
