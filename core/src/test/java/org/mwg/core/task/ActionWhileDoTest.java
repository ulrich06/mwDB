package org.mwg.core.task;


import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.task.*;

import static org.mwg.task.Actions.*;

public class ActionWhileDoTest extends AbstractActionTest {

    @Test
    public void test() {
        initComplexGraph(new Callback<Node>() {
            @Override
            public void on(Node root) {

                Task whiletask = newTask().inject(root).asVar("parent").whileDo(new TaskFunctionConditional() {
                    @Override
                    public boolean eval(TaskContext context) {
                        //System.out.println("condition");
                        return false;
                    }
                }, then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        //System.out.println("not printed");
                        context.continueTask();

                    }
                })).then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        //System.out.println("outside");
                        context.continueTask();
                    }
                });


                whiletask.execute(graph, new Callback<TaskResult>() {
                    @Override
                    public void on(TaskResult result) {
                        //System.out.println("end");
                    }
                });




            }
        });




    }
}