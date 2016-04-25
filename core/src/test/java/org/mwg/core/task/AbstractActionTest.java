package org.mwg.core.task;

import org.mwg.*;
import org.mwg.core.NoopScheduler;

public abstract class AbstractActionTest {

    protected Graph graph;

    public AbstractActionTest() {
        graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).build();
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

            }
        });
    }
    
}
