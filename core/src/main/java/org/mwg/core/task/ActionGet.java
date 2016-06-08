package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.plugin.AbstractNode;
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
        Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            //dry execute to count waiter
            Set<Long> collectedIds = new HashSet<Long>();
            List<Object> collectedProperties = new ArrayList<Object>();
            if (previousResult instanceof Object[]) {
                collectArray((Object[]) previousResult, collectedIds, collectedProperties);
            } else if (previousResult instanceof AbstractNode) {
                Node loop = (Node) previousResult;
                Object propValue = loop.get(_name);
                if (propValue != null) {
                    byte propType = loop.type(_name);
                    switch (propType) {
                        case Type.REF:
                        case Type.DEP_REF:
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
            }
            DeferCounter deferCounter = context.graph().counter(collectedIds.size());
            final Node[] resultNodes = new Node[collectedIds.size()];

            if (collectedIds.size() > 0) {
                final AtomicInteger cursor = new AtomicInteger(0);
                for (Long idNode : collectedIds) {
                    context.graph().lookup(context.getWorld(), context.getTime(), idNode, new Callback<Node>() {
                        @Override
                        public void on(Node result) {
                            resultNodes[cursor.getAndIncrement()] = result;
                            deferCounter.count();
                        }
                    });
                }
                Object[] finalCollectedProperties = collectedProperties.toArray(new Object[collectedProperties.size()]);
                deferCounter.then(new Callback() {
                    @Override
                    public void on(java.lang.Object result) {
                        if (finalCollectedProperties == null) {
                            context.setResult(resultNodes);
                            context.next();
                        } else {
                            Object[] merged = new Object[resultNodes.length + finalCollectedProperties.length];
                            System.arraycopy(resultNodes, 0, merged, 0, resultNodes.length);
                            System.arraycopy(finalCollectedProperties, 0, merged, resultNodes.length, finalCollectedProperties.length);
                            context.setResult(merged);
                            context.next();
                        }
                    }
                });
            } else {
                //potentially shrink result array
                final Object[] finalCollectedProperties = collectedProperties.toArray(new Object[collectedProperties.size()]);
                context.setResult(finalCollectedProperties);
                context.next();
            }
        } else {
            context.next();
        }
    }

    private void collectArray(Object[] current, Set<Long> toLoad, List<Object> leafs) {
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof Object[]) {
                collectArray((Object[]) current[i], toLoad, leafs);
            } else if (current[i] instanceof AbstractNode) {
                Node loop = (Node) current[i];
                Object propValue = loop.get(_name);
                if (propValue != null) {
                    byte propType = loop.type(_name);
                    switch (propType) {
                        case Type.REF:
                        case Type.DEP_REF:
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
            }
        }
    }

}