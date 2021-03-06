package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.Actions;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static org.mwg.task.Actions.newTask;
import static org.mwg.task.Actions.then;

public class ActionJumpTest extends AbstractActionTest {

    @Test
    public void testJump() {
        initGraph();

        newTask()
                .fromIndexAll("nodes")
                .asGlobalVar("nodes")
                .foreach(then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        TaskResult<Node> nodes = context.resultAsNodes();
                        Assert.assertEquals(0, nodes.get(0).time());
                        context.continueWith(null);
                    }
                }))
                .fromVar("nodes")
                .jump("10")
                .foreach(then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        TaskResult<Node> nodes = context.resultAsNodes();
                        Node it = nodes.get(0);
                        Assert.assertEquals(10, it.time());
                        context.continueWith(null);
                    }
                }))
                .execute(graph, null);


        removeGraph();
    }
}
