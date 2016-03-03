package graph;

import org.junit.Test;
import org.mwdb.*;

public class HelloWorldTest {

    @Test
    public void test() {
        KGraph graph = GraphBuilder.builder().buildGraph();
        graph.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KNode node0 = graph.createNode(0, 0);
                //do something with the node
                node0.attSet("name", KType.STRING, "MyValue");

                System.out.println(node0.att("name"));

                //destroy it explicitly without waiting GC
                node0.free();

            }
        });


    }

}
