package org.mwg.ws;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mwg.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mwg.Constants.BEGINNING_OF_TIME;
import static org.mwg.Constants.END_OF_TIME;

public class WebSocketCommunicationTest {
    private long _world;
    private long _time;
    private static final String DB_BASE_NAME = "data_";

    @Before
    public void init() {
         Random random = new Random(12345L);
        _world = BEGINNING_OF_TIME + (long)(random.nextDouble() * (END_OF_TIME - BEGINNING_OF_TIME));
        _time = BEGINNING_OF_TIME + (long)(random.nextDouble() * (END_OF_TIME - BEGINNING_OF_TIME));
    }

    @After
    public void deleteDB() {
        Path path = Paths.get(".");
        deleteDBDirectory(path.toFile());
    }

    @Test
    public void testGet() {
        int port = 12346;
        Path path = Paths.get(DB_BASE_NAME + 1);

        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage(path.toString()),port))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",port)).build();

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

        assertEquals(0,latch.getCount());



    }

    @Test
    public void testConnectionDisconnection() {
        int port = 12345;
        Path path = Paths.get(DB_BASE_NAME + 2);
        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage(path.toString()),port))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",port)).build();

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

    /**
     * Tests below currently does not pass due to a known bug
     * We keep it to show some known situations with unexpected behavior
     *
     */

    //    @Test
    public void testGet2() {
        int port = 12347;
        Path path = Paths.get(DB_BASE_NAME + 3);

        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage(path.toString()),port))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",port)).build();

        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Test with world=" + _world + " and time=" + _time);

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                assertEquals(true,result);

                clientGraph.connect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        assertEquals(true,result);

                        Node n1 = serverGraph.newNode(_world,_time);
                        n1.set("name","node1");
                        n1.set("value",1);
                        serverGraph.index("indexName", n1, new String[]{"name"}, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                assertEquals(true,result);

                                clientGraph.all(_world, _time, "indexName", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println(Arrays.toString(result));
                                        assertEquals(1,result.length);

                                        assertEquals("node1", result[0].get("name"));
                                        assertEquals(1, result[0].get("value"));
                                        assertEquals(_world,result[0].world());
                                        assertEquals(_time,result[0].time());
                                        latch.countDown();
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

        assertEquals(0,latch.getCount());

    }

    public void testPut() {
        int port = 12348;
        Path path = Paths.get(DB_BASE_NAME + 4);

        Graph serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage(path.toString()),port))
                .build();

        Graph clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",port)).build();

        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Test with world=" + _world + " and time=" + _time);

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                assertEquals(true,result);

                clientGraph.connect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        assertEquals(true,result);

                        Node n1 = clientGraph.newNode(_world,_time);
                        n1.set("name","node1");
                        n1.set("value",1);

                        clientGraph.index("indexName", n1, new String[]{"name"}, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                assertEquals(true,result);

                                //this should put data on the storage
                                clientGraph.save(new Callback<Boolean>() {
                                    @Override
                                    public void on(Boolean result) {

                                        serverGraph.all(_world, _time, "indexName", new Callback<Node[]>() {
                                            @Override
                                            public void on(Node[] result) {
                                                System.out.println(Arrays.toString(result));
                                                latch.countDown();
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

        try {
            latch.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(0,latch.getCount());
    }

    private void deleteDBDirectory(File file) {
        if (file.isDirectory()) {
            for (File content : file.listFiles()) {
                deleteDBDirectory(content);
            }
        }
        if(file.getPath().startsWith("./" + DB_BASE_NAME)) {

            file.delete();
        }
    }

}
