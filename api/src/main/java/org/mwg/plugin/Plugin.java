package org.mwg.plugin;

import org.mwg.task.TaskActionFactory;

public interface Plugin {

    String[] nodeTypes();

    NodeFactory nodeType(String nodeTypeName);

    String[] taskActionTypes();

    TaskActionFactory taskActionType(String taskTypeName);

}
