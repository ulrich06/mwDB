package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.core.task.AbstractActionTest;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import static org.mwg.task.Actions.*;

public class ActionAddTest extends AbstractActionTest {

    public ActionAddTest() {
        super();
        initGraph();
    }

    @Test
    public void testWithOneNode() {
        Node relatedNode = graph.newNode(0, 0);
        final long[] id = new long[1];
        newNode()
                .inject(relatedNode).asGlobalVar("x")
                .add("friend", "x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node node = (Node) context.result().get(0);
                        Assert.assertNotNull(node);
                        Assert.assertEquals(1, ((long[]) node.get("friend")).length);
                        id[0] = node.id();
                    }
                }).execute(graph, new Callback<TaskResult>() {
            @Override
            public void on(TaskResult result) {
                graph.lookup(0, 0, id[0], new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        Assert.assertEquals(1, ((long[]) result.get("friend")).length);
                    }
                });
            }
        });
    }

    @Test
    public void testWithArray() {
        Node relatedNode = graph.newNode(0, 0);

        final long[] ids = new long[5];
        inject(relatedNode).asGlobalVar("x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] nodes = new Node[5];
                        for (int i = 0; i < 5; i++) {
                            nodes[i] = graph.newNode(0, 0);
                        }
                        context.continueWith(context.wrap(nodes));
                    }
                })
                .add("friend", "x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        TaskResult<Node> nodes = context.resultAsNodes();
                        Assert.assertNotNull(nodes);
                        for (int i = 0; i < 5; i++) {
                            Assert.assertEquals(1, ((long[]) nodes.get(i).get("friend")).length);
                            ids[i] = nodes.get(i).id();
                        }
                    }
                }).execute(graph, null);

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

        final boolean[] nextCalled = new boolean[1];

        then(new Action() {
            @Override
            public void eval(TaskContext context) {
                context.continueWith(null);
            }
        }).inject(relatedNode).asGlobalVar("x")
                .add("friend", "x")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        nextCalled[0] = true;
                    }
                }).execute(graph, null);

        Assert.assertTrue(nextCalled[0]);
    }

}
