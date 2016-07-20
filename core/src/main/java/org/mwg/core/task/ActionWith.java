package org.mwg.core.task;

import org.mwg.Constants;
import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelect;
import org.mwg.task.TaskResult;

import java.util.regex.Pattern;

class ActionWith implements TaskAction {

    private final String _patternTemplate;
    private final String _name;

    ActionWith(final String name, final String stringPattern) {
        this._patternTemplate = stringPattern;
        this._name = name;
    }

    @Override
    public String toString() {
        return "with(\'" + _name + "\'" + Constants.QUERY_SEP + "\'" + _patternTemplate + "\')";
    }

    @Override
    public void eval(TaskContext context) {
        final Pattern pattern = Pattern.compile(context.template(_patternTemplate));
        final TaskResult previous = context.result();
        final TaskResult next = context.newResult();
        final int previousSize = previous.size();
        for (int i = 0; i < previousSize; i++) {
            final Object obj = previous.get(i);
            if (obj instanceof AbstractNode) {
                final Node casted = (Node) obj;
                Object currentName = casted.get(_name);
                if (currentName != null && pattern.matcher(currentName.toString()).matches()) {
                    next.add(casted.graph().cloneNode(casted));
                }
            } else {
                next.add(obj);
            }
        }
        context.continueWith(next);
    }
}
