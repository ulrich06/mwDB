package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.Job;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionJump implements TaskAction {
    private final String _time;

    ActionJump(String time) {
        _time = time;
    }

    @Override
    public void eval(TaskContext context) {
        String templatedTime = context.template(_time);
        long time = Long.parseLong(templatedTime);

        Object previousResult = context.result();
        Node[] jumpedNodes = TaskHelper.flatNodes(previousResult,false);
        final DeferCounter defer = new CoreDeferCounter(jumpedNodes.length);

        Node[] result = new Node[jumpedNodes.length];

        for(int i=0;i<jumpedNodes.length;i++) {
            final int ii = i;
            jumpedNodes[i].jump(time, new Callback<Node>() {
                @Override
                public void on(Node jumped) {
                    result[ii] = jumped;
                    defer.count();
                }
            });
        }

        defer.then(new Job() {
            @Override
            public void run() {
                context.cleanObj(previousResult);
                context.setUnsafeResult(result);
            }
        });

    }

    @Override
    public String toString() {
        return "jump(" + _time + ")";
    }
}
