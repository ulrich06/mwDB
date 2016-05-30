package org.mwg;

import org.junit.Test;

public class WSServerTest {

    @Test
    public void test() {

        Graph graph = GraphBuilder.builder().withMemorySize(10000).withAutoSave(1000).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node node = graph.newNode(0, 0);
                node.set("name", "hello");
                graph.index("nodes", node, "name", null);

                WSServer graphServer = new WSServer(graph, 8050);
                graphServer.start();

                Graph graph2 = GraphBuilder.builder().withMemorySize(10000).withAutoSave(1000).withStorage(new WSSClient("ws://localhost:8050")).build();
                graph2.connect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        System.out.println("connected");
                        graph2.all(0, 0, "nodes", new Callback<Node[]>() {
                            @Override
                            public void on(Node[] result) {
                                System.out.println(result[0]);
                            }
                        });
                    }
                });
                try {
                    Thread.sleep(10000000000000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


    }

}
