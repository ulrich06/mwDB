package org.mwg;

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

                System.out.println(node);

                WSServer graphServer = new WSServer(graph, 8050);
                graphServer.start();

                CountDownLatch latch = new CountDownLatch(1);

                Graph graph2 = GraphBuilder.builder().withMemorySize(10000).withAutoSave(1000).withStorage(new WSClient("ws://localhost:8050")).build();
                graph2.connect(result1 -> graph2.all(0, 0, "nodes", new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result1) {
                        System.out.println(result1[0]);

                        Node newNode = graph2.newNode(0, 0);
                        newNode.set("name", "hello2");

                        System.out.println(newNode);

                        graph2.index("nodes", newNode, "name", null);

                        graph2.all(0, 0, "nodes", new Callback<Node[]>() {
                            @Override
                            public void on(Node[] result) {
                                System.out.println(result.length);
                            }
                        });


                        graph2.save(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                //ok now try to access new node from graph
                                graph.all(0, 0, "nodes", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println(result[0].toString());
                                        System.out.println(result[1].toString());

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
