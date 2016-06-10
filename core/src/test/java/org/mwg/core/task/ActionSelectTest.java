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
        graph.newTask()
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
                .execute();
        removeGraph();
    }

    @Test
    public void test2() {
        initGraph();
        graph.newTask()
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
                .execute();
        removeGraph();
    }

    @Test
    public void test3() {
        initGraph();
        graph.newTask()
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
                .execute();
        removeGraph();
    }


}
