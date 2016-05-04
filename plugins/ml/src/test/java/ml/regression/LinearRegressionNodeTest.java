package ml.regression;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.algorithm.classifier.AbstractGaussianClassifierNode;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.ml.common.AbstractSlidingWindowManagingNode;

import java.util.Random;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by youradmin on 4/28/16.
 */
public class LinearRegressionNodeTest {

    double dummyDataset1[][] = new double[][]{{0, 1}, {1, 3}, {2, 5}, {3, 7}, {4, 9}, {5, 11}};

    final double eps = 0.000001;

    @Test
    public void testNormalPrecise() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 1);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, dummyDataset1[i]);
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j=0;j<coefs.length;j++){
                //    System.out.print(coefs[j]+", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < eps);
                assertTrue(Math.abs(coefs[1] - 1) < eps);
                assertTrue(lrNode.getBufferError() < eps);
                assertTrue(lrNode.getL2Regularization() < eps);
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

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 0);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, dummyDataset1[i]);
                    //double coefs[] = lrNode.getCoefficients();
                    //System.out.print("Coefficients: ");
                    //for (int j=0;j<coefs.length;j++){
                    //    System.out.print(coefs[j]+", ");
                    //}
                    //System.out.println();
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j=0;j<coefs.length;j++){
                //    System.out.print(coefs[j]+", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] + 0.5) < eps);
                assertTrue(Math.abs(coefs[1] - 0.5) < eps);
                assertTrue(lrNode.getBufferError() < eps);
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

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 1);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, dummyDataset1[i]);
                }
                assertFalse(lrNode.isInBootstrapMode());
                lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, new double[]{6, 1013});
                assertTrue(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j = 0; j < coefs.length; j++) {
                //    System.out.print(coefs[j] + ", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < eps);
                assertTrue(Math.abs(coefs[1] - 1) < eps);
                assertTrue(Math.abs(lrNode.getBufferError() - 166666.6666666) < eps);
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
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 1);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100);

                double resid = 0;
                lrNode.setL2Regularization(1000000000);
                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, dummyDataset1[i]);
                    resid += (dummyDataset1[i][1] - 6) * (dummyDataset1[i][1] - 6);
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j = 0; j < coefs.length; j++) {
                //    System.out.print(coefs[j] + ", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 0) < eps);
                assertTrue(Math.abs(coefs[1] - 6) < eps);
                assertTrue(Math.abs(lrNode.getBufferError() - (resid / 6)) < eps);
            }
        });
    }

    @Test
    public void testNormalSGD() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionSGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Random rng = new Random();
                rng.setSeed(1);

                LinearRegressionSGDNode lrNode = (LinearRegressionSGDNode) graph.newNode(0, 0, LinearRegressionSGDNode.NAME);

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 1);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 10000);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.1);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.001);
                lrNode.setLearningRate(0.003);

                for (int i = 0; i < 11000; i++) {
                    double x = rng.nextDouble() * 10;
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, new double[]{x, 2 * x + 1});
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j = 0; j < coefs.length; j++) {
                //    System.out.print(coefs[j] + ", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < 1e-3);
                assertTrue(Math.abs(coefs[1] - 1) < 1e-3);
                assertTrue(lrNode.getBufferError() < eps);
                assertTrue(lrNode.getL2Regularization() < eps);
            }
        });
    }

    @Test
    public void testNormalBatchGDIterationCountStop() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Random rng = new Random();
                rng.setSeed(1);

                LinearRegressionBatchGDNode lrNode = (LinearRegressionBatchGDNode) graph.newNode(0, 0, LinearRegressionBatchGDNode.NAME);

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 1);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 50);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setLearningRate(0.0001);

                lrNode.setIterationCountThreshold(10000);

                for (int i = 0; i < 100; i++) {
                    double x = rng.nextDouble() * 100;
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, new double[]{x, 2 * x + 1});
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                }
                //assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j = 0; j < coefs.length; j++) {
                //    System.out.print(coefs[j] + ", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < 1e-4);
                assertTrue(Math.abs(coefs[1] - 1) < 1e-4);
                assertTrue(lrNode.getBufferError() < 1e-4);
                assertTrue(lrNode.getL2Regularization() < 1e-4);
            }
        });

    }

    @Test
    public void testNormalBatchGDErrorThresholdStop() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Random rng = new Random();
                rng.setSeed(1);

                LinearRegressionBatchGDNode lrNode = (LinearRegressionBatchGDNode) graph.newNode(0, 0, LinearRegressionBatchGDNode.NAME);

                lrNode.setProperty(AbstractLinearRegressionNode.INPUT_DIM_KEY, Type.INT, 2);
                lrNode.setProperty(AbstractLinearRegressionNode.RESPONSE_INDEX_KEY, Type.INT, 1);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 10);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setLearningRate(0.0001);

                lrNode.removeIterationCountThreshold();
                lrNode.setIterationErrorThreshold(1e-11);

                for (int i = 0; i < 16; i++) {
                    double x = rng.nextDouble() * 100;
                    lrNode.set(AbstractSlidingWindowManagingNode.FEATURES_KEY, new double[]{x, 2 * x + 1});
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                //System.out.print("Final coefficients: ");
                //for (int j = 0; j < coefs.length; j++) {
                //    System.out.print(coefs[j] + ", ");
                //}
                //System.out.println();
                //System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < 1e-3);
                assertTrue(Math.abs(coefs[1] - 1) < 1e-3);
                assertTrue(lrNode.getBufferError() < 1e-6);
                assertTrue(lrNode.getL2Regularization() < 1e-6);
            }
        });

    }
}
