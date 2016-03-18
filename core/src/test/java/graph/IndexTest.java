package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.offheap.OffHeapChunkSpace;
import org.mwdb.task.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;

public class IndexTest {

    @Test
    public void heapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph());
    }

    @Test
    public void offHeapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withSpace(new OffHeapChunkSpace(10000, 20)).buildGraph());
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

                graph.find(0, 0, "nodes", "name=MyName", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName\"}}", kNode.toString()));
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
                graph.find(0, 0, "nodes", "name=MyName", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":1,\"data\": {\"name\": \"MyName\"}}", kNode.toString()));
                    }
                });

                //test the new indexed node
                graph.find(0, 0, "nodes", "name=MyName,version=1.0", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", node_t1.toString()));
                    }
                });

                //test potential inversion
                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", node_t1.toString()));
                    }
                });

                //unIndex the node @t1
                graph.unindex("nodes", node_t1, new String[]{"name", "version"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean o) {
                        counter[0]++;
                    }
                });

                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode == null);
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
                graph.find(0, 0, "nodes", "version=1.0,name=MyName", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", node_t1.toString()));
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
                node_index.find("children", "name=MyName,version=1.0", new KCallback<KNode>() {
                    @Override
                    public void on(KNode kNode) {
                        counter[0]++;
                        Assert.assertTrue(kNode != null);
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":0,\"id\":3,\"data\": {\"name\": \"MyName\",\"version\": \"1.0\"}}", node_t1.toString()));
                    }
                });


            }
        });
        Assert.assertTrue(counter[0] == 15);
    }

}
