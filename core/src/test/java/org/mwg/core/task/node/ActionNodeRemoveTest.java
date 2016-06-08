package org.mwg.core.task.node;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionNodeRemoveTest extends AbstractActionTest {

    public ActionNodeRemoveTest() {
        super();
        initGraph();
    }

    @Test
    public void testWithOneNode() {
        Node relatedNode = graph.newNode(0,0);

        long[] id = new long[1];
        graph.newTask()
                .world(0)
                .time(0)
                .createNode()
                .nodeAdd("friend",relatedNode)
                .nodeRemove("friend",relatedNode)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context.getPreviousResult());
                        Node node = (Node) context.getPreviousResult();
                        Assert.assertNull(node.get("friend"));
                        id[0] = node.id();
                    }
                }).execute();


        graph.lookup(0, 0, id[0], new Callback<Node>() {
            @Override
            public void on(Node result) {
                Assert.assertNull(result.get("friend"));
            }
        });
    }

    @Test
    public void testWithArray() {
        Node relatedNode = graph.newNode(0,0);

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
                .nodeAdd("friend",relatedNode)
                .nodeRemove("friend",relatedNode)
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertNotNull(context.getPreviousResult());
                        Node[] nodes = (Node[]) context.getPreviousResult();

                        for(int i=0;i<5;i++) {
                            Assert.assertNull(nodes[i].get("friend"));
                            ids[i] = nodes[i].id();
                        }
                    }
                }).execute();

        for(int i=0;i<ids.length;i++) {
            graph.lookup(0, 0, ids[i], new Callback<Node>() {
                @Override
                public void on(Node result) {
                    Assert.assertNull(result.get("friend"));
                }
            });
        }


    }

    @Test
    public void testWithNull() {
        Node relatedNode = graph.newNode(0,0);

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
                .nodeAdd("friend",relatedNode)
                .nodeRemove("friend",relatedNode)
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
        Node relatedNode = graph.newNode(0,0);

        boolean[] exceptionCaught = new boolean[1];

        try {
            graph.newTask()
                    .world(0)
                    .time(0)
                    .from(10)
                    .nodeRemove("name",relatedNode)
                    .execute();
        } catch (RuntimeException ex) {
            exceptionCaught[0] = true;
        } catch (Exception ex) {
            Assert.fail("Unexpected exception thrown");
        }

        Assert.assertTrue(exceptionCaught[0]);

    }
}
