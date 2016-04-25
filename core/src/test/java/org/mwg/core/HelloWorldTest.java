package org.mwg.core;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

public class HelloWorldTest {

    @Test
    public void heapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).build());
    }

    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10_000).withAutoSave(20).build());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);

    }

    private void test(Graph graph) {
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean o) {

                org.mwg.Node node0 = graph.newNode(0, 0);

                //do something selectWith the node
                graph.lookup(0, 0, node0.id(), new Callback<org.mwg.Node>() {
                    @Override
                    public void on(org.mwg.Node result) {
                        Assert.assertTrue(result.id() == node0.id());
                    }
                });

                node0.set("name", "MyName");
                Assert.assertTrue(PrimitiveHelper.equals("MyName", node0.get("name").toString()));

                node0.remove("name");
                Assert.assertTrue(node0.get("name") == null);
                node0.setProperty("name", Type.STRING, "MyName");

                node0.setProperty("value", Type.STRING, "MyValue");
                Assert.assertTrue(PrimitiveHelper.equals("MyValue", node0.get("value").toString()));
                //check that other attribute name is not affected
                Assert.assertTrue(PrimitiveHelper.equals("MyName", node0.get("name").toString()));

                node0.setProperty("name", Type.STRING, "MyName2");
                Assert.assertTrue(PrimitiveHelper.equals("MyName2", node0.get("name").toString()));
                Assert.assertTrue(PrimitiveHelper.equals("MyValue", node0.get("value").toString()));

                //check the simple json print

                String flatNode0 = "{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName2\",\"value\": \"MyValue\"}}";

                Assert.assertTrue(flatNode0.length() == node0.toString().length());
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName2\",\"value\": \"MyValue\"}}", node0.toString()));

                //Create a new node
                org.mwg.Node node1 = graph.newNode(0, 0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {}}", node1.toString()));

                //attach the new node
                node1.add("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1]}}", node1.toString()));

                node1.add("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,1]}}", node1.toString()));

                org.mwg.Node node2 = graph.newNode(0, 0);
                node1.add("children", node2);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,1,3]}}", node1.toString()));

                long[] refValuesThree = (long[]) node1.get("children");
                Assert.assertTrue(refValuesThree.length == 3);
                Assert.assertTrue(refValuesThree[0] == 1);
                Assert.assertTrue(refValuesThree[1] == 1);
                Assert.assertTrue(refValuesThree[2] == 3);

                node1.rel("children", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(Node[] resolvedNodes) {
                        Assert.assertTrue(resolvedNodes[0].id() == 1);
                        Assert.assertTrue(resolvedNodes[1].id() == 1);
                        Assert.assertTrue(resolvedNodes[2].id() == 3);

                        resolvedNodes[0].free();
                        resolvedNodes[1].free();
                        resolvedNodes[2].free();

                    }
                });

                node1.remove("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [1,3]}}", node1.toString()));

                node1.remove("children", node0);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {\"children\": [3]}}", node1.toString()));

                node1.remove("children", node2);
                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":2,\"data\": {}}", node1.toString()));

                long[] refValuesNull = (long[]) node1.get("children");
                Assert.assertNull(refValuesNull);

                //destroy the node explicitly selectWithout waiting GC
                node0.free();
                node1.free();
                node2.free();

                // System.out.println(((Graph) graph).space());

                graph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //end of test
                    }
                });

            }
        });

    }

}

