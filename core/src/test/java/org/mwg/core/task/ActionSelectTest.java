package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelect;

public class ActionSelectTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        new CoreTask()
                .fromIndexAll("nodes")
                .select(new TaskFunctionSelect() {
                    @Override
                    public boolean select(Node node) {
                        return PrimitiveHelper.equals(node.get("name").toString(), "root");
                    }
                })
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.result())[0].get("name"), "root");
                    }
                })
                .execute(graph);
        removeGraph();
    }

    @Test
    public void test2() {
        initGraph();
        new CoreTask()
                .fromIndexAll("nodes")
                .select(new TaskFunctionSelect() {
                    @Override
                    public boolean select(Node node) {
                        return false;
                    }
                })
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.result()).length, 0);
                    }
                })
                .execute(graph);
        removeGraph();
    }

    @Test
    public void test3() {
        initGraph();
        new CoreTask()
                .fromIndexAll("nodes")
                .select(new TaskFunctionSelect() {
                    @Override
                    public boolean select(Node node) {
                        return true;
                    }
                })
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.result()).length, 3);
                    }
                })
                .execute(graph);
        removeGraph();
    }


}
