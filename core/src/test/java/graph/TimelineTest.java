package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.offheap.*;
import org.mwdb.task.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;

public class TimelineTest {

    @Test
    public void heapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph());
    }

    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withSpace(new OffHeapChunkSpace(10000, 20)).buildGraph());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(KGraph graph) {
        final int[] counter = {0};
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean o) {
                KNode node_t0 = graph.newNode(0, 0);
                //timeTree should be already filled
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        counter[0]++;
                        Assert.assertTrue(longs.length == 1);
                        Assert.assertTrue(longs[0] == 0);
                    }
                });
                //do a simple modification
                node_t0.attSet("name", KType.STRING, "MyName");
                Assert.assertTrue(node_t0.timeDephasing() == 0);
                //check the unmodified time tree
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        counter[0]++;
                        Assert.assertTrue(longs.length == 1);
                        Assert.assertTrue(longs[0] == 0);
                    }
                });

                graph.lookup(node_t0.world(), 1, node_t0.id(), new KCallback<KNode>() {
                    @Override
                    public void on(KNode node_t1) {
                        counter[0]++;
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"data\": {\"name\": \"MyName\"}}", node_t1.toString()));
                        Assert.assertTrue(node_t1.timeDephasing() == 1); //node has a dephasing of 1 with last known state
                        node_t1.forcePhase(); // force the object to move to timepoint 1
                        Assert.assertTrue(node_t1.timeDephasing() == 0); //node should be in phase now
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"data\": {\"name\": \"MyName\"}}", node_t1.toString()));

                        node_t1.attSet("name", KType.STRING, "MyName@t1");
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"data\": {\"name\": \"MyName@t1\"}}", node_t1.toString()));
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName\"}}", node_t0.toString()));

                        node_t1.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                            @Override
                            public void on(long[] longs) {
                                counter[0]++;
                                Assert.assertTrue(longs.length == 2);
                                Assert.assertTrue(longs[0] == 1);
                                Assert.assertTrue(longs[1] == 0);
                            }
                        });

                        //now try to diverge the world
                        long newWorld = graph.diverge(0);
                        graph.lookup(newWorld, 2, node_t0.id(), new KCallback<KNode>() {
                            @Override
                            public void on(KNode node_t1_w0) {
                                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":1,\"time\":2,\"id\":1,\"data\": {\"name\": \"MyName@t1\"}}", node_t1_w0.toString()));
                                Assert.assertTrue(node_t1_w0.timeDephasing() == 1);

                                node_t1_w0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                                    @Override
                                    public void on(long[] longs) {
                                        counter[0]++;
                                        Assert.assertTrue(longs.length == 2);
                                        Assert.assertTrue(longs[0] == 1);
                                        Assert.assertTrue(longs[1] == 0);
                                    }
                                });
                                node_t1_w0.attSet("name", KType.STRING, "MyName@t1@w1");
                                Assert.assertTrue(PrimitiveHelper.equals("{\"world\":1,\"time\":2,\"id\":1,\"data\": {\"name\": \"MyName@t1@w1\"}}", node_t1_w0.toString()));
                                //test the new timeline
                                node_t1_w0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
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
                                node_t1.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                                    @Override
                                    public void on(long[] longs) {
                                        counter[0]++;
                                        Assert.assertTrue(longs.length == 2);
                                        Assert.assertTrue(longs[0] == 1);
                                        Assert.assertTrue(longs[1] == 0);
                                    }
                                });


                                //end of the test
                                graph.disconnect(new KCallback<Boolean>() {
                                    @Override
                                    public void on(Boolean result) {
                                        //test end
                                    }
                                });

                            }
                        });


                    }
                });


            }
        });
        Assert.assertTrue(counter[0] == 7);
    }

}
