package org.mwdb.task.action;

import org.mwdb.KNode;
import org.mwdb.KTask;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionSelect implements KTaskAction {

    private final KTask.KTaskSelect _filter;

    public ActionSelect(final KTask.KTaskSelect p_filter) {
        this._filter = p_filter;
    }

    @Override
    public final void eval(final KTaskContext context) {
        final Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            if (previousResult instanceof KNode[]) {
                context.setResult(filterNodeArray((KNode[]) previousResult));
            } else if (previousResult instanceof Object[]) {
                context.setResult(filterArray((Object[]) previousResult));
            }
        }
        context.next();
    }

    private Object[] filterArray(Object[] current) {
        Object[] filteredResult = new Object[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof KNode[]) {
                KNode[] filtered = filterNodeArray((KNode[]) current[i]);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            } else if (current[i] instanceof Object[]) {
                Object[] filtered = filterArray((Object[]) current[i]);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            }
            if (current[i] != null && current[i] instanceof KNode) {
                if (_filter.select((KNode) current[i])) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                }
            }
        }
        return filteredResult;
    }

    private KNode[] filterNodeArray(KNode[] current) {
        KNode[] filtered = new KNode[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null && _filter.select(current[i])) {
                filtered[cursor] = current[i];
                cursor++;
            }
        }
        if (cursor != current.length) {
            KNode[] filtered_2 = new KNode[cursor];
            System.arraycopy(filtered, 0, filtered_2, 0, cursor);
            return filtered_2;
        } else {
            return filtered;
        }
    }


}
