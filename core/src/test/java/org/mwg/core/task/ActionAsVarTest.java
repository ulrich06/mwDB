package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static org.mwg.task.Actions.*;

public class ActionAsVarTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        inject("hello").asGlobalVar("myVar").then(new Action() {
            @Override
            public void eval(TaskContext context) {
                Assert.assertEquals(context.result().get(0), "hello");
                Assert.assertEquals(context.variable("myVar").get(0), "hello");
                context.continueTask();
            }
        }).execute(graph, new Callback<TaskResult>() {
            @Override
            public void on(TaskResult result) {
                Assert.assertNotEquals(result.size(),0);
            }
        });
        removeGraph();
    }

}
