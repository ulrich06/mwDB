package org.mwg.ws;

import org.mwg.*;

import java.util.Arrays;

/**
 * Created by ludovicmouline on 04/05/16.
 */
public class Test2 {

    public static void main(String[] args) {
        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage("data"),8080))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",8080)).build();

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node node1 = serverGraph.newNode(0,0);
                node1.set("name","node1");
                final long id = node1.id();

                String[] attIndex = new String[]{"name"};
                serverGraph.index("indexName",node1,attIndex,null);

                serverGraph.save(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        clientGraph.connect(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                clientGraph.all(0, 0, "indexName", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println("Client " + Arrays.toString(result));

                                        /*Node clientNode = clientGraph.newNode(0,0);
                                        clientNode.set("name","node2");
                                        clientGraph.index("indexName",clientNode,attIndex,null);

                                        clientGraph.all(0, 0, "indexName", new Callback<Node[]>() {
                                            @Override
                                            public void on(Node[] result) {
                                                System.out.println("Client " + Arrays.toString(result));

                                                clientGraph.save(new Callback<Boolean>() {
                                                    @Override
                                                    public void on(Boolean result) {
                                                        serverGraph.all(0, 0, "indexName", new Callback<Node[]>() {
                                                            @Override
                                                            public void on(Node[] result) {
                                                                System.out.println("Server " + Arrays.toString(result));

                                               *//* clientGraph.disconnect(new Callback<Boolean>() {
                                                    @Override
                                                    public void on(Boolean result) {
                                                        serverGraph.disconnect(new Callback<Boolean>() {
                                                            @Override
                                                            public void on(Boolean result) {
                                                                System.out.println("End");
                                                            }
                                                        });
                                                    }
                                                });*//*
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });*/


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
