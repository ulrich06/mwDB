package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionFromVarTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .fromIndexAll("nodes")
                .asVar("x")
                .from("uselessPayload")
                .fromVar("x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.result())[0].get("name"), "n0");
                        Assert.assertEquals(((Node[]) context.result())[1].get("name"), "n1");
                        Assert.assertEquals(((Node[]) context.result())[2].get("name"), "root");
                    }
                })
                .execute();
        removeGraph();
    }

}
