package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionConditional;

import static org.mwg.task.Actions.*;

public class ActionIfThenTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        final boolean[] result = {false, false};

        Task modifyResult0 = then(new Action() {
            @Override
            public void eval(TaskContext context) {
                result[0] = true;
            }
        });

        Task modifyResult1 = then(new Action() {
            @Override
            public void eval(TaskContext context) {
                result[0] = true;
            }
        });

        ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return true;
            }
        }, modifyResult0).execute(graph, null);

        ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return false;
            }
        }, modifyResult0).execute(graph, null);

        Assert.assertEquals(true, result[0]);
        Assert.assertEquals(false, result[1]);
        removeGraph();
    }

    @Test
    public void testChainAfterIfThen() {
        initGraph();
        Task addVarInContext = inject(5).asGlobalVar("variable").then(new Action() {
            @Override
            public void eval(TaskContext context) {
                context.continueTask();
                //empty action
            }
        });

        ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return true;
            }
        }, addVarInContext).fromVar("variable").then(new Action() {
            @Override
            public void eval(TaskContext context) {
                Integer val = (Integer) context.result().get(0);
                Assert.assertEquals(5, (int) val);
            }
        }).execute(graph, null);
        removeGraph();
    }

    @Test
    public void accessContextVariableInThenTask() {
        initGraph();
        Task accessVar = then(new Action() {
            @Override
            public void eval(TaskContext context) {
                Integer variable = (Integer) context.variable("variable").get(0);
                Assert.assertEquals(5, (int) variable);
                context.continueTask();
            }
        });

        inject(5).asGlobalVar("variable").ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return true;
            }
        }, accessVar).execute(graph, null);
        removeGraph();
    }
}
