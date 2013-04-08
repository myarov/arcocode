package ru.sp.dystopia.arcocode.metrics;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Класс, представляющий информацию о метриках в виде JSON.
 * 
 * @author Maxim Yarov
 */
public class JSONWriter implements MetricsWriter {
    private MetricsRoot root;
    
    public JSONWriter() {
        root = new MetricsRoot();
    }
    
    @Override
    public void reset() {}
    
    public String getJSON() {
        Gson gson = new Gson();
        return gson.toJson(root);
    }
    
    @Override
    public void addPackage(String name) {
        if (!root.packages.containsKey(name)) {
            root.packages.put(name, new PackageMetrics());
        }
    }
    
    @Override
    public void addConnection(String importerPackage, String importeePackage) {
        PackageMetrics pkg;
        
        pkg = root.packages.get(importerPackage);
        if (pkg == null) {
            throw new IllegalArgumentException("Tried to add a connection from a nonexistent package");
        }
        
        pkg.imports.add(importeePackage);
    }
    
    @Override
    public void addClass(String name, String toPackage, String parent) {
        PackageMetrics pkg;
        
        pkg = root.packages.get(toPackage);
        if (pkg == null) {
            throw new IllegalArgumentException("Tried to add a class to a nonexistent package");
        }
        
        if (!pkg.classes.containsKey(name)) {
            pkg.classes.put(name, new ClassMetrics(parent));
        }
    }
    
    @Override
    public void addMethod(String name, String toClass, String toPackage) {
        PackageMetrics pkg;
        ClassMetrics cls;
        
        pkg = root.packages.get(toPackage);
        if (pkg == null) {
            throw new IllegalArgumentException("Tried to add a method to a class from a nonexistent package");
        }
        
        cls = pkg.classes.get(toClass);
        if (cls == null) {
            throw new IllegalArgumentException("Tried to add a method to a nonexistent class");
        }
        
        if (!cls.methods.containsKey(name)) {
            cls.methods.put(name, new MethodMetrics());
        }
    }
    
    @Override
    public void setMethodSize(int size, String name, String inClass, String inPackage) {
        PackageMetrics pkg;
        ClassMetrics cls;
        MethodMetrics mtd;
        
        pkg = root.packages.get(inPackage);
        if (pkg == null) {
            throw new IllegalArgumentException("Tried to set method size in a nonexistent package");
        }
        
        cls = pkg.classes.get(inClass);
        if (cls == null) {
            throw new IllegalArgumentException("Tried to set method size in a nonexistent class");
        }
        
        mtd = cls.methods.get(name);
        if (cls == null) {
            throw new IllegalArgumentException("Tried to set method size of a nonexistent method");
        }
        
        mtd.size = size;
    }
    
    @Override
    public void setMethodComplexity(int complexity, String name, String inClass, String inPackage) {
        PackageMetrics pkg;
        ClassMetrics cls;
        MethodMetrics mtd;
        
        pkg = root.packages.get(inPackage);
        if (pkg == null) {
            throw new IllegalArgumentException("Tried to set method complexity in a nonexistent package");
        }
        
        cls = pkg.classes.get(inClass);
        if (cls == null) {
            throw new IllegalArgumentException("Tried to set method complexity in a nonexistent class");
        }
        
        mtd = cls.methods.get(name);
        if (cls == null) {
            throw new IllegalArgumentException("Tried to set method complexity of a nonexistent method");
        }
        
        mtd.complexity = complexity;
    }
    
}



class MetricsRoot {
    HashMap<String, PackageMetrics> packages;

    public MetricsRoot() {
        this.packages = new HashMap<String, PackageMetrics>();
    }
}

class PackageMetrics {
    HashSet<String> imports;
    HashMap<String, ClassMetrics> classes;

    public PackageMetrics() {
        imports = new HashSet<String>();
        classes = new HashMap<String, ClassMetrics>();
    }
}

class ClassMetrics {
    private String parent;
    
    HashMap<String, MethodMetrics> methods;

    public ClassMetrics(String parent) {
        this.parent = parent;
        methods = new HashMap<String, MethodMetrics>();
    }
}

class MethodMetrics {
    int size;
    int complexity;
}