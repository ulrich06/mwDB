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
                        Assert.assertEquals(context.resultAsNodes().get(0).get("name"), "root");
                    }
                })
                .execute(graph,null);
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
                        Assert.assertEquals(context.result().size(), 0);
                    }
                })
                .execute(graph,null);
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
                        Assert.assertEquals(context.result().size(), 3);
                    }
                })
                .execute(graph,null);
        removeGraph();
    }

}
