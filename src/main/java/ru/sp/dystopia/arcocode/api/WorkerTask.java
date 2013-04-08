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
import ru.sp.dystopia.arcocode.examiner.JavaExaminer;
import ru.sp.dystopia.arcocode.metrics.JSONWriter;
import ru.sp.dystopia.arcocode.metrics.MetricsWriter;
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
    
    File tmpDir;
    
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
        boolean res;
        
        res = parse();
        if (isInterrupted() || !res) { return; }
        
        res = collect();
        if (isInterrupted() || !res) { return; }
        
        res = examine();
        if (isInterrupted() || !res) { return; }
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
    
    private boolean mkTmpDir() {
        File tmpRoot;
        String dirName;
        boolean res;
        
        tmpRoot = (File) context.getAttribute("javax.servlet.context.tempdir");
        
        if (project != null) {
            dirName = project.replaceAll("[^a-zA-Z0-9-_]", "") + String.valueOf(this.getId());
        } else {
            dirName = String.valueOf(this.getId());
        }
        
        tmpDir = new File(tmpRoot, dirName);
        res = tmpDir.mkdirs();
        
        return res;
    }
    
    private boolean collect() {
        GitRepoMan repoman = new GitRepoMan();
        boolean bRes;
        ODBService.Result oRes;
        
        bRes = mkTmpDir();
        if (!bRes) {
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
    
    private boolean examineRecursor(File parent, MetricsWriter writer) {
        boolean res;
        File[] children = parent.listFiles();
        
        if (isInterrupted()) {
            return false;
        }
        
        if (children != null) {
            for (File child: children) {
                if (child.isHidden()) {
                    continue;
                }
                
                if (child.isFile() && child.getName().endsWith(".java")) {
                    res = JavaExaminer.examine(child, writer);       
                    if (!res) {
                        return false;
                    }
                }
                
                res = examineRecursor(child, writer);
                if (!res) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean examine() {
        JSONWriter writer;
        boolean bRes;
        ODBService.Result oRes;
        
        if (tmpDir == null) {
            ODBService.projectErrorInternal(project);
            return false;
        }
        
        writer = new JSONWriter();
        
        bRes = examineRecursor(tmpDir, writer);
        if (!bRes) {
            ODBService.projectErrorInternal(project);
            return false;
        }
        
        oRes = ODBService.projectComplete(project, writer.getJSON());
        
        return (oRes == ODBService.Result.ODB_OK);
    }
}
