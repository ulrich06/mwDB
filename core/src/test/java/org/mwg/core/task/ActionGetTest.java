package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

public class ActionGetTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .fromIndexAll("nodes")
                .get("children")
                .get("name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Object[] lastResult = (Object[]) context.getPreviousResult();
                        Assert.assertEquals(lastResult[0], "n0");
                        Assert.assertEquals(lastResult[1], "n1");
                    }
                })
                .execute();
        removeGraph();
    }

    @Test
    public void testDefaultSynthax() {
        initGraph();
        graph.newTask()
                .fromIndexAll("nodes")
                .parse("children.name")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Object[] lastResult = (Object[]) context.getPreviousResult();
                        Assert.assertEquals(lastResult[0], "n0");
                        Assert.assertEquals(lastResult[1], "n1");
                    }
                })
                .execute();
        removeGraph();
    }

    /*
    @Test
    public void testParse() {
        initGraph();
        graph.newTask()
                .parse("fromIndexAll(nodes).traverse(children)")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] lastResult = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(lastResult[0].get("name"), "n0");
                        Assert.assertEquals(lastResult[1].get("name"), "n1");
                    }
                })
                .execute();
        removeGraph();
    }

    @Test
    public void testTraverseIndex() {
        initGraph();
        Node node1 = graph.newNode(0, 0);
        node1.setProperty("name", Type.STRING, "node1");
        node1.setProperty("value", Type.INT, 1);

        Node node2 = graph.newNode(0, 0);
        node2.setProperty("name", Type.STRING, "node2");
        node2.setProperty("value", Type.INT, 2);

        Node node3 = graph.newNode(0, 12);
        node3.setProperty("name", Type.STRING, "node3");
        node3.setProperty("value", Type.INT, 3);

        Node root = graph.newNode(0, 0);
        root.setProperty("name", Type.STRING, "root2");
        graph.index("rootIndex", root, "name", new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                root.index("childrenIndexed", node1, "name", null);
                root.index("childrenIndexed", node2, "name", null);
                root.index("childrenIndexed", node3, "name", null);

                root.jump(12, new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        root.index("childrenIndexed", node3, "name", null);
                    }
                });

            }
        });

        graph.newTask()
                .fromIndex("rootIndex", "name=root2")
                .traverseIndex("childrenIndexed", "name=node2")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] n = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(1, n.length);
                        Assert.assertEquals("node2", n[0].get("name"));
                    }
                }).execute();

        graph.newTask()
                .fromIndex("rootIndex", "name=root2")
                .traverseIndex("childrenIndexed", "name=node3")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] n = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(0, n.length);
                    }
                }).execute();

        graph.newTask().time(12)
                .fromIndex("rootIndex", "name=root2")
                .traverseIndex("childrenIndexed", "name=node2")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] n = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(1, n.length);
                        Assert.assertEquals("node2", n[0].get("name"));
                    }
                }).execute();

        graph.newTask()
                .fromIndex("rootIndex", "name=root2")
                .traverseIndexAll("childrenIndexed")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] n = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(2, n.length);
                        Assert.assertEquals("node1", n[0].get("name"));
                        Assert.assertEquals("node2", n[1].get("name"));
                    }
                }).execute();

        graph.newTask().time(13)
                .fromIndex("rootIndex", "name=root2")
                .traverseIndexAll("childrenIndexed")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node[] n = (Node[]) context.getPreviousResult();
                        Assert.assertEquals(3, n.length);
                        Assert.assertEquals("node1", n[0].get("name"));
                        Assert.assertEquals("node2", n[1].get("name"));
                        Assert.assertEquals("node3", n[2].get("name"));
                    }
                }).execute();
        removeGraph();
    }*/

}
