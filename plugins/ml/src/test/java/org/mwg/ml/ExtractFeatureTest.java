package org.mwg.ml;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;

public class ExtractFeatureTest {

    @Test
    public void test() {
        Graph graph = GraphBuilder.builder().withFactory(new NoopRegressionNodeFactory()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node domainNode = graph.newNode(0, 0);
                domainNode.set("value", 42.0);

                RegressionNode learningNode = (RegressionNode) graph.newTypedNode(0, 0, "NoopRegressionNode");
                learningNode.add("sensor", domainNode);
                learningNode.set("from", "sensor.value");
                learningNode.learn(3, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        Assert.assertEquals("{\"world\":0,\"time\":0,\"id\":2,\"sensor\":[1],\"from\":\"sensor.value\",\"extracted\":[42.0]}", learningNode.toString());
                    }
                });

                graph.disconnect(null);
            }
        });
    }

}
