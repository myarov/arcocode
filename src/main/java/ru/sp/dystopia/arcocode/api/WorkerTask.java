package ru.sp.dystopia.arcocode.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import ru.sp.dystopia.arcocode.data.ODBService;

/**
 * Класс, непосредственно осуществляющий обработку репозитория.
 * 
 * @author Maxim Yarov
 */
public class WorkerTask extends Thread {
    private String project;
    private String jsonData;
    
    private String uri;
    private String user;
    private String pass;
    
    private final static String REQUEST_URI_FIELD = "uri";
    private final static String REQUEST_USER_FIELD = "login";
    private final static String REQUEST_PASS_FIELD = "password";

    public WorkerTask(String project, String jsonData) {
        this.project = project;
        this.jsonData = jsonData;
    }
    
    @Override
    public void run() {
        ODBService.Result res;
        res = ODBService.addProject(project);
        
        parse();
        if (isInterrupted()) { return; }
        
        collect();
        if (isInterrupted()) { return; }
        
        examine();
        if (isInterrupted()) { return; }
    }
    
    private void parse() {
        JSON initial;
        JSONObject data;
        
        try {
            initial = JSONSerializer.toJSON(jsonData);
        } catch (JSONException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.SEVERE, null, ex);
            ODBService.projectErrorMalformed(project);
            return;
        }
        
        if (initial == null || initial.isEmpty() || initial.isArray()) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.SEVERE, "Request is empty or an array");
            ODBService.projectErrorMalformed(project);
            return;
        }
        
        data = (JSONObject)initial;
        
        try {
            uri = data.getString(REQUEST_URI_FIELD);
        } catch (JSONException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.SEVERE, null, ex);
            ODBService.projectErrorMalformed(project);
            return;
        }
        
        try {
            user = data.getString(REQUEST_USER_FIELD);
        } catch (JSONException ex) {
            user = null;
        }
        
        try {
            pass = data.getString(REQUEST_PASS_FIELD);
        } catch (JSONException ex) {
            pass = null;
        }
        
        ODBService.projectParseDone(project, uri);
    }
    
    private void collect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void examine() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
