package org.mwg.ws;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class WebSocketCommunicationTest {
    @AfterClass
    public static void cleanDB() {
        Path dbBath = Paths.get("data");
        if(Files.exists(dbBath)) {
            try {
                Files.walkFileTree(dbBath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void test() {
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

                        serverGraph.index("indexName",node1,"name",null);
                        serverGraph.index("indexName",node2,"name",null);
                        serverGraph.index("indexName",node3,"name",null);

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

    }
}
