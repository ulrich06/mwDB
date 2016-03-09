package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.manager.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;

public class IndexTest {

    @Test
    public void mwHeapTimelineTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph());
    }

    private void test(KGraph graph) {
        final int[] counter = {0};
        graph.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KNode node_t0 = graph.createNode(0, 0);
                node_t0.attSet("name", KType.STRING, "MyName");

                graph.all(0, 0, "nodes", new KCallback<KNode[]>() {
                    @Override
                    public void on(KNode[] allNodes) {
                        counter[0]++;
                        Assert.assertTrue(allNodes.length == 0);
                    }
                });

                graph.index("nodes", node_t0, new String[]{"name"}, new KCallback() {
                    @Override
                    public void on(Object o) {
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


                KNode node_t1 = graph.createNode(0, 0);
                node_t1.attSet("name", KType.STRING, "MyName");
                node_t1.attSet("version", KType.STRING, "1.0");

                graph.index("nodes", node_t1, new String[]{"name", "version"}, new KCallback() {
                    @Override
                    public void on(Object o) {
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


            }
        });
        Assert.assertTrue(counter[0] == 9);
    }

}
