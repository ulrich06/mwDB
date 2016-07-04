package org.mwg.importer;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.io.FileNotFoundException;

class ActionReadLines implements TaskAction {

    public static final String READLINE_NAME = "readLines";

    private final String _path;

    protected ActionReadLines(String p_path) {
        this._path = p_path;
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        context.cleanObj(previous);
        Object res = null;
        try {
            res = new IterableFile(_path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        context.setResult(res);
    }

}
