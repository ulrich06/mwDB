package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.newTask;

public class CoreTaskContextTests {

    @Test
    public void testArrayInTemplate() {
        Graph graph = new GraphBuilder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                newTask()
                        .setVar("array",new int[]{1,2,3,4,5,6,7,8,9})
                        .fromVar("array")
                        .then(new Action() {
                            @Override
                            public void eval(TaskContext context) {
                                Assert.assertEquals("5",context.template("{{array[4]}}"));
                                Assert.assertEquals("9",context.template("{{result[8]}}"));
                                Assert.assertEquals("[1,2,3,4,5,6,7,8,9]",context.template("{{result}}"));
                                Assert.assertEquals("[1,2,3,4,5,6,7,8,9]",context.template("{{array}}"));

                                boolean exceptionCaught = false;
                                try {
                                    context.template("{{result[]}}");
                                } catch (RuntimeException e) {
                                    exceptionCaught = true;
                                }
                                Assert.assertTrue(exceptionCaught);

                                exceptionCaught = false;
                                try {
                                    System.out.println(context.template("{{result[9]}}"));;
                                } catch (RuntimeException e) {
                                    exceptionCaught = true;
                                }
                                Assert.assertTrue(exceptionCaught);


                            }
                        })
                        .execute(graph,null);
            }
        });
    }
}
