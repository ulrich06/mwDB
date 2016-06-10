package ml;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.task.*;
import org.mwg.core.scheduler.NoopScheduler;

import java.util.Arrays;

/**
 * Created by assaad on 10/05/16.
 */
public class TestTMP {


    public static void main(String[] args) {
        Graph graph = new GraphBuilder().withScheduler(new NoopScheduler()).build();

        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node root = graph.newNode(0,0);
                root.set("name","root");
                root.set("enter",true);

                final long id = root.id();

                Node n1 = graph.newNode(0,0);
                n1.set("name","n1");
                n1.set("enter",true);

                Node n2 = graph.newNode(0,0);
                n2.set("name","n2");
                n2.set("enter",false);


                Node n3 = graph.newNode(0,0);
                n3.set("name","n3");
                n3.set("enter",false);


                root.add("fils",n1);
                root.add("fils",n2);
                root.add("fils",n3);

                Node n4 = graph.newNode(0,0);
                n4.set("name","n4");
                n4.set("enter",true);


                n1.add("fils",n4);

                Task creationTask = graph.newTask().then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Node node = (Node) context.variable("starterNode");
                        System.out.println("Creation: " + node);
                    }
                });


                final int[] recursionNb = new int[]{0};
                Task traverse = graph.newTask();
                traverse.then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        recursionNb[0]++;
                    }
                }).fromVar("starterNode").traverse("fils").select(new TaskFunctionSelect() {
                    @Override
                    public boolean select(Node node) {
                        return (boolean) node.get("enter");
                    }
                }).asVar("childNode")
                 .ifThen(new TaskFunctionConditional() {
                    @Override
                    public boolean eval(TaskContext context) {
                        Node[] result = (Node[]) context.variable("childNode");

                        if(result.length > 0) {
                            context.setVariable("starterNode",result[0]);
                            Node starter = (Node) context.variable("starterNode");
                            System.out.println(recursionNb[0] + " 1er ifThen " + starter + " -> " + Arrays.toString(result));

                        }
                        return result.length > 0;
                    }
                },traverse);
                /*.ifThen(new TaskFunctionConditional() {
                    @Override
                    public boolean eval(TaskContext context) {


                        Node[] result = (Node[]) context.variable("childNode");

                        if(result.length == 0) {
                            Node starter = (Node) context.variable("starterNode");
                            System.out.println(recursionNb[0] + " 2nd ifThen " + starter);
                        }
                        return result.length == 0;
                    }
                },creationTask);*/

                Task mainTask = graph.newTask().from(root).asVar("starterNode").executeSubTask(traverse).then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        //call callbask
                    }
                }).executeSubTask(creationTask);

                mainTask.execute();









            }
        });
    }
}
