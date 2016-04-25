package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.regex.Pattern;

class ActionWithout implements TaskAction {

    private final String _name;

    private final Pattern _pattern;

    ActionWithout(final String p_name, final String p_regex) {
        this._name = p_name;
        this._pattern = Pattern.compile(p_regex);
    }

    @Override
    public final void eval(final TaskContext context) {
        Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            if (previousResult instanceof Node[]) {
                context.setResult(filterNodeArray((Node[]) previousResult));
            } else if (previousResult instanceof Object[]) {
                context.setResult(filterArray((Object[]) previousResult));
            } else if (previousResult instanceof Node) {
                Object currentName = ((Node) previousResult).get(_name);
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
            if (current[i] instanceof Node[]) {
                Node[] filtered = filterNodeArray((Node[]) current[i]);
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
            } else if (current[i] instanceof Node) {
                Object currentName = ((Node) current[i]).get(_name);
                if (currentName == null || !_pattern.matcher(currentName.toString()).matches()) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                }
            }
        }
        return filteredResult;
    }

    private Node[] filterNodeArray(Node[] current) {
        Node[] filtered = new Node[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null) {
                Object currentName = current[i].get(_name);
                if (currentName == null || !_pattern.matcher(currentName.toString()).matches()) {
                    filtered[cursor] = current[i];
                    cursor++;
                }
            }
        }
        if (cursor != current.length) {
            Node[] filtered_2 = new Node[cursor];
            System.arraycopy(filtered, 0, filtered_2, 0, cursor);
            return filtered_2;
        } else {
            return filtered;
        }
    }


}
