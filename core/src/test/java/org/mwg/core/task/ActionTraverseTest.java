package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionTraverseTest extends AbstractActionTest {

    @Test
    public void test() {
        graph
                .newTask()
                .fromIndexAll("nodes")
                .traverse("children")
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] lastResult = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(lastResult[0].get("name"), "n0");
                        Assert.assertEquals(lastResult[1].get("name"), "n1");
                    }
                })
                .execute();
    }

}
