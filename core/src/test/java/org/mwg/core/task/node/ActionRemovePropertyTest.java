package org.mwg.core.task.node;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

/**
 * Created by ludovicmouline on 08/06/16.
 */
public class ActionRemovePropertyTest extends AbstractActionTest {

    public ActionRemovePropertyTest() {
        super();
        initGraph();
    }

    @Test
    public void testWithOneNode() {
        long[] id = new long[1];
        graph.newTask()
                .world(0)
                .time(0)
                .from("node").asVar("nodeName")
                .newNode()
                .set("name", "nodeName")
                .removeProperty("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node node = (Node) context.getPreviousResult();
                        Assert.assertNotNull(node);
                        Assert.assertNull(node.get("name"));

                        id[0] = node.id();
                    }
                }).execute();

        graph.lookup(0, 0, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertNull(result.get("name"));
            }
        });
    }

    @Test
    public void testWithArray() {
        long[] ids = new long[5];
        graph.newTask()
                .world(0)
                .time(0)
                .from("node").asVar("nodeName")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] nodes = new Node[5];
                        for (int i = 0; i < 5; i++) {
                            nodes[i] = graph.newNode(0, 0);
                        }
                        context.setResult(nodes);
                    }
                })
                .set("name", "nodeName")
                .removeProperty("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] nodes = (Node[]) context.getPreviousResult();
                        Assert.assertNotNull(nodes);

                        for (int i = 0; i < 5; i++) {
                            Assert.assertNull(nodes[i].get("name"));
                            ids[i] = nodes[i].id();
                        }
                    }
                }).execute();

        for (int i = 0; i < ids.length; i++) {
            graph.lookup(0, 0, ids[i], new Callback<Node>() {
                @Override
                public void on(Node result) {
                    Assert.assertNull(result.get("name"));
                }
            });
        }
    }

    @Test
    public void testWithNull() {
        boolean[] nextCalled = new boolean[1];
        graph.newTask()
                .world(0)
                .time(0)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        context.setResult(null);
                    }
                })
                .set("name", "node")
                .removeProperty("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        nextCalled[0] = true;
                    }
                }).execute();

        Assert.assertTrue(nextCalled[0]);
    }

}
