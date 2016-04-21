package org.mwdb.task.action;

import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

import java.util.regex.Pattern;

public class ActionWithout implements KTaskAction {

    private final String _name;

    private final Pattern _pattern;

    public ActionWithout(final String p_name, final String p_regex) {
        this._name = p_name;
        this._pattern = Pattern.compile(p_regex);
    }

    @Override
    public final void eval(final KTaskContext context) {
        Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            if (previousResult instanceof KNode[]) {
                context.setResult(filterNodeArray((KNode[]) previousResult));
            } else if (previousResult instanceof Object[]) {
                context.setResult(filterArray((Object[]) previousResult));
            } else if (previousResult instanceof KNode) {
                Object currentName = ((KNode) previousResult).att(_name);
                if (currentName == null || !_pattern.matcher(currentName.toString()).matches()) {
                    context.setResult(previousResult);
                }
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
            } else if (current[i] instanceof KNode) {
                Object currentName = ((KNode) current[i]).att(_name);
                if (currentName == null || !_pattern.matcher(currentName.toString()).matches()) {
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
            if (current[i] != null) {
                Object currentName = current[i].att(_name);
                if (currentName == null || !_pattern.matcher(currentName.toString()).matches()) {
                    filtered[cursor] = current[i];
                    cursor++;
                }
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
