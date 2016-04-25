package org.mwg.core.task;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mwg.*;
import org.mwg.core.NoopScheduler;

public abstract class AbstractActionTest {

    protected Graph graph;

    @Before
    public void initGraph() {
        graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create graph nodes
                Node n0 = graph.newNode(0, 0);
                n0.setProperty("name", Type.STRING, "n0");
                n0.setProperty("value", Type.INT, 8);

                Node n1 = graph.newNode(0, 0);
                n1.setProperty("name", Type.STRING, "n1");
                n1.setProperty("value", Type.INT, 3);

                Node root = graph.newNode(0, 0);
                root.setProperty("name", Type.STRING, "root");
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

    @After
    public void removeGraph() {
        graph.disconnect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Assert.assertEquals("Error during graph disconnection",true,result);
            }
        });
    }
    
}
