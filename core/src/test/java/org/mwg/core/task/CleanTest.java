package org.mwg.core.task;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

/**
 * Created by ludovicmouline on 17/05/16.
 */
public class CleanTest {

    @Test
    public void test() {
        TaskContext[] retention = new TaskContext[1];

        Graph graph = GraphBuilder.builder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                graph.newTask().then(new TaskAction() {
                    @Override
                    public void eval(TaskContext context) {
                        retention[0] = context;
                        Node node = graph.newNode(0,0);
                        node.set("name","node");

                        context.setResult(node);
                    }
                }).execute();
            }
        });

        System.out.println(retention[0]);
    }
}
