package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionAsVarTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .from("hello")
                .asVar("myVar")
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.getPreviousResult(), "hello");
                        Assert.assertEquals(context.getVariable("myVar"), "hello");
                    }
                })
                .execute();

        removeGraph();
    }

}
