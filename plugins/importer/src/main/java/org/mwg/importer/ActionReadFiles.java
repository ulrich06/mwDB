package org.mwg.importer;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.io.File;
import java.net.URL;

class ActionReadFiles implements TaskAction {

    private final String _pathOrTemplate;

    ActionReadFiles(String _pathOrTemplate) {
        this._pathOrTemplate = _pathOrTemplate;
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        context.cleanObj(previous);
        Object res;
        String path = context.template(_pathOrTemplate);
        if (path == null) {
            throw new RuntimeException("Variable " + _pathOrTemplate + " does not exist in the context");
        }
        res = getFilesPaths(path);
        context.setUnsafeResult(res);
    }

    private String[] getFilesPaths(String path) {
        String[] filesPath;
        File file = new File(path);
        if(!file.exists()) {
            URL url = this.getClass().getClassLoader().getResource(path);
            if(url == null) {
                throw new RuntimeException("File " + path + " does not exist and it is not present in resources directory.");
            }
            file = new File(url.getPath());
        }

        if(file.isDirectory()) {
            File[] listFiles = file.listFiles();
            filesPath = new String[listFiles.length];
            for(int i=0;i<listFiles.length;i++) {
                filesPath[i] = listFiles[i].getAbsolutePath();
            }
        } else {
            filesPath = new String[] {file.getAbsolutePath()};
        }
        return filesPath;
    }

    @Override
    public String toString() {
        return "readFiles(\'" + _pathOrTemplate + "\')";
    }


}
