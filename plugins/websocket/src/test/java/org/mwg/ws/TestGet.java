package org.mwg.ws;

import org.mwg.*;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by ludovicmouline on 09/05/16.
 */
public class TestGet {
    private static Graph serverGraph;
    private static Graph clientGraph;
    private static long seed = 8080L;
    private static Random random;

    public static void main(String[] args) {
        serverGraph = GraphBuilder.builder().withStorage(new WSStorageWrapper(new LevelDBStorage("data"),12345))
                .build();

        clientGraph = GraphBuilder.builder().withStorage(new WSStorageClient("0.0.0.0",12345)).build();
        random = new Random(seed);

        final long world = random.nextLong();
        final long time = random.nextLong();
        System.out.println("Test with world=" + world + " and time=" + time);

        serverGraph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node n1 = serverGraph.newNode(world,time);
                n1.set("name","node1");
                n1.set("value",1);
                serverGraph.index("indexName", n1, new String[]{"name"},null);

                serverGraph.save(new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        clientGraph.connect(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                assertEquals(true,result);

                                clientGraph.all(world, time, "indexName", new Callback<Node[]>() {
                                    @Override
                                    public void on(Node[] result) {
                                        System.out.println(Arrays.toString(result));
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
