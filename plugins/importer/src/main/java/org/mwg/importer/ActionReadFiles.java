package org.mwg.importer;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionReadFiles implements TaskAction{

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
        res = new IterableFiles(path);

        context.setUnsafeResult(res);


    }
}
