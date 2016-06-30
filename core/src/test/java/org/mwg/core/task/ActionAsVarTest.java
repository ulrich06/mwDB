package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.*;

public class ActionAsVarTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        from("hello").asVar("myVar").then(new Action() {
            @Override
            public void eval(TaskContext context) {
                Assert.assertEquals(context.result(), "hello");
                Assert.assertEquals(context.variable("myVar"), "hello");
            }
        }).execute(graph);
        removeGraph();
    }

}
