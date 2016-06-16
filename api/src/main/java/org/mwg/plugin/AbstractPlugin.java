package org.mwg.plugin;

import org.mwg.task.TaskActionFactory;

import java.util.HashMap;
import java.util.Map;

public class AbstractPlugin implements Plugin {

    private final Map<String, NodeFactory> _nodeTypes = new HashMap<String, NodeFactory>();

    private final Map<String, TaskActionFactory> _taskActionTypes = new HashMap<String, TaskActionFactory>();

    public AbstractPlugin declareNode(String name, NodeFactory factory) {
        _nodeTypes.put(name, factory);
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
        return _taskActionTypes.keySet().toArray(new String[_taskActionTypes.size()]);
    }

    @Override
    public TaskActionFactory taskActionType(String taskTypeName) {
        return _taskActionTypes.get(taskTypeName);
    }

    public AbstractPlugin declareAction(String name, TaskActionFactory factory) {
        _taskActionTypes.put(name, factory);
        return this;
    }

}
