package task;

import org.junit.Test;
import org.mwdb.*;

public class SimpleTraversalTest {

    @Test
    public void test() {
        KGraph graph = GraphBuilder.builder().build();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create graph nodes
                KNode n0 = graph.newNode(0, 0);
                n0.attSet("name", KType.STRING, "n0");
                n0.attSet("value", KType.INT, 8);

                KNode n1 = graph.newNode(0, 0);
                n1.attSet("name", KType.STRING, "n1");
                n1.attSet("value", KType.INT, 3);

                KNode root = graph.newNode(0, 0);
                root.attSet("name", KType.STRING, "root");
                root.relAdd("children", n0);
                root.relAdd("children", n1);

                //create some index
                graph.index("roots", root, new String[]{"name"}, null);
                graph.index("nodes", n0, new String[]{"name"}, null);
                graph.index("nodes", n1, new String[]{"name"}, null);
                graph.index("nodes", root, new String[]{"name"}, null);

                /*
                KTask task = graph.newTask();
                task
                        .input(new KNode[]{})
                        .globalAll("nodes")
                        .with("name", "n.*")
                        .without("name", "n0")
                        .filter(node -> true)
                        .count()
                        .wait(subTask)
                        .as("t1")
                        .wait(subTask2)
                        .as("t2")
                        .from("t1")
                        .then(new KCallback<KNode[]>() {
                            @Override
                            public void on(KNode[] result) {

                            }
                        });
                task.execute();
*/


                /*
                KTask traversal2 = graph.newTask();
                traversal2.globalAll("roots")
                        .as("x")
                        .relation("children")
                        .with("name", ".*0.*")
                        .count()
                        .as("nbChildren")
                        .from("x")
                        .input(3)
                        .input(new long[]{10, 20, 30})

                        .wait(graph.newTask().globalAll("roots")).as("sub_0")
                        .wait(graph.newTask().globalAll("nodes").count()).as("sub_1")
                        .then(new KTaskAction() {
                            @Override
                            public void eval(KTaskContext context) {

                                System.out.println(context.getPreviousResult());
                                System.out.println(context.getVariable("x"));
                            }
                        })
                        .execute();
*/

                KTask traversal = graph.newTask();
                traversal
                        .input(new long[]{1, 2, 3})
                        .as("x")
                        .foreach(graph.newTask().as("sub").from("x"))
                        .execute();

                graph.disconnect(new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean result) {

                    }
                });
            }
        });

    }


}
