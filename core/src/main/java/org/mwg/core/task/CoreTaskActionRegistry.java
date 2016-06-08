package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskActionFactory;
import org.mwg.task.TaskActionRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CoreTaskActionRegistry implements TaskActionRegistry {

    private Map<String, TaskActionFactory> _factory = null;

    public CoreTaskActionRegistry() {
        _factory = new HashMap<String, TaskActionFactory>();
        add("get", new TaskActionFactory() { //DefaultTask
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("get action need one parameter");
                }
                return new ActionGet(params[0]);
            }
        });
        add("traverse", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("traverse action need one parameter");
                }
                return new ActionTraverse(params[0]);
            }
        });
        add("traverseOrKeep", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("traverseOrKeep action need one parameter");
                }
                return new ActionTraverseOrKeep(params[0]);
            }
        });
        add("fromIndexAll", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException("fromIndexAll action need one parameter");
                }
                return new ActionFromIndexAll(params[0]);
            }
        });
        add("fromIndex", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 2) {
                    throw new RuntimeException("fromIndex action need two parameter");
                }
                return new ActionFromIndex(params[0], params[1]);
            }
        });
        add("with", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 2) {
                    throw new RuntimeException("with action need two parameter");
                }
                return new ActionWith(params[0], Pattern.compile(params[1]));
            }
        });
        add("without", new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 2) {
                    throw new RuntimeException("without action need two parameter");
                }
                return new ActionWithout(params[0], Pattern.compile(params[1]));
            }
        });
    }

    @Override
    public void add(String name, TaskActionFactory action) {
        _factory.put(name, action);
    }

    @Override
    public void remove(String name) {
        _factory.remove(name);
    }

    @Override
    public TaskActionFactory get(String name) {
        return _factory.get(name);
    }

}
