package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionWithoutTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .fromIndexAll("nodes")
                .selectWithout("name", "n0")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[0].att("name"), "n1");
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[1].att("name"), "root");
                    }
                })
                .execute();

        graph.newTask()
                .fromIndexAll("nodes")
                .selectWithout("name", "n.*")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[0].att("name"), "root");
                    }
                })
                .execute();

    }

}
