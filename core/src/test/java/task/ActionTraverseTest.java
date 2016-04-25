package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KNode;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionTraverseTest extends AbstractActionTest {

    @Test
    public void test() {
        graph
                .newTask()
                .fromIndexAll("nodes")
                .traverse("children")
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        KNode[] lastResult = (KNode[]) context.getPreviousResult();
                        Assert.assertEquals(lastResult[0].att("name"), "n0");
                        Assert.assertEquals(lastResult[1].att("name"), "n1");
                    }
                })
                .execute();
    }

}
