package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionMap;

public class ActionMapTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        graph.newTask()
                .fromIndexAll("nodes")
                .map(new TaskFunctionMap() {
                    @Override
                    public Object map(Node node) {
                        return node.get("name");
                    }
                })
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Object[] names = (Object[]) context.result();
                        Assert.assertEquals(names[0], "n0");
                        Assert.assertEquals(names[1], "n1");
                        Assert.assertEquals(names[2], "root");
                    }
                })
                .execute();
        removeGraph();
    }

}
