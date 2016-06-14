package org.mwg.ml.regression;

import org.junit.Test;
import org.mwg.*;
import org.mwg.ml.RegressionNode;
import org.mwg.ml.algorithm.regression.LiveLinearRegressionNode;

import java.util.Random;

/**
 * Created by assaad on 14/06/16.
 */
public class LiveLinearRegressionTest {
    @Test
    public void testRegression() {
        final Graph graph = new GraphBuilder()
                .addNodeType(new LiveLinearRegressionNode.Factory()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node MultiSensor = graph.newNode(0, 0);
                MultiSensor.set("temperature", 20);
                MultiSensor.set("humidity", 5);
                MultiSensor.set("power", 7);

                RegressionNode learningNode = (RegressionNode) graph.newTypedNode(0, 0, LiveLinearRegressionNode.NAME);
                learningNode.add("sensor", MultiSensor);
                learningNode.set("from", "sensor.temperature; sensor.humidity; sensor.power");
                learningNode.set(LiveLinearRegressionNode.ALPHA_KEY, 0.01);
                MultiSensor.add("regression", learningNode);

                final Random random = new Random();
                final double coef[] = {2, -3, 0.001, -3};

                for (int i = 0; i < 500; i++) {
                    MultiSensor.jump(i + 1, new Callback<Node>() {
                        @Override
                        public void on(Node result) {
                            double temp = random.nextDouble() * 10 + 15; //random between 15 and 25
                            double humidity = random.nextDouble() * 5 + 5; //random between 5 and 10
                            double power = random.nextDouble() * 600 + 200; //random between 200 and 800

                            final double corr = temp * coef[0] + humidity * coef[1] + power * coef[2] + coef[3];

                            result.set("temperature", temp);
                            result.set("humidity", humidity);
                            result.set("power", power);

                            result.rel("regression", new Callback<Node[]>() {
                                @Override
                                public void on(Node[] result) {
                                    RegressionNode regressionNode = (RegressionNode) (result[0]);
                                    regressionNode.learn(corr, null);
                                    regressionNode.free();
                                }
                            });

                            result.free();
                        }
                    });

                }

                final double[] weights = new double[coef.length];
                learningNode.jump(Constants.END_OF_TIME, new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        double[] w =(double[])result.get(LiveLinearRegressionNode.WEIGHT_KEY);
                        for(int i=0;i<w.length;i++){
                            weights[i]=w[i];
                        }
                        result.free();
                    }
                });

                double max=0;
                for(int i=0;i<weights.length;i++){
                    max=Math.max(max,Math.abs(weights[i]-coef[i]));
                }
                System.out.println(max);
                graph.disconnect(null);
            }
        });

    }
}
