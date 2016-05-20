package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionFromIndexAllTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .from("uselessPayload")
                .fromIndexAll("nodes")
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.getPreviousResult())[0].get("name"), "n0");
                        Assert.assertEquals(((Node[]) context.getPreviousResult())[1].get("name"), "n1");
                        Assert.assertEquals(((Node[]) context.getPreviousResult())[2].get("name"), "root");
                    }
                })
                .execute();
        removeGraph();
    }

}
