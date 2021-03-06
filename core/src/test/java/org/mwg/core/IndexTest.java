package org.mwg.core;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

public class IndexTest {

    @Test
    public void heapTest() {
        test(new GraphBuilder().withScheduler(new NoopScheduler()).build());
        testRelation(new GraphBuilder().withScheduler(new NoopScheduler()).build());
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

        test(new GraphBuilder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10000).saveEvery(100).build());
        testRelation(new GraphBuilder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10000).saveEvery(100).build());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void testRelation(final Graph graph) {
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final org.mwg.Node node_t0 = graph.newNode(0, 0);
                node_t0.setProperty("name", Type.STRING, "MyName");

                final org.mwg.Node node_t1 = graph.newNode(0, 0);
                node_t1.setProperty("name", Type.STRING, "MyName2");

                node_t1.add("children", node_t0);
                graph.index("bigram", node_t1, "children", null);

                graph.findAll(0, 0, "bigram", new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result) {
                        Assert.assertEquals(result.length, 1);
                        Assert.assertEquals(result[0].id(), node_t1.id());
                    }
                });


                Query q = graph.newQuery();
                q.setIndexName("bigram");
                q.setTime(0);
                q.setWorld(0);
                q.add("children", new long[]{node_t0.id()});
                graph.findByQuery(q, new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result) {
                        Assert.assertEquals(result.length, 1);
                        Assert.assertEquals(result[0].id(), node_t1.id());
                    }
                });


                graph.disconnect(null);
            }
        });
    }

    private void test(final Graph graph) {
        final int[] counter = {0};
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean o) {
                org.mwg.Node node_t0 = graph.newNode(0, 0);
                node_t0.setProperty("name", Type.STRING, "MyName");

                graph.findAll(0, 0, "nodes", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 0);
                    }
                });

                graph.index("nodes", node_t0, "name", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });

                graph.findAll(0, 0, "nodes", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"name\":\"MyName\"}", allNodes[0].toString()));
                    }
                });

                graph.find(0, 0, "nodes", "name=MyName", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertEquals(1, kNode.length);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"name\":\"MyName\"}", kNode[0].toString()));
                    }
                });

                //test a null index
                graph.findAll(0, 0, "unknownIndex", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 0);
                    }
                });


                org.mwg.Node node_t1 = graph.newNode(0, 0);
                node_t1.setProperty("name", Type.STRING, "MyName");
                node_t1.setProperty("version", Type.STRING, "1.0");

                graph.index("nodes", node_t1, "name,version", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });


                //test the old indexed node
                graph.find(0, 0, "nodes", "name=MyName", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"name\":\"MyName\"}", kNode[0].toString()));
                    }
                });

                //test the new indexed node
                graph.find(0, 0, "nodes", "name=MyName,version=1.0", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"name\":\"MyName\",\"version\":\"1.0\"}", kNode[0].toString()));
                    }
                });


                //test potential inversion
                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"name\":\"MyName\",\"version\":\"1.0\"}", kNode[0].toString()));
                    }
                });


                //unIndex the node @t1
                graph.unindex("nodes", node_t1, "name,version", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });


                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 0);
                    }
                });


                //reIndex
                graph.index("nodes", node_t1, "name,version", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });


                //should work again
                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(org.mwg.Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"name\":\"MyName\",\"version\":\"1.0\"}", kNode[0].toString()));
                    }
                });


                //local index usage
                org.mwg.Node node_index = graph.newNode(0, 0);
                node_index.index("children", node_t1, "name,version", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        counter[0]++;
                    }
                });

                node_index.find("children", "name=MyName,version=1.0", new Callback<org.mwg.Node[]>() {
                    @Override
                    public void on(Node[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"name\":\"MyName\",\"version\":\"1.0\"}", kNode[0].toString()));
                    }
                });

                graph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //end of the test
                    }
                });

            }
        });
        Assert.assertTrue(counter[0] == 15);
    }

}
