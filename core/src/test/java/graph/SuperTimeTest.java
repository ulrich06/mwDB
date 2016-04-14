package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.KTimeTreeChunk;
import org.mwdb.chunk.offheap.*;
import org.mwdb.task.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

import java.util.HashMap;
import java.util.HashSet;

public class SuperTimeTest {

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

        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10000).withAutoSave(20).build());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(KGraph graph) {
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean o) {
                Graph cgraph = (Graph) graph;

                KNode node_t0 = graph.newNode(0, 0);
                for (int i = 0; i < Constants.SCALE_1; i++) {
                    final int finalI = i;
                    graph.lookup(0, i, node_t0.id(), new KCallback<KNode>() {
                        @Override
                        public void on(KNode result) {
                            result.attSet("time", KType.INT, finalI);
                        }
                    });
                }

                KChunkSpace space = cgraph.space();

                KTimeTreeChunk superTimeTree = (KTimeTreeChunk) space.getAndMark(Constants.TIME_TREE_CHUNK, 0, Constants.NULL_LONG, node_t0.id());
                Assert.assertTrue(superTimeTree != null);
                long superTimeTreeSize = superTimeTree.size();
                Assert.assertTrue(superTimeTreeSize == 1); // TimeTree is not splitted

                //insert a new timePoint
                graph.lookup(0, Constants.SCALE_1, node_t0.id(), new KCallback<KNode>() {
                    @Override
                    public void on(KNode result) {
                        result.attSet("time", KType.INT, (int) Constants.SCALE_1);
                    }
                });

                long superTimeTreeSize2 = superTimeTree.size();
                Assert.assertTrue(superTimeTreeSize2 == 1); // TimeTree is still not splitted (split at 2n)

                //insert the rest
                for (int i = ((int) Constants.SCALE_1 + 1); i < (Constants.SCALE_1 * 2); i++) {
                    final int finalI = i;
                    graph.lookup(0, i, node_t0.id(), new KCallback<KNode>() {
                        @Override
                        public void on(KNode result) {
                            result.attSet("time", KType.INT, finalI);
                        }
                    });
                }

                long superTimeTreeSize3 = superTimeTree.size();
                Assert.assertTrue(superTimeTreeSize3 == 2);

                for (int i = ((int) Constants.SCALE_1 * 2); i < (Constants.SCALE_1 * 5); i++) {
                    final int finalI = i;
                    graph.lookup(0, i, node_t0.id(), new KCallback<KNode>() {
                        @Override
                        public void on(KNode result) {
                            result.attSet("time", KType.INT, finalI);
                        }
                    });
                }

                long superTimeTreeSize5 = superTimeTree.size();
                Assert.assertTrue(superTimeTreeSize5 == 5); // TimeTree is splitted in two

                //unmark the superTimeTree
                space.unmarkChunk(superTimeTree);

                //test that lookup are corrects
                for (int i = 0; i < (Constants.SCALE_1 * 5); i++) {
                    final int finalI = i;
                    graph.lookup(0, i, node_t0.id(), new KCallback<KNode>() {
                        @Override
                        public void on(KNode result) {
                            Assert.assertTrue((int) result.att("time") == finalI);
                        }
                    });
                }

                //test the range now
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                    @Override
                    public void on(long[] result) {
                        for (int i = 0; i < (Constants.SCALE_1 * 5); i++) {
                            result[i] = (Constants.SCALE_1 * 5) + 1 - i;
                        }
                    }
                });

                graph.disconnect(null);
            }
        });
    }

}
