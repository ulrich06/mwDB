package org.mwg;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class WSServerTest {

    @Test
    public void test() {

        Graph graph = GraphBuilder.builder()
                .withMemorySize(10000)
                .withAutoSave(1000)
                .withOffHeapMemory()
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node node = graph.newNode(0, 0);
                node.set("name", "hello");
                graph.index("nodes", node, "name", null);

                Assert.assertEquals("{\"world\":0,\"time\":0,\"id\":1,\"name\":\"hello\"}", node.toString());

                WSServer graphServer = new WSServer(graph, 8050);
                graphServer.start();

                CountDownLatch latch = new CountDownLatch(1);

                Graph graph2 = GraphBuilder.builder().withMemorySize(10000).withAutoSave(1000).withStorage(new WSClient("ws://localhost:8050")).build();
                graph2.connect(result1 -> graph2.all(0, 0, "nodes", new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result1) {

                        Node newNode = graph2.newNode(0, 0);
                        newNode.set("name", "hello2");

                        Assert.assertEquals("{\"world\":0,\"time\":0,\"id\":137438953473,\"name\":\"hello2\"}", newNode.toString());

                        graph2.index("nodes", newNode, "name", null);

                        graph2.all(0, 0, "nodes", new Callback<Node[]>() {
                            @Override
                            public void on(Node[] result) {
                                Assert.assertEquals(2, result.length);
                            }
                        });


                        graph2.save(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                //ok now try to access new node from graph
                                graph.all(0, 0, "nodes", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        Assert.assertEquals(2, result.length);
                                        Assert.assertEquals(result[0].toString(), "{\"world\":0,\"time\":0,\"id\":1,\"name\":\"hello\"}");
                                        Assert.assertEquals(result[1].toString(), "{\"world\":0,\"time\":0,\"id\":137438953473,\"name\":\"hello2\"}");

                                        latch.countDown();

                                    }
                                });

                            }
                        });
                    }
                }));

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


    }

}
