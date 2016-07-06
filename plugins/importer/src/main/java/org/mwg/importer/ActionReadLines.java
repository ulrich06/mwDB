package org.mwg.importer;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.io.FileNotFoundException;

class ActionReadLines implements TaskAction {

    private final String _pathOrTemplate;

    ActionReadLines(String p_pathOrTemplate) {
        this._pathOrTemplate = p_pathOrTemplate;
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        context.cleanObj(previous);
        Object res = null;

        String path = context.template(_pathOrTemplate);
        try {
            res = new IterableLines(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        context.setUnsafeResult(res);
    }

}
