package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.ArrayList;
import java.util.List;

public class ActionForeachTest extends AbstractActionTest {

    @Test
    public void test() {

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

}
