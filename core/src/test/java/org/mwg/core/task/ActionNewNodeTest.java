package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.inject;
import static org.mwg.task.Actions.setWorld;

public class ActionNewNodeTest extends AbstractActionTest {

    public ActionNewNodeTest() {
        super();
        initGraph();
    }

    @Test
    public void testCreateNode() {
        final long id[] = new long[1];
        inject(15).asVar("world").setWorld("{{world}}").
                inject(587).asVar("time").setTime("{{time}}").newNode()
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context.result());
                        Node n = (Node) context.result();
                        id[0] = n.id();
                        Assert.assertEquals(15, n.world());
                        Assert.assertEquals(587, n.time());
                    }
                })
                .execute(graph, null);

        graph.lookup(15, 587, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertNotEquals(null, result);
            }
        });
    }

}
