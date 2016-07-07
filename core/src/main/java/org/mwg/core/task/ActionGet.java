package org.mwg.core.task;

import org.mwg.*;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class ActionGet implements TaskAction {

    private final String _name;

    ActionGet(final String p_name) {
        this._name = p_name;
    }

    @Override
    public final void eval(final TaskContext context) {
        Object previousResult = context.result();
        if (previousResult != null) {
            String flatName = context.template(_name);
            //dry execute to count waiter
            Set<Long> collectedIds = new HashSet<Long>();
            List<Object> collectedProperties = new ArrayList<Object>();
            if (previousResult instanceof Object[]) {
                collectArray((Object[]) previousResult, collectedIds, collectedProperties,flatName);
            } else if (previousResult instanceof AbstractNode) {
                Node loop = (Node) previousResult;
                Object propValue = loop.get(flatName);
                if (propValue != null) {
                    byte propType = loop.type(flatName);
                    switch (propType) {
                        case Type.RELATION:
                            long[] propValueRef = (long[]) propValue;
                            for (int j = 0; j < propValueRef.length; j++) {
                                collectedIds.add(propValueRef[j]);
                            }
                            break;
                        default:
                            collectedProperties.add(propValue);
                            break;
                    }
                }
                loop.free();
            }
            final DeferCounter deferCounter = context.graph().newCounter(collectedIds.size());
            final Node[] resultNodes = new Node[collectedIds.size()];

            if (collectedIds.size() > 0) {
                final AtomicInteger cursor = new AtomicInteger(0);
                for (Long idNode : collectedIds) {
                    context.graph().lookup(context.world(), context.time(), idNode, new Callback<Node>() {
                        @Override
                        public void on(Node result) {
                            resultNodes[cursor.getAndIncrement()] = result;
                            deferCounter.count();
                        }
                    });
                }
                final Object[] finalCollectedProperties = collectedProperties.toArray(new Object[collectedProperties.size()]);
                deferCounter.then(new Job() {
                    @Override
                    public void run() {
                        if (finalCollectedProperties == null) {
                            context.setResult(resultNodes);
                        } else {
                            Object[] merged = new Object[resultNodes.length + finalCollectedProperties.length];
                            System.arraycopy(resultNodes, 0, merged, 0, resultNodes.length);
                            System.arraycopy(finalCollectedProperties, 0, merged, resultNodes.length, finalCollectedProperties.length);
                            context.setResult(merged);
                        }
                    }
                });
            } else {
                //potentially shrink result array
                final Object[] finalCollectedProperties = collectedProperties.toArray(new Object[collectedProperties.size()]);
                context.setUnsafeResult(finalCollectedProperties);
            }
        } else {
            context.setUnsafeResult(null);
        }
    }

    private void collectArray(Object[] current, Set<Long> toLoad, List<Object> leafs,String flatName) {
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof Object[]) {
                collectArray((Object[]) current[i], toLoad, leafs,flatName);
            } else if (current[i] instanceof AbstractNode) {
                Node loop = (Node) current[i];
                Object propValue = loop.get(flatName);
                if (propValue != null) {
                    byte propType = loop.type(flatName);
                    switch (propType) {
                        case Type.RELATION:
                            long[] interResult = (long[]) propValue;
                            for (int j = 0; j < interResult.length; j++) {
                                toLoad.add(interResult[j]);
                            }
                            break;
                        default:
                            leafs.add(propValue);
                            break;
                    }
                }
                loop.free();
            }
        }
    }

    @Override
    public String toString() {
        return "get(\'" + _name + "\')";
    }
}