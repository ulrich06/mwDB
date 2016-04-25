package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionFromTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .from("uselessPayload")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(context.getPreviousResult(), "uselessPayload");
                    }
                })
                .execute();
    }

}
