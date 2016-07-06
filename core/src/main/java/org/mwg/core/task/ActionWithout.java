package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.task.TaskFunctionSelect;

import java.util.regex.Pattern;

class ActionWithout extends ActionSelect {

    ActionWithout(final String name, final Pattern pattern) {
        super(new TaskFunctionSelect() {
            @Override
            public boolean select(Node node) {
                if (node != null) {
                    Object currentName = node.get(name);
                    if (currentName == null || !pattern.matcher(currentName.toString()).matches()) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public String toString() {
        return "without()";
    }

}
