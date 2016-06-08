package org.mwg.core.task.node;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionCreateNodeTest extends AbstractActionTest {

    public ActionCreateNodeTest() {
        super();
        initGraph();
    }

    @Test
    public void testCreateNode() {
        final long id[] = new long[1];
        graph.newTask()
                .world(15)
                .time(587)
                .createNode()
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context.getPreviousResult());
                        Node n = (Node) context.getPreviousResult();
                        id[0] = n.id();
                        Assert.assertEquals(15,n.world());
                        Assert.assertEquals(587,n.time());
                    }
                })
                .execute();

        graph.lookup(15, 587, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertNotEquals(null,result);
            }
        });
    }

    @Test
    public void testCreateNodeOn() {
        final long id[] = new long[1];
        graph.newTask()
                .world(15)
                .time(587)
                .createNodeOn(87,8745)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context);
                        Node n = (Node) context.getPreviousResult();
                        id[0] = n.id();
                        Assert.assertEquals(87,n.world());
                        Assert.assertEquals(8745,n.time());
                    }
                })
                .execute();

        graph.lookup(87, 8745, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertNotEquals(null,result);
            }
        });
    }
}
