package org.mwg.core.task.node;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.setWorld;

public class ActionRemovePropertyTest extends AbstractActionTest {

    public ActionRemovePropertyTest() {
        super();
        initGraph();
    }

    @Test
    public void testWithOneNode() {
        final long[] id = new long[1];
        setWorld(0)
                .setTime(0)
                .from("node").asVar("nodeName")
                .newNode()
                .setProperty("name", Type.STRING, "nodeName")
                .removeProperty("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node node = (Node) context.result();
                        Assert.assertNotNull(node);
                        Assert.assertNull(node.get("name"));

                        id[0] = node.id();
                    }
                }).execute(graph);

        graph.lookup(0, 0, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertNull(result.get("name"));
            }
        });
    }

    @Test
    public void testWithArray() {
        final long[] ids = new long[5];
        setWorld(0)
                .setTime(0)
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
                .setProperty("name", Type.STRING, "nodeName")
                .removeProperty("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] nodes = (Node[]) context.result();
                        Assert.assertNotNull(nodes);

                        for (int i = 0; i < 5; i++) {
                            Assert.assertNull(nodes[i].get("name"));
                            ids[i] = nodes[i].id();
                        }
                    }
                }).execute(graph);

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
        final boolean[] nextCalled = new boolean[1];
        setWorld(0)
                .setTime(0)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        context.setResult(null);
                    }
                })
                .setProperty("name", Type.STRING, "node")
                .removeProperty("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        nextCalled[0] = true;
                    }
                }).execute(graph);

        Assert.assertTrue(nextCalled[0]);
    }

}
