package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionIndexOrUnindexNode implements TaskAction {
    private final String _indexName;
    private final String _flatKeyAttributes;
    private final boolean _isIndexation;

    ActionIndexOrUnindexNode(String indexName, String flatKeyAttributes, boolean isIndexation) {
        this._indexName = indexName;
        this._flatKeyAttributes = flatKeyAttributes;
        this._isIndexation = isIndexation;
    }

    @Override
    public void eval(TaskContext context) {
        final TaskResult previousResult = context.result();
        final String templatedIndexName = context.template(_indexName);
        final String templatedKeyAttributes = context.template(_flatKeyAttributes);
        DeferCounter counter = new CoreDeferCounter(previousResult.size());

        Callback<Boolean> end = new Callback<Boolean>() {
            @Override
            public void on(Boolean succeed) {
                if (succeed) {
                    counter.count();
                } else {
                    throw new RuntimeException("Error during indexation of node with id " + ((Node) previousResult.get(0)).id());
                }
            }
        };
        for (int i = 0; i < previousResult.size(); i++) {
            Object loop = previousResult.get(i);
            if (loop instanceof AbstractNode) {
                if (_isIndexation) {
                    context.graph().index(templatedIndexName, (Node) loop, templatedKeyAttributes, end);
                } else {
                    context.graph().unindex(templatedIndexName, (Node) loop, templatedKeyAttributes, end);
                }
            } else {
                counter.count();
            }
        }
        counter.then(new Job() {
            @Override
            public void run() {
                context.continueTask();
            }
        });
    }

    @Override
    public String toString() {
        if (_isIndexation) {
            return "indexNode('" + _indexName + "','" + _flatKeyAttributes + "')";
        } else {
            return "unindexNode('" + _indexName + "','" + _flatKeyAttributes + "')";
        }
    }
}
