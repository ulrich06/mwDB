package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionWorldTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .world(10)
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.getWorld(), 10);
                    }
                })
                .execute();
        removeGraph();
    }

}
