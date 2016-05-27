package org.mwg;

import org.junit.Test;

public class WSServerTest {

    @Test
    public void test(){

        Graph graph = GraphBuilder.builder().withMemorySize(10000).withAutoSave(1000).build();
        WSServer graphServer = new WSServer(graph,8050);
        graphServer.start();

        try {
            Thread.sleep(10000000000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
