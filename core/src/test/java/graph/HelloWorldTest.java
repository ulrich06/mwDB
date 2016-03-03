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

                //check the simple json print
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName2\",\"value\": \"MyValue\"}}", node0.toString()));

                //Create a new node
                KNode node1 = graph.createNode(0, 0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {}}", node1.toString()));

                //attach the new node
                node1.refAdd("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1]}}", node1.toString()));

                node1.refAdd("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,1]}}", node1.toString()));

                KNode node2 = graph.createNode(0, 0);
                node1.refAdd("children", node2);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,1,3]}}", node1.toString()));

                
                //destroy the node explicitly without waiting GC
                node0.free();

            }
        });

    }

}

