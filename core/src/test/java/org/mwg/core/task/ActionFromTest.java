package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionFromTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .from("uselessPayload")
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.getPreviousResult(), "uselessPayload");
                    }
                })
                .execute();
    }

}
