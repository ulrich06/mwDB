package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelectObject;

/**
 * Created by ludovicmouline on 05/07/16.
 */
public class ActionSelectObject implements TaskAction {

    private final TaskFunctionSelectObject _filter;

    public ActionSelectObject(TaskFunctionSelectObject filterFunction) {
        _filter = filterFunction;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.result();
        if (previousResult != null) {
            if (previousResult instanceof Object[]) {
                context.setResult(filterArray((Object[]) previousResult, context));
            } else {
                if(_filter.select(previousResult)) {
                    context.setResult(previousResult);
                } else {
                    context.cleanObj(previousResult);
                    context.setUnsafeResult(null);
                }
            }
        } else {
            context.setUnsafeResult(null);
        }
    }

    private Object[] filterArray(Object[] current, TaskContext context) {
        Object[] filteredResult = new Object[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof Object[]) {
                Object[] filtered = filterArray((Object[]) current[i],context);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            } else {
                if(_filter.select(current[i])) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                } else {
                    context.cleanObj(current[i]);
                }
            }
        }

        if (cursor == filteredResult.length) {
            return filteredResult;
        } else {
            Object[] shrinkedResult = new Object[cursor];
            System.arraycopy(filteredResult, 0, shrinkedResult, 0, cursor);
            return shrinkedResult;
        }

    }
}
