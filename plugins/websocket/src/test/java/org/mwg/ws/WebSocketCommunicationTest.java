package org.mwg.ws;


import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.ws.builder.WSStorageClientBuilder;
import org.mwg.ws.builder.WSStorageWrapperBuilder;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
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
    private Graph serverGraph;
    private Graph clientGraph;
    private Path dbPath;

    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEYSTORE_TYPE = "JKS";

    public void init(boolean ssl)  {
         Random random = new Random(12345L);
        _world = BEGINNING_OF_TIME + (long)(random.nextDouble() * (END_OF_TIME - BEGINNING_OF_TIME));
        _time = BEGINNING_OF_TIME + (long)(random.nextDouble() * (END_OF_TIME - BEGINNING_OF_TIME));

        dbPath = Paths.get("data");

        try {
            if (!ssl) {
                WSStorageWrapper server = WSStorageWrapperBuilder.builder()
                        .withWrappedStorage(new LevelDBStorage(dbPath.toString()))
                        .withPort(12345)
                        .build();
                serverGraph = GraphBuilder.builder().withStorage(server).build();

                WSStorageClient client = WSStorageClientBuilder.builder().withUrl("0.0.0.0").withPort(12345).build();
                clientGraph = GraphBuilder.builder().withStorage(client).build();
            } else {
                String serverKeyStore = TestWSS.class.getClassLoader().getResource("server.keystore").getPath();
                String serverTrustStore = TestWSS.class.getClassLoader().getResource("server.truststore").getPath();
                WSStorageWrapper server = WSStorageWrapperBuilder.builder()
                        .withWrappedStorage(new LevelDBStorage(dbPath.toString()))
                        .withPort(12345)
                        .withSSL()
                        .withKeyStore(serverKeyStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .withTrustStore(serverTrustStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .build();
                serverGraph = GraphBuilder.builder().withStorage(server).build();


                String clientKeyStore = TestWSS.class.getClassLoader().getResource("client.keystore").getPath();
                String clientTrustStore = TestWSS.class.getClassLoader().getResource("client.truststore").getPath();
                WSStorageClient client = WSStorageClientBuilder.builder()
                        .withUrl("0.0.0.0")
                        .withPort(12345)
                        .withSSL()
                        .withKeyStore(clientKeyStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .withTrustStore(clientTrustStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .build();
                clientGraph = GraphBuilder.builder().withStorage(client).build();
            }
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @After
    public void deleteDB() {
        CountDownLatch latch = new CountDownLatch(1);
        clientGraph.disconnect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                serverGraph.disconnect(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        latch.countDown();
                    }
                });
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deleteDBDirectory(dbPath.toFile());
    }


    @Test
    public void testGetSSL() {
        init(true);
        testGet();
    }

    @Test
    public void testGetWithoutSSL() {
        init(false);
        testGet();
    }

    public void testGet() {

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
    public void testConnectionDisconnectionSSL() {
        init(true);
        testConnectionDisconnection();
    }

    @Test
    public void testConnectionDisconnectionWithoutSSL() {
        init(false);
        testConnectionDisconnection();
    }

    public void testConnectionDisconnection() {
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

    @Test
    public void testReleaseOfWSPortSSL() {
        init(false);
        testReleaseOfWSPort(true);
    }

    @Test
    public void testReleaseOfWSPortWithoutSSL() {
        init(false);
        testReleaseOfWSPort(false);
    }

    public void testReleaseOfWSPort(boolean ssl) {
        WSStorageWrapper wsServer1 = null;
        WSStorageWrapper wsServer2 = null;
        try {
            if (!ssl) {
                wsServer1 = WSStorageWrapperBuilder.builder()
                        .withPort(8484)
                        .withWrappedStorage(new LevelDBStorage("data1"))
                        .build();

                wsServer2 = WSStorageWrapperBuilder.builder()
                        .withPort(8484)
                        .withWrappedStorage(new LevelDBStorage("data2"))
                        .build();
            } else {
                String serverKeyStore = TestWSS.class.getClassLoader().getResource("server.keystore").getPath();
                String serverTrustStore = TestWSS.class.getClassLoader().getResource("server.truststore").getPath();

                wsServer1 = WSStorageWrapperBuilder.builder()
                        .withPort(8484)
                        .withWrappedStorage(new LevelDBStorage("data1"))
                        .withSSL()
                        .withKeyStore(serverKeyStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .withTrustStore(serverTrustStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .build();

                wsServer2 = WSStorageWrapperBuilder.builder()
                        .withPort(8484)
                        .withWrappedStorage(new LevelDBStorage("data2"))
                        .withSSL()
                        .withKeyStore(serverKeyStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .withTrustStore(serverTrustStore, KEYSTORE_PASSWORD, KEYSTORE_TYPE)
                        .build();

            }
        }catch (IOException io) {
            io.printStackTrace();
            Assert.fail(io.getMessage());
        }

        if(wsServer1 != null) {
            Graph server1 = GraphBuilder.builder()
                    .withStorage(wsServer1)
                    .build();


            Graph server2 = GraphBuilder.builder()
                    .withStorage(wsServer2)
                    .build();


            CountDownLatch latch = new CountDownLatch(1);

            server1.connect(new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    try {
                        server2.connect(null);
                    } catch (RuntimeException exception) {
                        try {
                            throw exception.getCause();
                        } catch (BindException throwable) {

                        } catch (Throwable e) {
                            Assert.fail("Exception throw:" + e.getMessage());
                        }
                    } catch (Exception e) {
                        Assert.fail("Exception throw:" + e.getMessage());
                    }
                }
            });


            try {
                latch.await(350, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            server1.disconnect(null);
            server2.disconnect(null);

            deleteDBDirectory(Paths.get("data1").toFile());
            deleteDBDirectory(Paths.get("data2").toFile());
        } else {
            Assert.fail("Error during websocket building");
        }
    }

    /**
     * Tests below currently does not pass due to a known bug
     * We keep it to show some known situations with unexpected behavior
     *
     */

    //    @Test
    public void testGet2() {
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

        file.delete();
    }

}
