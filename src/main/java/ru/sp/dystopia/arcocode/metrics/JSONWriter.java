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
    
    public JSONWriter() {
        root = new JSONObject();
    }
    
    @Override
    public void reset() {}
    
    public String getJSON() {
        return root.toString();
    }

    @Override
    public void addPackage(String strPackage) {
        JSONObject pkg = new JSONObject();
        pkg.put(JW_PNAME_KEY, strPackage);
        
        root.accumulate(JW_PACKAGES_KEY, pkg);
    }

    @Override
    public void addConnection(String importerPackage, String importeePackage) {
        
    }

    @Override
    public void addClass(String strClass, String strPackage, String strParent) {
        
    }

    @Override
    public void addMethod(String strMethod, String strClass, String strPackage) {
        
    }

    @Override
    public void setMethodSize(int size, String strMethod, String strClass, String strPackage) {
        
    }

    @Override
    public void setMethodComplexity(int complexity, String strMethod, String strClass, String strPackage) {
        
    }
    
}
