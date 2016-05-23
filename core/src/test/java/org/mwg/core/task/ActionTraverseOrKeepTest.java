package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionTraverseOrKeepTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .fromIndexAll("nodes")
                .traverseOrKeep("children")
                .traverseOrKeep("children")
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] lastResult = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(lastResult[0].get("name"), "n0");
                        Assert.assertEquals(lastResult[1].get("name"), "n1");
                    }
                })
                .execute();
        removeGraph();
    }

}
