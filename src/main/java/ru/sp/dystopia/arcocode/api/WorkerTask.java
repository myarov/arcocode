package ru.sp.dystopia.arcocode.api;

import ru.sp.dystopia.arcocode.data.ODBService;

/**
 * Класс, непосредственно осуществляющий обработку репозитория.
 * 
 * @author Maxim Yarov
 */
public class WorkerTask extends Thread {
    public WorkerTask() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void run() {
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
