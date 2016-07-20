package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.task.*;

import static org.mwg.task.Actions.newTask;
import static org.mwg.task.Actions.setTime;

/**
 * Created by assaad on 20/07/16.
 */
public class FlagTest {
    @Test
    public void traverse() {
        Graph graph = new GraphBuilder()
                .withMemorySize(30000)
                .saveEvery(10000)
                .withScheduler(new NoopScheduler())
                .build();

        graph.connect(result -> {

            String relName = "children";

            Node n1 = graph.newNode(0, 13);
            graph.save(null);
            long initcache = graph.space().available();


            Node n2 = graph.newNode(0, 13);
            Node n3 = graph.newNode(0, 13);
            Node n4 = graph.newNode(0, 13);

            n1.add(relName, n2);
            n1.add(relName, n3);
            n1.add(relName, n4);


            Node n5 = graph.newNode(0, 13);
            Node n6 = graph.newNode(0, 13);
            n2.add(relName, n5);
            n2.add(relName, n6);

            Node n7 = graph.newNode(0, 13);
            Node n8 = graph.newNode(0, 13);
            n3.add(relName, n7);
            n3.add(relName, n8);

            Node n9 = graph.newNode(0, 13);
            Node n10 = graph.newNode(0, 13);
            n4.add(relName, n9);
            n4.add(relName, n10);

            n2.free();
            n3.free();
            n4.free();
            n5.free();
            n6.free();
            n7.free();
            n8.free();
            n9.free();
            n10.free();

            graph.save(null);
            Assert.assertTrue(graph.space().available() == initcache);


            Task traverse = newTask();

            traverse.asVar("parent").traverse(relName).then(new Action() {
                @Override
                public void eval(TaskContext context) {

                    TaskResult<Node> children = context.resultAsNodes();
                    if (children.size() != 0) {
                        context.continueWith(context.wrap(graph.cloneNode(children.get(0))));
                    } else {
                        context.continueWith(null);
                    }
                }
            }).ifThen(new TaskFunctionConditional() {
                @Override
                public boolean eval(TaskContext context) {
                    return (context.result() != null);
                }
            }, traverse);


            Task mainTask = setTime("13").setWorld("0").inject(n1).executeSubTask(traverse);
            mainTask.execute(graph, new Callback<TaskResult>() {
                @Override
                public void on(TaskResult result) {
                    graph.save(null);
                    Assert.assertTrue(graph.space().available() == initcache);
                    if (result != null) {
                        result.free();
                    }
                }

            });

            graph.save(null);
            Assert.assertTrue(graph.space().available() == initcache);
        });
    }


    @Test
    public void traverseOrKeep(){
        Graph graph = new GraphBuilder()
                .withMemorySize(30000)
                .saveEvery(10000)
                .withScheduler(new NoopScheduler())
                .build();

        graph.connect(result -> {

            String relName = "children";

            Node n1 = graph.newNode(0, 13);
            graph.save(null);
            long initcache = graph.space().available();


            Node n2 = graph.newNode(0, 13);
            Node n3 = graph.newNode(0, 13);
            Node n4 = graph.newNode(0, 13);

            n1.add(relName, n2);
            n1.add(relName, n3);
            n1.add(relName, n4);


            Node n5 = graph.newNode(0, 13);
            Node n6 = graph.newNode(0, 13);
            n2.add(relName, n5);
            n2.add(relName, n6);

            Node n7 = graph.newNode(0, 13);
            Node n8 = graph.newNode(0, 13);
            n3.add(relName, n7);
            n3.add(relName, n8);

            Node n9 = graph.newNode(0, 13);
            Node n10 = graph.newNode(0, 13);
            n4.add(relName, n9);
            n4.add(relName, n10);

            n2.free();
            n3.free();
            n4.free();
            n5.free();
            n6.free();
            n7.free();
            n8.free();
            n9.free();
            n10.free();

            graph.save(null);
            Assert.assertTrue(graph.space().available() == initcache);



            Task traverse = newTask();

            traverse.asVar("parent").traverseOrKeep(relName).then(new Action() {
                @Override
                public void eval(TaskContext context) {
                    TaskResult<Integer> count = context.variable("count");
                    int c=0;
                    if(count!=null){
                        c=(int)count.get(0)+1;
                    }
                    context.setVariable("count",context.wrap(c));

                    TaskResult<Node> children = context.resultAsNodes();
                    if (children.size() != 0) {
                        context.continueWith(context.wrap(graph.cloneNode(children.get(0))));
                    } else {
                        context.continueWith(null);
                    }
                }
            }).ifThen(new TaskFunctionConditional() {
                @Override
                public boolean eval(TaskContext context) {
                    int x=(int)context.variable("count").get(0);
                    return (x!=3);
                }
            }, traverse);


            Task mainTask = setTime("13").setWorld("0").inject(n1).executeSubTask(traverse);
            mainTask.execute(graph, new Callback<TaskResult>() {
                @Override
                public void on(TaskResult result) {
                    graph.save(null);
                   // System.out.println(graph.space().available() + " , "+ initcache);
                    Assert.assertTrue(graph.space().available() == initcache);
                    if (result != null) {
                        result.free();
                    }
                }

            });

            graph.save(null);
            Assert.assertTrue(graph.space().available() == initcache);
        });
    }
}
