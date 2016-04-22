package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionWorldTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .world(10)
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(context.getWorld(), 10);
                    }
                })
                .execute();
    }

}
