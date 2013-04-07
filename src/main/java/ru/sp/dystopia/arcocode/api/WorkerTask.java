package ru.sp.dystopia.arcocode.api;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import ru.sp.dystopia.arcocode.data.ODBService;
import ru.sp.dystopia.arcocode.repoman.GitRepoMan;

/**
 * Класс, непосредственно осуществляющий обработку репозитория.
 * 
 * @author Maxim Yarov
 */
public class WorkerTask extends Thread {    
    private ServletContext context;
    
    private String project;
    private String jsonData;
    
    private String uri;
    private String user;
    private String pass;
    
    private final static String REQUEST_URI_FIELD = "uri";
    private final static String REQUEST_USER_FIELD = "login";
    private final static String REQUEST_PASS_FIELD = "password";

    public WorkerTask(ServletContext context, String project, String jsonData) {
        this.context = context;
        this.project = project;
        this.jsonData = jsonData;
    }
    
    @Override
    public void run() {
        boolean bRes;
        ODBService.Result oRes;
        
        oRes = ODBService.addProject(project);
        if (isInterrupted() || !(oRes == ODBService.Result.ODB_OK)) { return; }
        
        bRes = parse();
        if (isInterrupted() || !bRes) { return; }
        
        bRes = collect();
        if (isInterrupted() || !bRes) { return; }
        
        examine();
        if (isInterrupted()) { return; }
    }
    
    private boolean parse() {
        JSON initial;
        JSONObject data;
        ODBService.Result res;
        
        try {
            initial = JSONSerializer.toJSON(jsonData);
        } catch (JSONException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, null, ex);
            ODBService.projectErrorMalformed(project);
            return false;
        }
        
        if (initial == null || initial.isEmpty() || initial.isArray()) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, "Request is empty or an array");
            ODBService.projectErrorMalformed(project);
            return false;
        }
        
        data = (JSONObject)initial;
        
        try {
            uri = data.getString(REQUEST_URI_FIELD);
        } catch (JSONException ex) {
            Logger.getLogger(WorkerTask.class.getName()).log(Level.INFO, null, ex);
            ODBService.projectErrorMalformed(project);
            return false;
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
        
        res = ODBService.projectParseDone(project, uri);
        
        return (res == ODBService.Result.ODB_OK);
    }
    
    private File mkTmpDir() {
        File tmpRoot;
        File tmpProjectDir;
        String dirName;
        boolean res;
        
        tmpRoot = (File) context.getAttribute("javax.servlet.context.tempdir");
        
        if (project != null) {
            dirName = project.replaceAll("[^a-zA-Z0-9-_]", "") + String.valueOf(this.getId());
        } else {
            dirName = String.valueOf(this.getId());
        }
        
        tmpProjectDir = new File(tmpRoot, dirName);
        res = tmpProjectDir.mkdirs();
        
        return (res ? tmpProjectDir : null);
    }
    
    private boolean collect() {
        GitRepoMan repoman = new GitRepoMan();
        File tmpDir = mkTmpDir();
        boolean bRes;
        ODBService.Result oRes;
        
        if (tmpDir == null) {
            ODBService.projectErrorInternal(project);
            return false;
        }
        
        repoman.setRemoteRepo(uri, user, pass);
        repoman.setLocalDir(tmpDir);
        
        bRes = repoman.collect();
        if (!bRes) {
            ODBService.projectErrorCollectFailed(project);
            return false;
        }
        
        oRes = ODBService.projectCollectDone(project, repoman.getLastRevision());
        
        return (oRes == ODBService.Result.ODB_OK);
    }
    
    private void examine() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
