package org.mwg.importer;

import org.mwg.task.Task;
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
        String path = Task.template(_pathOrTemplate,context);
        res = new IterableFiles(path);

        context.setUnsafeResult(res);


    }
}
