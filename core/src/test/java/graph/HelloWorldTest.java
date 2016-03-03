package graph;

import org.junit.Test;
import org.mwdb.GraphBuilder;
import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.KNode;

public class HelloWorldTest {

    @Test
    public void test() {
        KGraph graph = GraphBuilder.builder().buildGraph();
        graph.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KNode node0 = graph.createNode(0, 0);

            }
        });


    }

}
