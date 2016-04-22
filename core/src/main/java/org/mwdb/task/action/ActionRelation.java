package org.mwdb.task.action;

import org.mwdb.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionRelation implements KTaskAction {

    private final String _name;

    public ActionRelation(final String p_name) {
        this._name = p_name;
    }

    @Override
    public final void eval(final KTaskContext context) {
        Object previousResult = context.getPreviousResult();
        if (previousResult != null) {
            //dry execute to count waiter
            Set<Long> toLoad = new HashSet<Long>();
            if (previousResult instanceof KNode[]) {
                collectNodeArray((KNode[]) previousResult, toLoad);
            } else if (previousResult instanceof Object[]) {
                collectArray((Object[]) previousResult, toLoad);
            } else if (previousResult instanceof KNode) {
                KNode loop = (KNode) previousResult;
                Object rel = loop.att(_name);
                if (rel != null && rel instanceof long[]) {
                    long[] interResult = (long[]) rel;
                    for (int j = 0; j < interResult.length; j++) {
                        toLoad.add(interResult[j]);
                    }
                }
            }
            if (!toLoad.isEmpty()) {
                KDeferCounter deferCounter = context.graph().counter(toLoad.size());
                final KNode[] resultNodes = new KNode[toLoad.size()];
                final AtomicInteger cursor = new AtomicInteger();
                for (Long idNode : toLoad) {
                    context.graph().lookup(context.getWorld(), context.getTime(), idNode, new KCallback<KNode>() {
                        @Override
                        public void on(KNode result) {
                            resultNodes[cursor.getAndIncrement()] = result;
                            deferCounter.count();
                        }
                    });
                }
                deferCounter.then(new KCallback() {
                    @Override
                    public void on(Object result) {
                        context.setResult(resultNodes);
                        context.next();
                    }
                });
            } else {
                context.next();
            }
        } else {
            context.next();
        }
    }

    private void collectArray(Object[] current, Set<Long> toLoad) {
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof KNode[]) {
                collectNodeArray((KNode[]) current[i], toLoad);
            } else if (current[i] instanceof Object[]) {
                collectArray((Object[]) current[i], toLoad);
            } else if (current[i] instanceof KNode) {
                KNode loop = (KNode) current[i];
                Object rel = loop.att(_name);
                if (rel != null && rel instanceof long[]) {
                    long[] interResult = (long[]) rel;
                    for (int j = 0; j < interResult.length; j++) {
                        toLoad.add(interResult[j]);
                    }
                }
            }
        }
    }

    private void collectNodeArray(KNode[] current, Set<Long> toLoad) {
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null) {
                Object rel = current[i].att(_name);
                if (rel != null && rel instanceof long[]) {
                    long[] interResult = (long[]) rel;
                    for (int j = 0; j < interResult.length; j++) {
                        toLoad.add(interResult[j]);
                    }
                }
            }
        }
    }

}
