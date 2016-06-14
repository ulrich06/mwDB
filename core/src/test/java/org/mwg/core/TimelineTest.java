package org.mwg.core;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

public class TimelineTest {

    @Test
    public void heapTest() {
        test(new GraphBuilder().withScheduler(new NoopScheduler()).build());
    }

    /**
     * @ignore ts
     */
    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        test(new GraphBuilder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10000).saveEvery(20).build());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(final Graph graph) {
        final int[] counter = {0};
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean o) {
                final org.mwg.Node node_t0 = graph.newNode(0, 0);
                //timeTree should be already filled
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        counter[0]++;
                        Assert.assertTrue(longs.length == 1);
                        Assert.assertTrue(longs[0] == 0);
                    }
                });
                //do a simple modification
                node_t0.setProperty("name", Type.STRING, "MyName");
                Assert.assertTrue(node_t0.timeDephasing() == 0);
                //check the unmodified time tree
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        counter[0]++;
                        Assert.assertTrue(longs.length == 1);
                        Assert.assertTrue(longs[0] == 0);
                    }
                });

                graph.lookup(node_t0.world(), 1, node_t0.id(), new Callback<org.mwg.Node>() {
                    @Override
                    public void on(final org.mwg.Node node_t1) {
                        counter[0]++;
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"name\":\"MyName\"}", node_t1.toString()));
                        Assert.assertTrue(node_t1.timeDephasing() == 1); //node hasField a dephasing of 1 selectWith last known state
                        node_t1.rephase(); // force the object to move to timepoint 1
                        Assert.assertTrue(node_t1.timeDephasing() == 0); //node should be in phase now
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"name\":\"MyName\"}", node_t1.toString()));

                        node_t1.setProperty("name", Type.STRING, "MyName@t1");
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"name\":\"MyName@t1\"}", node_t1.toString()));
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"name\":\"MyName\"}", node_t0.toString()));

                        node_t1.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
                            @Override
                            public void on(long[] longs) {
                                counter[0]++;
                                Assert.assertTrue(longs.length == 2);
                                Assert.assertTrue(longs[0] == 1);
                                Assert.assertTrue(longs[1] == 0);
                            }
                        });

                        node_t1.timepoints(1, Constants.END_OF_TIME, new Callback<long[]>() {
                            @Override
                            public void on(long[] longs) {
                                counter[0]++;
                                Assert.assertTrue(longs.length == 1);
                                Assert.assertTrue(longs[0] == 1);
                            }
                        });

                        //now try to diverge the world
                        long newWorld = graph.fork(0);
                        graph.lookup(newWorld, 2, node_t0.id(), new Callback<org.mwg.Node>() {
                            @Override
                            public void on(Node node_t1_w0) {
                                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":1,\"time\":2,\"id\":1,\"name\":\"MyName@t1\"}", node_t1_w0.toString()));
                                Assert.assertTrue(node_t1_w0.timeDephasing() == 1);

                                node_t1_w0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
                                    @Override
                                    public void on(long[] longs) {
                                        counter[0]++;
                                        Assert.assertTrue(longs.length == 2);
                                        Assert.assertTrue(longs[0] == 1);
                                        Assert.assertTrue(longs[1] == 0);
                                    }
                                });
                                node_t1_w0.setProperty("name", Type.STRING, "MyName@t1@w1");
                                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":1,\"time\":2,\"id\":1,\"name\":\"MyName@t1@w1\"}", node_t1_w0.toString()));
                                //test the new timeline
                                node_t1_w0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
                                    @Override
                                    public void on(long[] longs) {
                                        counter[0]++;
                                        Assert.assertTrue(longs.length == 3);
                                        Assert.assertTrue(longs[0] == 2);
                                        Assert.assertTrue(longs[1] == 1);
                                        Assert.assertTrue(longs[2] == 0);
                                    }
                                });
                                //test the old timeline
                                node_t1.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
                                    @Override
                                    public void on(long[] longs) {
                                        counter[0]++;
                                        Assert.assertTrue(longs.length == 2);
                                        Assert.assertTrue(longs[0] == 1);
                                        Assert.assertTrue(longs[1] == 0);
                                    }
                                });


                            }
                        });


                    }
                });


                //end of the test
                graph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //test end
                    }
                });

            }
        });
        Assert.assertTrue(counter[0] == 8);
    }

}
