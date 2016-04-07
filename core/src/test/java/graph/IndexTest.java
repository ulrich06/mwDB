package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.offheap.*;
import org.mwdb.task.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;

public class IndexTest {

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

        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10000).withAutoSave(20).build());

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
                node_t0.attSet("name", KType.STRING, "MyName");

                graph.all(0, 0, "nodes", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 0);
                    }
                });

                graph.index("nodes", node_t0, new String[]{"name"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });

                graph.all(0, 0, "nodes", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName\"}}", allNodes[0].toString()));
                    }
                });

                graph.find(0, 0, "nodes", "name=MyName", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName\"}}", kNode[0].toString()));
                    }
                });

                //test a null index
                graph.all(0, 0, "unknownIndex", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 0);
                    }
                });


                KNode node_t1 = graph.newNode(0, 0);
                node_t1.attSet("name", KType.STRING, "MyName");
                node_t1.attSet("version", KType.STRING, "1.0");

                graph.index("nodes", node_t1, new String[]{"name", "version"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });

                //test the old indexed node
                graph.find(0, 0, "nodes", "name=MyName", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName\"}}", kNode[0].toString()));
                    }
                });

                //test the new indexed node
                graph.find(0, 0, "nodes", "name=MyName,version=1.0", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", kNode[0].toString()));
                    }
                });

                //test potential inversion
                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", kNode[0].toString()));
                    }
                });

                //unIndex the node @t1
                graph.unindex("nodes", node_t1, new String[]{"name", "version"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });

                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 0);
                    }
                });

                //reIndex
                graph.index("nodes", node_t1, new String[]{"name", "version"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });

                //should work again
                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", kNode[0].toString()));
                    }
                });


                //local index usage
                KNode node_index = graph.newNode(0, 0);
                node_index.index("children", node_t1, new String[]{"name", "version"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        counter[0]++;
                    }
                });
                node_index.find("children", "name=MyName,version=1.0", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(kNode.length == 1);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", kNode[0].toString()));
                    }
                });

                graph.disconnect(new KCallback<Boolean>() {
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
