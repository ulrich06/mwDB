package ml.regression;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.ml.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.ml.common.AbstractMLNode;

import java.util.Random;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by youradmin on 4/28/16.
 */
public class LinearRegressionNodeTest {

    private static final String FEATURE = "f1";

    double dummyDataset1[][] = new double[][]{{0, 1}, {1, 3}, {2, 5}, {3, 7}, {4, 9}, {5, 11}};

    final double eps = 0.000001;

    protected  class RegressionJumpCallback implements Callback<AbstractLinearRegressionNode> {
        double coefs[] = new double[0];
        double intercept = Double.NaN;
        double bufferError = Double.NaN;
        boolean bootstrapMode = true;
        double l2Reg = Double.NaN;

        public double value;
        public double response;

        Callback<Boolean> cb = new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                //Nothing so far
            }
        };

        @Override
        public void on(AbstractLinearRegressionNode result) {
            result.set(FEATURE, value);
            result.learn(response, cb);
            coefs = result.getCoefficients();
            intercept = result.getIntercept();
            bufferError = result.getBufferError();
            bootstrapMode = result.isInBootstrapMode();
            l2Reg = result.getL2Regularization();
            result.free();
        }
    };

    private void standardSettings(AbstractLinearRegressionNode lrNode){
        lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
        lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
        lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
        lrNode.set(AbstractMLNode.FROM, FEATURE);
    }

    private RegressionJumpCallback runRandom(AbstractLinearRegressionNode lrNode, int rounds){
        Random rng = new Random();
        rng.setSeed(1);

        RegressionJumpCallback rjc = new RegressionJumpCallback();

        for (int i = 0; i < rounds; i++) {
            double x = rng.nextDouble() * 10;
            rjc.value = x;
            rjc.response = 2*x+1;
            lrNode.jump(i, rjc);
        }
        assertFalse(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bootstrapMode);

        return rjc;
    }

    private RegressionJumpCallback runThroughDummyDataset(LinearRegressionNode lrNode, boolean swapResponse) {
        RegressionJumpCallback rjc = new RegressionJumpCallback();
        for (int i = 0; i < dummyDataset1.length; i++) {
            assertTrue(rjc.bootstrapMode);
            rjc.value = dummyDataset1[i][swapResponse?1:0];
            rjc.response = dummyDataset1[i][swapResponse?0:1];
            lrNode.jump(i, rjc);
        }
        assertFalse(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bootstrapMode);
        return rjc;
    }

    @Test
    public void testNormalPrecise() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);
                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 1) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bufferError < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.l2Reg < eps);
            }
        });

    }


    @Test
    public void testNormalPrecise2() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, true);
                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept + 0.5) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 0.5) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bufferError < eps);
            }
        });

    }


    @Test
    public void testSuddenError() {
        //This test fails only on crash. Otherwise, it is just for
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);

                rjc.value = 6;
                rjc.response = 1013;
                lrNode.jump(dummyDataset1.length, rjc);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bootstrapMode);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 1) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.bufferError - 166666.6666666) < eps);
            }
        });
    }

    @Test
    public void testTooLargeRegularization() {
        //This test fails only on crash. Otherwise, it is just for
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                double resid = 0;
                for (int i = 0; i < dummyDataset1.length; i++) {
                    resid += (dummyDataset1[i][1] - 6) * (dummyDataset1[i][1] - 6);
                }

                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                lrNode.setL2Regularization(1000000000);

                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 0) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 6) < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.bufferError - (resid / 6)) < eps);
            }
        });
    }

    @Test
    public void testNormalSGD() {
        Graph graph = GraphBuilder.builder()
                //.withOffHeapMemory()
                //.withMemorySize(20_000)
                //.withAutoSave(10000)
                //.withStorage(new LevelDBStorage("data"))
                .withFactory(new LinearRegressionSGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionSGDNode lrNode = (LinearRegressionSGDNode) graph.newNode(0, 0, LinearRegressionSGDNode.NAME);

                final int BUFFER_SIZE = 8100;
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFFER_SIZE);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.1);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.001);
                lrNode.setLearningRate(0.003);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                RegressionJumpCallback rjc = runRandom(lrNode, BUFFER_SIZE+1000);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < 1e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 1) < 2e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bufferError < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.l2Reg < eps);
            }
        });
    }

    @Test
    public void testNormalBatchGDIterationCountStop() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionBatchGDNode lrNode = (LinearRegressionBatchGDNode) graph.newNode(0, 0, LinearRegressionBatchGDNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 50);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setLearningRate(0.0001);
                lrNode.setIterationCountThreshold(10000);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                RegressionJumpCallback rjc = runRandom(lrNode, 100);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg,Math.abs(rjc.coefs[0] - 2) < 1e-4);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg,Math.abs(rjc.intercept - 1) < 1e-4);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg,rjc.bufferError < 1e-4);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg,rjc.l2Reg < 1e-4);
            }
        });

    }

    @Test
    public void testNormalBatchGDErrorThresholdStop() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionBatchGDNode lrNode = (LinearRegressionBatchGDNode) graph.newNode(0, 0, LinearRegressionBatchGDNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 10);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setLearningRate(0.0001);
                lrNode.removeIterationCountThreshold();
                lrNode.setIterationErrorThreshold(1e-11);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                RegressionJumpCallback rjc = runRandom(lrNode, 16);

                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < 1e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 1) < 1e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bufferError < 1e-6);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.l2Reg < 1e-6);
            }
        });

    }
}
