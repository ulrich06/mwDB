package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionTraverseOrKeepTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        new CoreTask()
                .fromIndexAll("nodes")
                .traverseOrKeep("children")
                .traverseOrKeep("children")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] lastResult = (Node[]) context.result();
                        Assert.assertEquals(lastResult[0].get("name"), "n0");
                        Assert.assertEquals(lastResult[1].get("name"), "n1");
                    }
                })
                .execute(graph,null);
        removeGraph();
    }

}
