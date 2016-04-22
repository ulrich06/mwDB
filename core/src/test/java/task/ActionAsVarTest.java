package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;

public class ActionAsVarTest extends AbstractActionTest {

    @Test
    public void test() {
        graph
                .newTask()
                .from("hello")
                .asVar("myVar")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(context.getPreviousResult(), "hello");
                        Assert.assertEquals(context.getVariable("myVar"), "hello");
                    }
                })
                .execute();
    }

}
