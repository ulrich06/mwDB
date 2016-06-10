package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionAsVarTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .from("hello")
                .asVar("myVar")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.result(), "hello");
                        Assert.assertEquals(context.variable("myVar"), "hello");
                    }
                })
                .execute();

        removeGraph();
    }

}
