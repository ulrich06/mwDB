package org.mwg.plugin;

import org.mwg.task.TaskActionFactory;

import java.util.HashMap;
import java.util.Map;

public class AbstractPlugin implements Plugin {

    private final Map<String, NodeFactory> _nodeTypes = new HashMap<String, NodeFactory>();

    private final Map<String, TaskActionFactory> _taskActions = new HashMap<String, TaskActionFactory>();

    @Override
    public Plugin declareNodeType(String name, NodeFactory factory) {
        _nodeTypes.put(name, factory);
        return this;
    }

    @Override
    public Plugin declareTaskAction(String name, TaskActionFactory factory) {
        _taskActions.put(name, factory);
        return this;
    }

    @Override
    public final String[] nodeTypes() {
        return _nodeTypes.keySet().toArray(new String[_nodeTypes.size()]);
    }

    @Override
    public final NodeFactory nodeType(String nodeTypeName) {
        return _nodeTypes.get(nodeTypeName);
    }

    @Override
    public String[] taskActionTypes() {
        return _taskActions.keySet().toArray(new String[_taskActions.size()]);
    }

    @Override
    public TaskActionFactory taskActionType(String taskTypeName) {
        return _taskActions.get(taskTypeName);
    }
    
}
