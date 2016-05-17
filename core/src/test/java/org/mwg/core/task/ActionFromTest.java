package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class ActionFromTest extends AbstractActionTest {

    @Test
    public void test() {
        graph.newTask()
                .from("uselessPayload")
                .then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.getPreviousResult(), "uselessPayload");
                    }
                })
                .execute();
    }

    @Test
    public void testFromNodes() {
        graph.all(0, 0, "nodes", new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                Assert.assertEquals(3,result.length);

                String[] expected = new String[]{(String) result[0].get("name"),
                        (String) result[1].get("name"),
                        (String) result[2].get("name")};

                graph.newTask()
                        .from(result)
                        .then(new TaskAction() {
                            @Override
                            public void eval(TaskContext context) {
                                //empty task
                            }
                        })
                        .execute();

                String[] resultName = new String[3];
                try {
                    int i = 0;
                    for(Node n : result) {
                        resultName[i] = (String) n.get("name");
                        i++;
                    }
                } catch (Exception e) {
                    resultName[0] = "fail";
                }

                Assert.assertArrayEquals(expected,resultName);
            }
        });
    }

    @Test
    public void testFromNode() {
        graph.find(0, 0, "roots", "name=root", new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                Assert.assertEquals(1,result.length);

                graph.newTask()
                        .from(result[0])
                        .then(new TaskAction() {
                            @Override
                            public void eval(TaskContext context) {
                                //empty task
                            }
                        })
                        .execute();
                String name = "";
                try {
                    name = (String) result[0].get("name");
                } catch (Exception e) {
                    name = "fail";
                }

                Assert.assertEquals("root",name);
            }
        });
    }


}
