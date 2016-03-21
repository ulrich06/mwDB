package graph;

import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.offheap.OffHeapChunkSpace;

public class CallbackFail {

    @Test
    public void test0() {
        final KGraph graph = GraphBuilder.builder().withSpace(new OffHeapChunkSpace(10000, 100)).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final KNode node0 = graph.newNode(0, 0);
                node0.attSet("x", KType.INT, 0);
                System.out.println("before index xs node0");
                graph.index("xs", node0, new String[]{"x"}, new KCallback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        System.out.println("callback index xs node0");
                        System.out.println("before save");
                        graph.save(new KCallback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("callback save");
                                System.out.println("before all 1");
                                graph.all(0, 1, "xs", new KCallback<KNode[]>() {
                                    @Override
                                    public void on(KNode[] result) {
                                        System.out.println("callback all 1");
                                        System.out.println("before unindex");
                                        graph.unindex("xs", result[0], new String[]{"x"}, new KCallback<Boolean>() {
                                            @Override
                                            public void on(Boolean result) {
                                                System.out.println("callback unindex");
                                                final KNode node1 = graph.newNode(0, 1);
                                                node1.attSet("x", KType.INT, 1);
                                                System.out.println("before index xs node1");
                                                graph.index("xs", node1, new String[]{"x"}, new KCallback<Boolean>() {
                                                    @Override
                                                    public void on(Boolean result) {
                                                        System.out.println("callback index xs node1");
                                                        System.out.println("before all 2");
                                                        graph.all(0, 2, "xs", result1 -> {
                                                            System.out.println("callback all 2");
                                                            System.out.println("OK " + result1);
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

}
