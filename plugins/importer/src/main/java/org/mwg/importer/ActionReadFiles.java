package org.mwg.importer;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionReadFiles implements TaskAction{
    public static final String READFILES_NAME = "readFiles";

    private final String _pathOrTemplate;

    protected ActionReadFiles(String _pathOrTemplate) {
        this._pathOrTemplate = _pathOrTemplate;
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        context.cleanObj(previous);
        Object res;

        String path = context.template(_pathOrTemplate);
        if(path == null) {
            throw new RuntimeException("Variable " + _pathOrTemplate + " does not exist in the context");
        }
        res = new IterableFiles(path);

        context.setUnsafeResult(res);


    }
}
