package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.TaskAction;
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
                        return node.get("name").equals("root");
                    }
                })
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.getPreviousResult())[0].get("name"), "root");
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
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.getPreviousResult()).length, 0);
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
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.getPreviousResult()).length, 3);
                    }
                })
                .execute();
        removeGraph();
    }


}
