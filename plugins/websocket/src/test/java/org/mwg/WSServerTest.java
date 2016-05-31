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

                System.out.println(node);

                WSServer graphServer = new WSServer(graph, 8050);
                graphServer.start();

                Graph graph2 = GraphBuilder.builder().withMemorySize(10000).withAutoSave(1000).withStorage(new WSSClient("ws://localhost:8050")).build();
                graph2.connect(result1 -> graph2.all(0, 0, "nodes", new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result1) {
                        System.out.println(result1[0]);

                        Node newNode = graph2.newNode(0, 0);
                        newNode.set("name", "hello2");
                        graph2.index("nodes", node, "name", null);
                        graph2.save(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("Saved");
                            }
                        });

                        System.out.println(newNode);

                    }
                }));

                /*
                try {
                    Thread.sleep(10000000000000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        });


    }

}
