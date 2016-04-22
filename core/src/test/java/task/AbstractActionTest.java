package task;

import org.mwdb.*;
import org.mwdb.manager.NoopScheduler;

public abstract class AbstractActionTest {

    protected KGraph graph;

    public AbstractActionTest() {
        graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).build();
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

            }
        });
    }
    
}
