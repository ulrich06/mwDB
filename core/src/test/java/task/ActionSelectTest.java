package task;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.KNode;
import org.mwdb.KTask;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

public class ActionSelectTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .fromIndexAll("nodes")
                .select(new KTask.KTaskSelect() {
                    @Override
                    public boolean select(KNode node) {
                        return node.att("name").equals("root");
                    }
                })
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult())[0].att("name"), "root");
                    }
                })
                .execute();
    }

    @Test
    public void test2() {
        graph.newTask()
                .fromIndexAll("nodes")
                .select(new KTask.KTaskSelect() {
                    @Override
                    public boolean select(KNode node) {
                        return false;
                    }
                })
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult()).length, 0);
                    }
                })
                .execute();
    }

    @Test
    public void test3() {
        graph.newTask()
                .fromIndexAll("nodes")
                .select(new KTask.KTaskSelect() {
                    @Override
                    public boolean select(KNode node) {
                        return true;
                    }
                })
                .then(new KTaskAction() {
                    @Override
                    public void eval(KTaskContext context) {
                        Assert.assertEquals(((KNode[]) context.getPreviousResult()).length, 3);
                    }
                })
                .execute();
    }



}
