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
        final String path = context.template(_pathOrTemplate);
        context.continueWith(new IterableLines(path));
    }

}
