package org.mwg.ws;


import org.junit.Before;
import org.junit.Test;
import org.mwg.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

    private Random _random;
    private long _seed = 12345L;
    private long _world;
    private long _time;

    @Before
    public void init() {
        _random = new Random(_seed);
        _world = _random.nextLong();
        _time = -214748364888888888L;
    }



    @Test
    public void testGet() {
        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage("data"),12345))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",12345)).build();

        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Test with world=" + _world + " and time=" + _time);

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                assertEquals(true,result);

                Node n1 = serverGraph.newNode(_world,_time);
                n1.set("name","node1");
                n1.set("value",1);
                serverGraph.index("indexName", n1, new String[]{"name"},null);

                serverGraph.save(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        assertEquals(true,result);
                        clientGraph.connect(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                assertEquals(true,result);

                                clientGraph.all(_world, _time, "indexName", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        latch.countDown();
                                        assertEquals(1,result.length);

                                        assertEquals("node1", result[0].get("name"));
                                        assertEquals(1, result[0].get("value"));
                                        assertEquals(_world,result[0].world());
                                        assertEquals(_time,result[0].time());

                                        System.out.println(Arrays.toString(result));
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });

        try {
            latch.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(latch.getCount());


    }

    @Test
    public void testConnectionDisconnection() {
        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage("data"),12345))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",12345)).build();

        int[] finalResult = new int[]{-1,-1};
        CountDownLatch countDownLatch = new CountDownLatch(1);

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                finalResult[0] = (result)? 1 : 0;
                clientGraph.connect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        finalResult[1] = result? 1 : 0;
                        countDownLatch.countDown();
                    }
                });
            }
        });

        try {
            countDownLatch.await(350,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertArrayEquals(new int[]{1,1},finalResult);

        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        finalResult[0] = -1;
        finalResult[1] = -1;

        clientGraph.disconnect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                finalResult[0] = (result)? 1 : 0;
                serverGraph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        finalResult[1] = (result)? 1 : 0;
                        countDownLatch2.countDown();
                    }
                });
            }
        });

        try {
            countDownLatch2.await(350,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertArrayEquals(new int[]{1,1},finalResult);
    }

    /*@Test
    public void testGet2() {
        final long world = random.nextLong();
        final long time = random.nextLong();
        System.out.println("Test with world=" + world + " and time=" + time);

        org.mwg.core.utility.DeferCounter counter = new org.mwg.core.utility.DeferCounter(1);



        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                assertEquals(true,result);

                System.out.println("coonnectec");

                clientGraph.connect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        assertEquals(true,result);

                        System.out.println("connected");

                        Node n1 = serverGraph.newNode(world,time);
                        n1.set("name","node1");
                        n1.set("value",1);
                        serverGraph.index("indexName", n1, new String[]{"name"}, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                assertEquals(true,result);
                                System.out.println("titi");
                                clientGraph.all(world, time, "indexName", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println("toto");
                                        System.out.println(Arrays.toString(result));
                                        assertEquals(1,result.length);

                                        assertEquals("node1", result[0].get("name"));
                                        assertEquals(1, result[0].get("value"));
                                        assertEquals(world,result[0].world());
                                        assertEquals(time,result[0].time());
                                        counter.count();
                                    }
                                });
                            }
                        });


                    }
                });


            }
        });

    }*/


}
