package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.offheap.OffHeapChunkSpace;
import org.mwdb.manager.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;

public class HelloWorldTest {

    @Test
    public void heapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph());
    }

    @Test
    public void offHeapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withSpace(new OffHeapChunkSpace(10000, 20)).buildGraph());
    }

    private void test(KGraph graph) {
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean o) {

                KNode node0 = graph.newNode(0, 0);
                //do something with the node
                graph.lookup(0, 0, node0.id(), new KCallback<KNode>() {
                    @Override
                    public void on(KNode result) {
                        Assert.assertTrue(result.id() == node0.id());
                    }
                });


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

                String flatNode0 = "{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName2\",\"value\": \"MyValue\"}}";

                Assert.assertTrue(flatNode0.length() == node0.toString().length());
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName2\",\"value\": \"MyValue\"}}", node0.toString()));

                //Create a new node
                KNode node1 = graph.newNode(0, 0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {}}", node1.toString()));

                //attach the new node
                node1.relAdd("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1]}}", node1.toString()));

                node1.relAdd("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,1]}}", node1.toString()));

                KNode node2 = graph.newNode(0, 0);
                node1.relAdd("children", node2);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,1,3]}}", node1.toString()));

                long[] refValuesThree = node1.relValues("children");
                Assert.assertTrue(refValuesThree.length == 3);
                Assert.assertTrue(refValuesThree[0] == 1);
                Assert.assertTrue(refValuesThree[1] == 1);
                Assert.assertTrue(refValuesThree[2] == 3);

                node1.rel("children", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] resolvedNodes) {
                        Assert.assertTrue(resolvedNodes[0].id() == 1);
                        Assert.assertTrue(resolvedNodes[1].id() == 1);
                        Assert.assertTrue(resolvedNodes[2].id() == 3);

                        resolvedNodes[0].free();
                        resolvedNodes[1].free();
                        resolvedNodes[2].free();

                    }
                });

                node1.relRemove("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,3]}}", node1.toString()));

                node1.relRemove("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [3]}}", node1.toString()));

                node1.relRemove("children", node2);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {}}", node1.toString()));

                long[] refValuesNull = node1.relValues("children");
                Assert.assertTrue(refValuesNull == null);

                //destroy the node explicitly without waiting GC
                node0.free();
                node1.free();
                node2.free();

                // System.out.println(((Graph) graph).space());

            }
        });

    }

}

