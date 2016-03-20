package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.heap.HeapChunkSpace;
import org.mwdb.chunk.offheap.*;
import org.mwdb.task.NoopScheduler;

public class Benchmark2Test {

    @Test
    public void heapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withSpace(new HeapChunkSpace(100, 10)).buildGraph());
    }

    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withSpace(new OffHeapChunkSpace(100_000, 10_000)).buildGraph());
    }

    //final int valuesToInsert = 10_000_000;
    final int valuesToInsert = 500;

    final long timeOrigin = 1000;

    private void test(KGraph graph) {
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final long before = System.currentTimeMillis();

                KNode temp_node = graph.newNode(0, 0);
                long nodeID = temp_node.id();
                temp_node.free();

                //KNode node = graph.newNode(0, 0);
                final KDeferCounter counter = graph.counter(valuesToInsert);
                for (long i = 0; i < valuesToInsert; i++) {

                    if (i % 10 == 0) {
                        //node.free();
                        //node = graph.newNode(0, 0);

                        temp_node = graph.newNode(0, 0);
                        nodeID = temp_node.id();
                        temp_node.free();

                        System.out.println(i+" node>"+nodeID);
                    }

                    if (i % 1_000_000 == 0) {
                        System.out.println("<insert til " + i + " in " + (System.currentTimeMillis() - before) / 1000 + "s");
                    }

                    final double value = i * 0.3;
                    final long time = timeOrigin + i;
                    final long finalNodeID = nodeID;
                    graph.lookup(0, time, nodeID, new KCallback<KNode>() {
                        @Override
                        public void on(KNode timedNode) {

                            if (timedNode == null) {
                                graph.lookup(0, time, finalNodeID, new KCallback<KNode>() {
                                    @Override
                                    public void on(KNode timedNode) {
                                        timedNode.attSet("value", KType.DOUBLE, value);
                                        counter.count();
                                        timedNode.free();//free the node, for cache management
                                    }
                                });
                            }


                            timedNode.attSet("value", KType.DOUBLE, value);
                            counter.count();
                            timedNode.free();//free the node, for cache management
                        }
                    });
                }
                // node.free();


                counter.then(new KCallback() {
                    @Override
                    public void on(Object result) {

                        long beforeRead = System.currentTimeMillis();

                        //System.out.println("<end insert phase>" + " " + (System.currentTimeMillis() - before) / 1000 + "s");
                        //System.out.println("result: " + (valuesToInsert / ((System.currentTimeMillis() - before) / 1000) / 1000) + "kv/s");

                        /*
                        final KDeferCounter counterRead = graph.counter(valuesToInsert);
                        for (long i = 0; i < valuesToInsert; i++) {
                            final double value = i * 0.3;
                            final long time = timeOrigin + i;

                            graph.lookup(0, time, node.id(), new KCallback<KNode>() {
                                @Override
                                public void on(KNode timedNode) {
                                    Assert.assertTrue((double) timedNode.att("value") == value);
                                    counterRead.count();
                                    timedNode.free();//free the node, for cache management
                                }
                            });
                        }
                        counterRead.then(new KCallback() {
                            @Override
                            public void on(Object result) {
                                long afterRead = System.currentTimeMillis();
                                System.out.println("<end read phase>" + " " + (afterRead - beforeRead) / 1000 + "s ");
                                System.out.println("result: " + (valuesToInsert / ((afterRead - beforeRead) / 1000) / 1000) + "kv/s");

                            }
                        });
*/

                        graph.disconnect(new KCallback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("Graph disconnected");

                                Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
                                Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
                                Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
                                Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
                            }
                        });


                    }
                });

            }
        });
    }

}
