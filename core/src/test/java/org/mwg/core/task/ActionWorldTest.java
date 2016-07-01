package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.setWorld;

public class ActionWorldTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        setWorld(10)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.world(), 10);
                    }
                })
                .execute(graph,null);
        removeGraph();
    }

}
