package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.ArrayList;
import java.util.List;

public class ActionForeachTest extends AbstractActionTest {

    @Test
    public void testForeachWhere() {

        final long[] i = {0};
        graph.newTask()
                .from(new long[]{1, 2, 3})
                .foreach(graph.newTask().then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        i[0]++;
                        Assert.assertEquals(context.getPreviousResult(), i[0]);
                        context.setResult(context.getPreviousResult());//propagate result
                    }
                }))
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Object[] result = (Object[]) context.getPreviousResult();
                        Assert.assertEquals(result.length, 3);
                        Assert.assertEquals(result[0], 1l);
                        Assert.assertEquals(result[1], 2l);
                        Assert.assertEquals(result[2], 3l);
                    }
                })
                .execute();

        graph.newTask().fromIndexAll("nodes").foreach(graph.newTask().then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                context.setResult(context.getPreviousResult());
            }
        })).then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                Object[] result = (Object[]) context.getPreviousResult();
                Assert.assertEquals(result.length, 3);
                Assert.assertEquals(((Node) result[0]).get("name"), "n0");
                Assert.assertEquals(((Node) result[1]).get("name"), "n1");
                Assert.assertEquals(((Node) result[2]).get("name"), "root");
            }
        }).execute();


        List<String> paramIterable = new ArrayList<String>();
        paramIterable.add("n0");
        paramIterable.add("n1");
        paramIterable.add("root");
        graph.newTask().from(paramIterable).foreach(graph.newTask().then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                context.setResult(context.getPreviousResult());
            }
        })).then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                Object[] result = (Object[]) context.getPreviousResult();
                Assert.assertEquals(result.length, 3);
                Assert.assertEquals(result[0], "n0");
                Assert.assertEquals(result[1], "n1");
                Assert.assertEquals(result[2], "root");
            }
        }).execute();

    }


    @Test
    public void testForeach() {
        long[] toTest = {1, 2, 3, 4, 5};
        int[] index = {0};

        graph.newTask().from(toTest).foreachThen(new Callback<Long>() {
            @Override
            public void on(Long object) {
                Assert.assertEquals(toTest[index[0]], (long) object);
                index[0]++;
            }
        }).execute();

        index[0] = 0;
        graph.newTask().fromIndexAll("nodes").foreachThen(new Callback<Node>() {
            @Override
            public void on(Node object) {
                object.set("name", "node" + index[0]);
                index[0]++;
            }
        }).fromIndexAll("nodes").then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                Node[] result = (Node[]) context.getPreviousResult();
                Assert.assertEquals(3, result.length);
                Assert.assertEquals("node0", result[0].get("name"));
                Assert.assertEquals("node1", result[1].get("name"));
                Assert.assertEquals("node2", result[2].get("name"));
            }
        }).execute();
    }

    @Test
    public void testForEachMergeVariables() {
        final int[] index = {0};
        org.mwg.task.Task forEachTask = graph.newTask().then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                context.setVariable("param" + index[0]++, context.getPreviousResult());
                context.setResult(context.getPreviousResult());
            }
        });

        List<String> paramIterable = new ArrayList<String>();
        paramIterable.add("n0");
        paramIterable.add("n1");
        paramIterable.add("root");
        graph.newTask().from(paramIterable).foreach(forEachTask).fromVar("param0").then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                Object result = (String) context.getPreviousResult();
                Assert.assertEquals("n0", result);
            }
        }).fromVar("param1").then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                Object result = (String) context.getPreviousResult();
                Assert.assertEquals("n1", result);
            }
        }).fromVar("param2").then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                Object result = (String) context.getPreviousResult();
                Assert.assertEquals("root", result);
            }
        }).execute();
    }

}
