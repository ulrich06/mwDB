package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionWithTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .fromIndexAll("nodes")
                .selectWith("name", "n0")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[0].att("name"), "n0");
                    }
                })
                .execute();

        graph.newTask()
                .fromIndexAll("nodes")
                .selectWith("name", "n.*")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[0].att("name"), "n0");
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[1].att("name"), "n1");
                    }
                })
                .execute();

    }

}
