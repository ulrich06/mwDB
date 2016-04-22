package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionTimeTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .time(10)
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(context.getTime(), 10);
                    }
                })
                .execute();
    }

}
