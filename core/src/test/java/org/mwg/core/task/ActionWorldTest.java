package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionWorldTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .setWorld(10)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.getWorld(), 10);
                    }
                })
                .execute();
        removeGraph();
    }

}
