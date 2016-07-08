package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.task.Action;
import org.mwg.task.Actions;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;

import static org.mwg.task.Actions.newTask;

public class ActionIndexNodeTest {

    @Test
    public void testIndexOneNode() {
        Graph graph = new GraphBuilder().build();

        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                newTask()
                        .newNode()
                        .setProperty("name", Type.STRING,"root")
                        .indexNode("indexName","name")
                        .asVar("nodeIndexed")
                        .fromIndexAll("indexName")
                        .then(new Action() {
                            @Override
                            public void eval(TaskContext context) {
                                Assert.assertNotNull(context.result());

                                Node[] nodes = (Node[]) context.result();
                                Node indexedNode = (Node) context.variable("nodeIndexed");

                                Assert.assertEquals(1,nodes.length);
                                Assert.assertEquals(indexedNode.id(),nodes[0].id());
                            }
                        })
                        .execute(graph,null);
            }
        });
    }

    @Test
    public void testIndexComplexArrayOfNodes() {
        Graph graph = new GraphBuilder().build();

        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node[][] complexArray= new Node[3][2];

                for(int i=0;i<3;i++) {
                    for(int j=0;j<2;j++) {
                        complexArray[i][j] = graph.newNode(0,0);
                        complexArray[i][j].set("name","node" + i + j);
                    }
                }

                newTask()
                        .inject(complexArray)
                        .indexNode("indexName","name")
                        .asVar("nodeIndexed")
                        .fromIndexAll("indexName")
                        .then(new Action() {
                            @Override
                            public void eval(TaskContext context) {
                                Assert.assertNotNull(context.result());

                                Node[] nodes = (Node[]) context.result();

                                Assert.assertEquals(6,nodes.length);

                                for(int i=0;i<3;i++) {
                                    for(int j=0;j<2;j++) {
                                        Assert.assertEquals(complexArray[i][j].get("name"),"node" + i + j);
                                    }
                                }

                            }
                        })
                        .execute(graph,null);
            }
        });
    }

    @Test
    public void testIndexNodeIncorrectInput() {
        Graph graph = new GraphBuilder().build();

        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Task withOneIncoorectInput = Actions.newTask()
                        .inject(55)
                        .indexNode("indexName","name");

                Object[][] complexArray= new Object[3][2];

                for(int i=0;i<3;i++) {
                    for(int j=0;j<2;j++) {
                        if(i == 2 && j == 0) {
                            complexArray[i][j] = graph.newNode(0,0);
                            ((Node)complexArray[i][j]).set("name","node" + i + j);
                        } else {
                            complexArray[i][j] = 55;
                        }
                    }
                }

                Task withIncorrectArray = newTask()
                        .inject(complexArray)
                        .indexNode("indexName","name");

                boolean exceptionCaught = false;
                try {
                    withOneIncoorectInput.execute(graph,null);
                } catch (RuntimeException ex) {
                    exceptionCaught = true;
                }
                Assert.assertTrue(exceptionCaught);

                exceptionCaught = false;
                try {
                    withIncorrectArray.execute(graph,null);
                } catch (RuntimeException ex) {
                    exceptionCaught = true;
                }
                Assert.assertTrue(exceptionCaught);

            }
        });
    }
}
