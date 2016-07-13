package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.Actions;
import org.mwg.task.TaskContext;


/**
 * Created by ludovicmouline on 13/07/16.
 */
public class ActionJumpTest extends AbstractActionTest {

    @Test
    public void testJump() {
        initGraph();

        Actions.newTask()
                .fromIndexAll("nodes")
                .asVar("nodes")
                .foreach(Actions.then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node it = (Node) context.result();
                        Assert.assertEquals(0,it.time());
                        System.err.println(it.time());
                        context.setResult(null);
                    }
                }))
                .fromVar("nodes")
                .jump("10")
                .foreach(Actions.then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node it = (Node) context.result();
                        Assert.assertEquals(10,it.time());
                        System.err.println(it.time());
                        context.setResult(null);
                    }
                }))
                .execute(graph,null);


        removeGraph();
    }
}
