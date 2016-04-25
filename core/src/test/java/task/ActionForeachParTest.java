package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

import java.util.ArrayList;
import java.util.List;

public class ActionForeachParTest extends AbstractActionTest {

    @Test
    public void test() {

        final long[] i = {0};
        graph.newTask()
                .from(new long[]{1, 2, 3})
                .foreachPar(graph.newTask().then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        i[0]++;
                        Assert.assertEquals(context.getPreviousResult(), i[0]);
                        context.setResult(context.getPreviousResult());//propagate result
                    }
                }))
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Object[] result = (Object[]) context.getPreviousResult();
                        Assert.assertEquals(result.length, 3);
                        Assert.assertEquals(result[0], 1l);
                        Assert.assertEquals(result[1], 2l);
                        Assert.assertEquals(result[2], 3l);
                    }
                })
                .execute();

        graph.newTask().fromIndexAll("nodes").foreachPar(graph.newTask().then(new KTaskAction() {
            @Override
            public void eval(KTaskContext context) {
                context.setResult(context.getPreviousResult());
            }
        })).then(new KTaskAction() {
            @Override
            public void eval(KTaskContext context) {
                Object[] result = (Object[]) context.getPreviousResult();
                Assert.assertEquals(result.length, 3);
                Assert.assertEquals(((KNode) result[0]).att("name"), "n0");
                Assert.assertEquals(((KNode) result[1]).att("name"), "n1");
                Assert.assertEquals(((KNode) result[2]).att("name"), "root");
            }
        }).execute();


        List<String> paramIterable = new ArrayList<String>();
        paramIterable.add("n0");
        paramIterable.add("n1");
        paramIterable.add("root");
        graph.newTask().from(paramIterable).foreachPar(graph.newTask().then(new KTaskAction() {
            @Override
            public void eval(KTaskContext context) {
                context.setResult(context.getPreviousResult());
            }
        })).then(new KTaskAction() {
            @Override
            public void eval(KTaskContext context) {
                Object[] result = (Object[]) context.getPreviousResult();
                Assert.assertEquals(result.length, 3);
                Assert.assertEquals(result[0], "n0");
                Assert.assertEquals(result[1], "n1");
                Assert.assertEquals(result[2], "root");
            }
        }).execute();

    }

}
