package org.kevoree.modeling.addons.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.core.scheduler.NoopScheduler;

public class GetPostTest {

    @Test
    public void test() throws Exception {
        Graph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).build();
        graph.connect(o -> {
            for (int i = 0; i < 10; i++) {
                Node nodeLoop = graph.newNode(0, i);
                nodeLoop.set("name", "node" + i);
                nodeLoop.set("load", i);
                Node subProcessLoop = graph.newNode(0, i);
                subProcessLoop.set("name", "process" + i);
                subProcessLoop.set("load", i);
                nodeLoop.add("processes", subProcessLoop);
                graph.index("nodes", nodeLoop, "name", null);
            }
            RestGateway gateway = RestGateway.expose(graph, 8050);
            gateway.start();

            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get("http://localhost:8050/fromIndexAll(nodes)?time=10")
                        .header("accept", "application/json")
                        .queryString("time", System.currentTimeMillis())
                        .asJson();
                Assert.assertEquals("[{\"world\":0,\"data\":{\"processes\":[2],\"load\":0,\"name\":\"node0\"},\"time\":10,\"id\":1},{\"world\":0,\"data\":{\"processes\":[5],\"load\":1,\"name\":\"node1\"},\"time\":10,\"id\":4},{\"world\":0,\"data\":{\"processes\":[7],\"load\":2,\"name\":\"node2\"},\"time\":10,\"id\":6},{\"world\":0,\"data\":{\"processes\":[9],\"load\":3,\"name\":\"node3\"},\"time\":10,\"id\":8},{\"world\":0,\"data\":{\"processes\":[11],\"load\":4,\"name\":\"node4\"},\"time\":10,\"id\":10},{\"world\":0,\"data\":{\"processes\":[13],\"load\":5,\"name\":\"node5\"},\"time\":10,\"id\":12},{\"world\":0,\"data\":{\"processes\":[15],\"load\":6,\"name\":\"node6\"},\"time\":10,\"id\":14},{\"world\":0,\"data\":{\"processes\":[17],\"load\":7,\"name\":\"node7\"},\"time\":10,\"id\":16},{\"world\":0,\"data\":{\"processes\":[19],\"load\":8,\"name\":\"node8\"},\"time\":10,\"id\":18},{\"world\":0,\"data\":{\"processes\":[21],\"load\":9,\"name\":\"node9\"},\"time\":10,\"id\":20}]", jsonResponse.getBody().toString());

                jsonResponse = Unirest.get("http://localhost:8050/fromIndexAll(nodes)/traverse(processes)?time=10")
                        .header("accept", "application/json")
                        .queryString("time", System.currentTimeMillis())
                        .asJson();
                Assert.assertEquals("[{\"world\":0,\"data\":{\"load\":7,\"name\":\"process7\"},\"time\":10,\"id\":17},{\"world\":0,\"data\":{\"load\":0,\"name\":\"process0\"},\"time\":10,\"id\":2},{\"world\":0,\"data\":{\"load\":8,\"name\":\"process8\"},\"time\":10,\"id\":19},{\"world\":0,\"data\":{\"load\":1,\"name\":\"process1\"},\"time\":10,\"id\":5},{\"world\":0,\"data\":{\"load\":9,\"name\":\"process9\"},\"time\":10,\"id\":21},{\"world\":0,\"data\":{\"load\":2,\"name\":\"process2\"},\"time\":10,\"id\":7},{\"world\":0,\"data\":{\"load\":3,\"name\":\"process3\"},\"time\":10,\"id\":9},{\"world\":0,\"data\":{\"load\":4,\"name\":\"process4\"},\"time\":10,\"id\":11},{\"world\":0,\"data\":{\"load\":5,\"name\":\"process5\"},\"time\":10,\"id\":13},{\"world\":0,\"data\":{\"load\":6,\"name\":\"process6\"},\"time\":10,\"id\":15}]", jsonResponse.getBody().toString());

                jsonResponse = Unirest.get("http://localhost:8050/fromIndexAll(nodes)/traverse(processes)/with(name,process9)?time=10&world=0")
                        .header("accept", "application/json")
                        .queryString("time", System.currentTimeMillis())
                        .asJson();
                Assert.assertEquals("[{\"world\":0,\"data\":{\"load\":9,\"name\":\"process9\"},\"time\":10,\"id\":21}]", jsonResponse.getBody().toString());

                //world 1 should be empty
                jsonResponse = Unirest.get("http://localhost:8050/fromIndexAll(nodes)/traverse(processes)/with(name,process9)?time=10&world=1")
                        .header("accept", "application/json")
                        .queryString("time", System.currentTimeMillis())
                        .asJson();
                Assert.assertEquals("[]", jsonResponse.getBody().toString());


                //Now test the post
                Unirest.post("http://localhost:8050/fromIndexAll(nodes)/traverse(processes)/with(name,process9)?time=10&world=0")
                        .header("accept", "application/json")
                        .queryString("time", System.currentTimeMillis())
                        .body("load=42,meta=Hello").asBinary();
                jsonResponse = Unirest.get("http://localhost:8050/fromIndexAll(nodes)/traverse(processes)/with(name,process9)?time=10&world=0")
                        .header("accept", "application/json")
                        .queryString("time", System.currentTimeMillis())
                        .asJson();
                Assert.assertEquals("[{\"world\":0,\"data\":{\"load\":\"42\",\"meta\":\"Hello\",\"name\":\"process9\"},\"time\":10,\"id\":21}]", jsonResponse.getBody().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            graph.disconnect(null);
            gateway.stop();

        });
    }


}
