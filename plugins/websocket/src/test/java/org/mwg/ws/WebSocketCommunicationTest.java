package org.mwg.ws;


public class WebSocketCommunicationTest {
    //@org.junit.Test
    /*public void test() {
        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage("data"),8080))
                                .build();

        WSStorageClient wsClient = null;
        try {
            wsClient = WSStorageClient.init("0.0.0.0",8080);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(wsClient);
        Graph clientGraph = GraphBuilder.builder().withStorage(wsClient).build();

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                clientGraph.connect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        System.out.println("ok");
                        Node node1 = serverGraph.newNode(0,0);
                        node1.set("name","node1");

                        Node node2 = serverGraph.newNode(0,0);
                        node2.set("name","node2");

                        Node node3 = serverGraph.newNode(0,0);
                        node3.set("name","node3");


                        String[] attIndex = new String[]{"name"};
                        serverGraph.index("indexName",node1,attIndex,null);
                        serverGraph.index("indexName",node2,attIndex,null);
                        serverGraph.index("indexName",node3,attIndex,null);

                        clientGraph.all(0, 0, "indexName", new Callback<Node[]>() {
                            @Override
                            public void on(Node[] result) {
                                System.out.println("heu....");
                                System.out.println(Arrays.toString(result));
                            }
                        });
                    }
                });
            }
        });

    }*/
}
