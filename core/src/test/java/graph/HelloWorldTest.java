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
                node0.attSet("name", KType.STRING, "MyName");
                System.out.println(node0.att("name"));
                node0.attSet("value", KType.STRING, "MyValue");
                System.out.println(node0.att("name"));
                System.out.println(node0.att("value"));
                node0.attSet("name", KType.STRING, "MyName2");
                System.out.println(node0.att("name"));
                System.out.println(node0.att("value"));

                //destroy the node explicitly without waiting GC
                node0.free();

            }
        });


    }

}
