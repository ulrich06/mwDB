package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.*;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;

/**
 * Created by ludovicmouline on 26/04/16.
 */
public class ActionIfThenTest extends AbstractActionTest{

    @Test
    public void test() {
        initGraph();
        boolean[] result = {false,false};

        Task modifyResult0 = graph.newTask()
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        result[0] = true;
                    }
                });

        Task modifyResult1 = graph.newTask()
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        result[0] = true;
                    }
                });


        graph.newTask()
                .ifThen(new TaskFunctionConditional() {
                    @Override
                    public boolean eval(TaskContext context) {
                        return true;
                    }
                },modifyResult0).execute();

        graph.newTask().ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return false;
            }
        },modifyResult0).execute();

        Assert.assertEquals(true,result[0]);
        Assert.assertEquals(false,result[1]);
        removeGraph();
    }

    @Test
    public void testChainAfterIfThen() {
        initGraph();
        Task addVarInContext = graph.newTask().from(5).asVar("variable").then(new Action() {
            @Override
            public void eval(TaskContext context) {
                //empty action
            }
        });

        graph.newTask().ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return true;
            }
        },addVarInContext).fromVar("variable").then(new Action() {
            @Override
            public void eval(TaskContext context) {
                Integer val = (Integer) context.getPreviousResult();
                Assert.assertEquals(5,(int)val);
            }
        }).execute();
        removeGraph();
    }

    @Test
    public void accessContextVariableInThenTask() {
        initGraph();
        Task accessVar = graph.newTask().then(new Action() {
            @Override
            public void eval(TaskContext context) {
                Integer variable = (Integer) context.getVariable("variable");
                Assert.assertEquals(5,(int)variable);
            }
        });

        graph.newTask().from(5).asVar("variable").ifThen(new TaskFunctionConditional() {
            @Override
            public boolean eval(TaskContext context) {
                return true;
            }
        },accessVar).execute();
        removeGraph();
    }
}
