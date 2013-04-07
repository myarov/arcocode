package ru.sp.dystopia.arcocode.api;

import ru.sp.dystopia.arcocode.data.ODBService;

/**
 * Класс, непосредственно осуществляющий обработку репозитория.
 * 
 * @author Maxim Yarov
 */
public class WorkerTask extends Thread {
    private String project;
    private String jsonData;

    public WorkerTask(String project, String jsonData) {
        this.project = project;
        this.jsonData = jsonData;
    }
    
    @Override
    public void run() {
        ODBService.addProject(project);
        
        parse();
        if (isInterrupted()) { return; }
        
        collect();
        if (isInterrupted()) { return; }
        
        examine();
        if (isInterrupted()) { return; }
    }
    
    private void parse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void collect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void examine() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
