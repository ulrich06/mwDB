package org.mwg.core.task;

import org.junit.Assert;
import org.mwg.*;
import org.mwg.core.scheduler.NoopScheduler;

public abstract class AbstractActionTest {

    protected Graph graph;

    protected void initGraph() {
        graph = new GraphBuilder().withScheduler(new NoopScheduler()).build();
        final AbstractActionTest selfPointer = this;
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create graph nodes
                Node n0 = selfPointer.graph.newNode(0, 0);
                n0.setProperty("name", Type.STRING, "n0");
                n0.setProperty("value", Type.INT, 8);

                Node n1 = selfPointer.graph.newNode(0, 0);
                n1.setProperty("name", Type.STRING, "n1");
                n1.setProperty("value", Type.INT, 3);

                Node root = selfPointer.graph.newNode(0, 0);
                root.setProperty("name", Type.STRING, "root");
                root.add("children", n0);
                root.add("children", n1);

                //create some index
                selfPointer.graph.index("roots", root, "name", null);
                selfPointer.graph.index("nodes", n0, "name", null);
                selfPointer.graph.index("nodes", n1, "name", null);
                selfPointer.graph.index("nodes", root, "name", null);

            }
        });
    }


    protected void removeGraph() {
        graph.disconnect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Assert.assertEquals(true, result);
            }
        });
    }

}
