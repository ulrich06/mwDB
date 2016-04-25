package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionTimeTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .time(10)
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.getTime(), 10);
                    }
                })
                .execute();
    }

}
