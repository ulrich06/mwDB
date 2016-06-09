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
        Graph graph = GraphBuilder.builder().withFactory(new NoopRegressionNode.NoopRegressionNodeFactory()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node domainNode = graph.newNode(0, 0);
                domainNode.set("value", 42.2);

                RegressionNode learningNode = (RegressionNode) graph.newTypedNode(0, 0, "NoopRegressionNode");
                learningNode.add("sensor", domainNode);
                learningNode.set("from", "sensor.value");
                learningNode.learn(3, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        Assert.assertEquals("{\"world\":0,\"time\":0,\"id\":2,\"sensor\":[1],\"from\":\"sensor.value\",\"extracted\":[42.2]}", learningNode.toString());
                    }
                });

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void testMath() {
        Graph graph = GraphBuilder.builder().withFactory(new NoopRegressionNode.NoopRegressionNodeFactory()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node domainNode = graph.newNode(0, 0);
                domainNode.set("value", 2.5);

                RegressionNode learningNode = (RegressionNode) graph.newTypedNode(0, 0, "NoopRegressionNode");
                learningNode.add("sensor", domainNode);
                learningNode.set("from", "sensor.math(value*3)");
                learningNode.learn(3, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        Assert.assertEquals("{\"world\":0,\"time\":0,\"id\":2,\"sensor\":[1],\"from\":\"sensor.math(value*3)\",\"extracted\":[7.5]}", learningNode.toString());
                    }
                });

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void testMathEscaped() {
        Graph graph = GraphBuilder.builder().withFactory(new NoopRegressionNode.NoopRegressionNodeFactory()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node domainNode = graph.newNode(0, 0);
                domainNode.set("value", 2.5);

                RegressionNode learningNode = (RegressionNode) graph.newTypedNode(0, 0, "NoopRegressionNode");
                learningNode.add("sensor", domainNode);
                learningNode.set("from", "sensor.math('value*3')");
                learningNode.learn(3, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        Assert.assertEquals("{\"world\":0,\"time\":0,\"id\":2,\"sensor\":[1],\"from\":\"sensor.math('value*3')\",\"extracted\":[7.5]}", learningNode.toString());
                    }
                });

                graph.disconnect(null);
            }
        });
    }

}
