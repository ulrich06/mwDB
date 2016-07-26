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


                Task whiletask = newTask().inject(root).whileDo(new TaskFunctionConditional() {
                    @Override
                    public boolean eval(TaskContext context) {
                        System.out.println("condition while");
                        return context.result().size() != 0;
                    }
                }, foreach(ifThenElse(new TaskFunctionConditional() {
                            @Override
                            public boolean eval(TaskContext context) {
                                System.out.println("condition if");
                                Node res= (Node) context.result().get(0);
                                long[] c= (long[]) res.get("child");
                                return (c!=null && c.length>0);
                            }
                        }, traverse("child")
                        , then(new Action() {
                            @Override
                            public void eval(TaskContext context) {
                                System.out.println("if is false");
                                context.addToGlobalVariable("leaves",context.result());
                                context.continueWith(null);
                            }
                        })))
                ).then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        System.out.println("done with "+context.result().size());
                        context.continueTask();
                    }
                });


                whiletask.execute(graph, new Callback<TaskResult>() {
                    @Override
                    public void on(TaskResult result) {
                        System.out.println("end "+result.size());
                    }
                });


            }
        });


    }
}