package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.utility.PrimitiveHelper;

public class HelloWorldTest {

    @Test
    public void mwHeapTest() {
        test(GraphBuilder.builder().buildGraph());
    }

    private void test(KGraph graph) {
        graph.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KNode node0 = graph.createNode(0, 0);
                //do something with the node

                node0.attSet("name", KType.STRING, "MyName");
                Assert.assertTrue(PrimitiveHelper.equals("MyName", node0.att("name").toString()));

                node0.attSet("value", KType.STRING, "MyValue");
                Assert.assertTrue(PrimitiveHelper.equals("MyValue", node0.att("value").toString()));
                //check that other attribute name is not affected
                Assert.assertTrue(PrimitiveHelper.equals("MyName", node0.att("name").toString()));

                node0.attSet("name", KType.STRING, "MyName2");
                Assert.assertTrue(PrimitiveHelper.equals("MyName2", node0.att("name").toString()));
                Assert.assertTrue(PrimitiveHelper.equals("MyValue", node0.att("value").toString()));

                //destroy the node explicitly without waiting GC
                node0.free();

            }
        });

    }

}
