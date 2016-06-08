package org.mwg.core.task.node;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionNodeSetPropertyTest extends AbstractActionTest {

    public ActionNodeSetPropertyTest() {
        super();
        initGraph();
    }

    @Test
    public void testWithOneNode() {
        long[] id = new long[1];
        graph.newTask()
                .world(0)
                .time(0)
                .createNode()
                .nodeSetProperty("name", Type.STRING,"node")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context.getPreviousResult());
                        Node node = (Node) context.getPreviousResult();
                        Assert.assertEquals("node",node.get("name"));

                        id[0] = node.id();
                    }
                }).execute();

        graph.lookup(0, 0, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertEquals("node",result.get("name"));
            }
        });
    }

    @Test
    public void testWithArray() {
        long[] ids = new long[5];
        graph.newTask()
                .world(0)
                .time(0)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] nodes = new Node[5];
                        for(int i=0;i<5;i++) {
                            nodes[i] = graph.newNode(0,0);
                        }
                        context.setResult(nodes);
                    }
                })
                .nodeSetProperty("name", Type.STRING,"node")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context.getPreviousResult());
                        Node[] nodes = (Node[]) context.getPreviousResult();

                        for(int i=0;i<5;i++) {
                            Assert.assertEquals("node",nodes[i].get("name"));
                            ids[i] = nodes[i].id();
                        }
                    }
                }).execute();

        for(int i=0;i<ids.length;i++) {
            graph.lookup(0, 0, ids[i], new Callback<Node>() {
                @Override
                public void on(Node result) {
                    Assert.assertEquals("node", result.get("name"));
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
                .nodeSetProperty("name", Type.STRING,"node")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        nextCalled[0] = true;
                    }
                }).execute();

        Assert.assertTrue(nextCalled[0]);
    }

    @Test
    public void testWithObject() {
        boolean[] exceptionCaught = new boolean[1];

        try {
            graph.newTask()
                    .world(0)
                    .time(0)
                    .from(10)
                    .nodeSetProperty("name", Type.STRING,"node")
                    .execute();
        } catch (RuntimeException ex) {
            exceptionCaught[0] = true;
        } catch (Exception ex) {
            Assert.fail("Unexpected exception thrown");
        }

        Assert.assertTrue(exceptionCaught[0]);

    }
}
