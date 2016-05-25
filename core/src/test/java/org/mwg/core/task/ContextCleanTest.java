package org.mwg.core.task;

import org.junit.Assert;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelect;

public class ContextCleanTest {

   // @Test
    public void finalCleanTest() {
        TaskContext[] retention = new TaskContext[1];

        Graph graph = GraphBuilder.builder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                graph.newTask().then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        retention[0] = context;
                        Node node = graph.newNode(0, 0);
                        node.set("name", "node");
                        context.setResult(node);
                    }
                }).execute();
            }
        });

        boolean shouldCrash = false;
        try {
            System.out.println(retention[0]);
        } catch (Exception e) {
            shouldCrash = true;
        }
        Assert.assertEquals(shouldCrash, true);
    }

   // @Test
    public void complexTest() {
        TaskContext[] retention = new TaskContext[2];
        Graph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final String[] flat = {""};
                Node n0 = graph.newNode(0, 0);
                Node n1 = graph.newNode(0, 0);
                graph
                        .newTask()
                        .from(new Node[]{n0, n1})
                        .select(new TaskFunctionSelect() {
                            @Override
                            public boolean select(Node node) {
                                return true;
                            }
                        })
                        .then(new TaskAction() {
                            @Override
                            public void eval(TaskContext context) {
                                retention[0] = context;
                            }
                        })
                        .foreachThen(new Callback<Node>() {
                            @Override
                            public void on(Node result) {
                                flat[0] += result.toString();
                            }
                        })
                        .executeThen(new TaskAction() {
                            @Override
                            public void eval(TaskContext context) {
                                retention[1] = context;
                            }
                        });
            }
        });

        boolean shouldCrash = false;
        try {
            System.out.println(retention[0]);
            System.out.println(retention[1]);
        } catch (Exception e) {
            shouldCrash = true;
        }
        Assert.assertEquals(shouldCrash, true);
    }

}
