package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.from;

public class ActionFromIndexTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        from("uselessPayload")
                .fromIndex("nodes", "name=n0")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(((Node[]) context.result())[0].get("name"), "n0");
                        Assert.assertEquals(((Node[]) context.result()).length, 1);
                    }
                })
                .execute(graph);
        removeGraph();
    }

}
