package org.mwg.core.task.node;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionAddTest extends AbstractActionTest {

    public ActionAddTest() {
        super();
        initGraph();
    }

    @Test
    public void testWithOneNode() {
        Node relatedNode = graph.newNode(0, 0);

        long[] id = new long[1];
        graph.newTask()
                .setWorld(0)
                .setTime(0)
                .newNode()
                .from(relatedNode).asVar("x")
                .add("friend", "x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node node = (Node) context.result();
                        Assert.assertNotNull(node);
                        Assert.assertEquals(1, ((long[]) node.get("friend")).length);
                        id[0] = node.id();
                    }
                }).execute();


        graph.lookup(0, 0, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertEquals(1, ((long[]) result.get("friend")).length);
            }
        });
    }

    @Test
    public void testWithArray() {
        Node relatedNode = graph.newNode(0, 0);

        long[] ids = new long[5];
        graph.newTask()
                .setWorld(0)
                .setTime(0)
                .from(relatedNode).asVar("x")
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
                .add("friend", "x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] nodes = (Node[]) context.result();
                        Assert.assertNotNull(nodes);

                        for (int i = 0; i < 5; i++) {
                            Assert.assertEquals(1, ((long[]) nodes[i].get("friend")).length);
                            ids[i] = nodes[i].id();
                        }
                    }
                }).execute();

        for (int i = 0; i < ids.length; i++) {
            graph.lookup(0, 0, ids[i], new Callback<Node>() {
                @Override
                public void on(Node result) {
                    Assert.assertEquals(1, ((long[]) result.get("friend")).length);
                }
            });
        }


    }

    @Test
    public void testWithNull() {
        Node relatedNode = graph.newNode(0, 0);

        boolean[] nextCalled = new boolean[1];
        graph.newTask()
                .setWorld(0)
                .setTime(0)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        context.setResult(null);
                    }
                })
                .from(relatedNode).asVar("x")
                .add("friend", "x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        nextCalled[0] = true;
                    }
                }).execute();

        Assert.assertTrue(nextCalled[0]);
    }

}
