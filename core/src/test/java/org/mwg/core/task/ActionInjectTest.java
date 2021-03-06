package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.inject;

public class ActionInjectTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        inject("uselessPayload")
                .then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(context.resultAsStrings().get(0), "uselessPayload");
                    }
                })
                .execute(graph, null);
        removeGraph();
    }

    @Test
    public void testFromNodes() {
        initGraph();
        final ActionInjectTest selfPointer = this;
        graph.findAll(0, 0, "nodes", new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                Assert.assertEquals(3, result.length);

                String[] expected = new String[]{(String) result[0].get("name"),
                        (String) result[1].get("name"),
                        (String) result[2].get("name")};

                inject(result)
                        .then(new Action() {
                            @Override
                            public void eval(TaskContext context) {
                                //empty task
                            }
                        })
                        .execute(selfPointer.graph, null);

                String[] resultName = new String[3];
                try {
                    int i = 0;
                    for (Node n : result) {
                        resultName[i] = (String) n.get("name");
                        i++;
                    }
                } catch (Exception e) {
                    resultName[0] = "fail";
                    e.printStackTrace();
                }

                Assert.assertArrayEquals(expected, resultName);
            }
        });
        removeGraph();
    }

    @Test
    public void testFromNode() {
        initGraph();
        final ActionInjectTest selfPointer = this;
        graph.find(0, 0, "roots", "name=root", new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                Assert.assertEquals(1, result.length);

                inject(result[0])
                        .then(new Action() {
                            @Override
                            public void eval(TaskContext context) {
                                //empty task
                            }
                        })
                        .execute(graph, null);
                String name;
                try {
                    name = (String) result[0].get("name");
                } catch (Exception e) {
                    name = "fail";
                }

                Assert.assertEquals("root", name);
            }
        });
        removeGraph();
    }


}
