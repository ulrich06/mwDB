package org.mwg.importer;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.io.FileNotFoundException;

class ActionReadLines implements TaskAction {

    private final String _path;

    ActionReadLines(String p_path) {
        this._path = p_path;
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        context.cleanObj(previous);
        Object res = null;
        try {
            res = new IterableLines(_path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        context.setUnsafeResult(res);
    }

}
