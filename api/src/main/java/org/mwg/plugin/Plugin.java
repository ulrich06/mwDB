package org.mwg.plugin;

import org.mwg.task.TaskActionFactory;

public interface Plugin {

    Plugin declareNodeType(String name, NodeFactory factory);

    Plugin declareTaskAction(String name, TaskActionFactory factory);

    String[] nodeTypes();

    NodeFactory nodeType(String nodeTypeName);

    String[] taskActionTypes();

    TaskActionFactory taskActionType(String taskTypeName);

}
