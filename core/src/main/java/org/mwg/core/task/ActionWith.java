package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelect;

import java.util.regex.Pattern;

class ActionWith extends ActionSelect {

    ActionWith(final String name, final String stringPattern) {
        super(new TaskFunctionSelect() {
            @Override
            public boolean select(Node node, TaskContext context) {
                if (node != null) {
                    Object currentName = node.get(name);
                    Pattern pattern = Pattern.compile(context.template(stringPattern));
                    if (currentName != null && pattern.matcher(currentName.toString()).matches()) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public String toString() {
        return "with()";
    }

}
