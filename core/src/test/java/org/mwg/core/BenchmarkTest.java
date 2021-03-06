package org.mwg.core;

import org.junit.Assert;
import org.mwg.*;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.Job;

public class BenchmarkTest {

    // @Test
    public void heapTest() {
        final Graph graph = new GraphBuilder().withScheduler(new NoopScheduler()).withMemorySize(100000).saveEvery(10000).build();
        test("heap ", graph, new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                graph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        System.out.println("Graph disconnected");
                    }
                });
            }
        });

    }

    /**
     * @ignore ts
     */
    //@Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        final Graph graph = new GraphBuilder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(100000).saveEvery(10000).build();

        test("offheap ", graph, new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                graph.disconnect(new Callback<Boolean>() {
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

    //final int valuesToInsert = 10000000;
    final int valuesToInsert = 10000000;

    final long timeOrigin = 1000;

    private void test(final String name, final Graph graph, final Callback<Boolean> testEnd) {
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final long before = System.currentTimeMillis();
                org.mwg.Node node = graph.newNode(0, 0);
                final DeferCounter counter = graph.newCounter(valuesToInsert);
                for (long i = 0; i < valuesToInsert; i++) {

                    if (i % 1000 == 0) {
                        node.free();
                        node = graph.newNode(0, 0);
                    }

                    if (i % 1000000 == 0) {
                        System.out.println("<insert til " + i + " in " + (System.currentTimeMillis() - before) / 1000 + "s");
                    }

                    final double value = i * 0.3;
                    final long time = timeOrigin + i;
                    graph.lookup(0, time, node.id(), new Callback<org.mwg.Node>() {
                        @Override
                        public void on(Node timedNode) {
                            timedNode.setProperty("value", Type.DOUBLE, value);
                            counter.count();
                            timedNode.free();//free the node, for cache management
                        }
                    });
                }
                node.free();


                counter.then(new Job() {
                    @Override
                    public void run() {

                        long beforeRead = System.currentTimeMillis();

                        System.out.println("<end insert phase>" + " " + (System.currentTimeMillis() - before) / 1000 + "s");
                        System.out.println(name + " result: " + (valuesToInsert / ((System.currentTimeMillis() - before) / 1000) / 1000) + "kv/s");

                        /*
                        final CoreDeferCounter counterRead = graph.counter(valuesToInsert);
                        for (long i = 0; i < valuesToInsert; i++) {
                            final double value = i * 0.3;
                            final long time = timeOrigin + i;

                            graph.lookup(0, time, node.id(), new Callback<Node>() {
                                @Override
                                public void on(Node timedNode) {
                                    Assert.assertTrue((double) timedNode.get("value") == value);
                                    counterRead.count();
                                    timedNode.free();//free the node, for cache management
                                }
                            });
                        }
                        counterRead.then(new Callback() {
                            @Override
                            public void on(Object result) {
                                long afterRead = System.currentTimeMillis();
                                System.out.println("<end read phase>" + " " + (afterRead - beforeRead) / 1000 + "s ");
                                System.out.println("result: " + (valuesToInsert / ((afterRead - beforeRead) / 1000) / 1000) + "kv/s");

                            }
                        });
*/
                        testEnd.on(true);


                    }
                });

            }
        });
    }

}
