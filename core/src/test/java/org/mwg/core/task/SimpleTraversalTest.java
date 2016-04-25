package org.mwg.core.task;

import org.junit.Test;
import org.mwg.*;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

public class SimpleTraversalTest {

    @Test
    public void test() {
        Graph graph = GraphBuilder.builder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create graph nodes
                Node n0 = graph.newNode(0, 0);
                n0.set("name", Type.STRING, "n0");
                n0.set("value", Type.INT, 8);

                Node n1 = graph.newNode(0, 0);
                n1.set("name", Type.STRING, "n1");
                n1.set("value", Type.INT, 3);

                Node root = graph.newNode(0, 0);
                root.set("name", Type.STRING, "root");
                root.add("children", n0);
                root.add("children", n1);

                //create some index
                graph.index("roots", root, new String[]{"name"}, null);
                graph.index("nodes", n0, new String[]{"name"}, null);
                graph.index("nodes", n1, new String[]{"name"}, null);
                graph.index("nodes", root, new String[]{"name"}, null);

                /*
                Task task = graph.newTask();
                task
                        .from(new Node[]{})
                        .fromIndexAll("nodes")
                        .selectWith("name", "n.*")
                        .selectWithout("name", "n0")
                        .select(node -> true)
                        .count()
                        .wait(subTask)
                        .as("t1")
                        .wait(subTask2)
                        .as("t2")
                        .fromVar("t1")
                        .then(new Callback<Node[]>() {
                            @Override
                            public void on(Node[] result) {

                            }
                        });
                task.execute();
*/


                /*
                Task traversal2 = graph.newTask();
                traversal2.fromIndexAll("roots")
                        .as("x")
                        .traverseIndex("children")
                        .selectWith("name", ".*0.*")
                        .count()
                        .as("nbChildren")
                        .fromVar("x")
                        .from(3)
                        .from(new long[]{10, 20, 30})

                        .wait(graph.newTask().fromIndexAll("roots")).as("sub_0")
                        .wait(graph.newTask().fromIndexAll("nodes").count()).as("sub_1")
                        .then(new TaskAction() {
                            @Override
                            public void eval(TaskContext context) {

                                System.out.println(context.getPreviousResult());
                                System.out.println(context.getVariable("x"));
                            }
                        })
                        .execute();
*/

                Task hello = graph.newTask();
                hello.thenAsync(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {

                        new Callback(){

                            @Override
                            public void on(Object result) {
                                context.setResult("Hello");
                                context.next();
                            }
                        };


                    }
                });
                hello.traverse("children").then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        //TODO
                        context.getVariable("x");
                    }
                });

                Task traversal = graph.newTask();
                traversal
                        .from(new long[]{1, 2, 3})
                        .asVar("x")

                        .foreach(hello)

                        .foreach(graph.newTask().asVar("sub").fromVar("sub"))
                        .executeThen(new TaskAction() {
                            @Override
                            public void eval(TaskContext context) {
                                System.out.println(context);
                            }
                        });

                graph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {

                    }
                });
            }
        });

    }


}
