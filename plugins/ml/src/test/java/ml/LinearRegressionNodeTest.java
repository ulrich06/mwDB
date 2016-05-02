package ml;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.core.NoopScheduler;
import org.mwg.regression.linear.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by youradmin on 4/28/16.
 */
public class LinearRegressionNodeTest {

    double dummyDataset1[][] = new double[][]{{0,1}, {1,3}, {2,5}, {3,7}, {4,9}, {5,11}};

    final double eps = 0.000001;

    @Test
    public void testNormalPrecise() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KLinearRegression lrNode = (KLinearRegression) graph.newNode(0, 0, "LinearRegressionNode");

                lrNode.initialize(2,1,6,100, 100);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set("value", dummyDataset1[i]);
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                    //System.out.print("Coefficients: ");
                    //for (int j=0;j<coefs.length;j++){
                    //    System.out.print(coefs[j]+", ");
                    //}
                    //System.out.println();
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                System.out.print("Final coefficients: ");
                for (int j=0;j<coefs.length;j++){
                    System.out.print(coefs[j]+", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0]-2) < eps);
                assertTrue(Math.abs(coefs[1]-1) < eps);
                assertTrue(lrNode.getBufferError() < eps);
                assertTrue(lrNode.getL2Regularization() < eps);
            }
        });

    }


    @Test
    public void testNormalPrecise2() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KLinearRegression lrNode = (KLinearRegression) graph.newNode(0, 0, "LinearRegressionNode");

                lrNode.initialize(2,0,6,100, 100);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set("value", dummyDataset1[i]);
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
                System.out.print("Final coefficients: ");
                for (int j=0;j<coefs.length;j++){
                    System.out.print(coefs[j]+", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0]+0.5) < eps);
                assertTrue(Math.abs(coefs[1]-0.5) < eps);
                assertTrue(lrNode.getBufferError() < eps);
            }
        });

    }


    @Test
    public void testSuddenError() {
        //This test fails only on crash. Otherwise, it is just for
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KLinearRegression lrNode = (KLinearRegression) graph.newNode(0, 0, "LinearRegressionNode");

                lrNode.initialize(2, 1, 6, 100, 100);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set("value", dummyDataset1[i]);
                }
                assertFalse(lrNode.isInBootstrapMode());
                lrNode.set("value", new double[]{6, 1013});
                assertTrue(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                System.out.print("Final coefficients: ");
                for (int j = 0; j < coefs.length; j++) {
                    System.out.print(coefs[j] + ", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < eps);
                assertTrue(Math.abs(coefs[1] - 1) < eps);
                assertTrue(Math.abs(lrNode.getBufferError() - 166666.6666666) < eps);
            }
        });
    }

    @Test
    public void testTooLargeRegularization() {
        //This test fails only on crash. Otherwise, it is just for
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KLinearRegression lrNode = (KLinearRegression) graph.newNode(0, 0, "LinearRegressionNode");

                lrNode.initialize(2, 1, 6, 100, 100);

                double resid = 0;
                lrNode.setL2Regularization(1000000000);
                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set("value", dummyDataset1[i]);
                    resid += (dummyDataset1[i][1] - 6)*(dummyDataset1[i][1] - 6);
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                System.out.print("Final coefficients: ");
                for (int j = 0; j < coefs.length; j++) {
                    System.out.print(coefs[j] + ", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 0) < eps);
                assertTrue(Math.abs(coefs[1] - 6) < eps);
                assertTrue(Math.abs(lrNode.getBufferError() - (resid/6)) < eps);
            }
        });
    }

    @Test
    public void testNormalSGD() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionSGDNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KGradientDescentLinearRegression lrNode = (KGradientDescentLinearRegression) graph.newNode(0, 0, "LinearRegressionSGDNode");

                lrNode.initialize(2, 1, 20000, 0.01, 0.001);
                lrNode.setLearningRate(0.003);

                for (int i = 0; i < 21000; i++) {
                    double x = Math.random() * 10;
                    lrNode.set("value", new double[]{x, 2 * x + 1});
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                System.out.print("Final coefficients: ");
                for (int j = 0; j < coefs.length; j++) {
                    System.out.print(coefs[j] + ", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < eps);
                assertTrue(Math.abs(coefs[1] - 1) < eps);
                assertTrue(lrNode.getBufferError() < eps);
                assertTrue(lrNode.getL2Regularization() < eps);
            }
        });
    }

        @Test
        public void testNormalBatchGDIterationCountStop() {
            Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNodeFactory()).withScheduler(new NoopScheduler()).build();
            graph.connect(new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    KBatchGradientDescentLinearRegression lrNode = (KBatchGradientDescentLinearRegression) graph.newNode(0, 0, "LinearRegressionBatchGDNode");

                    lrNode.initialize(2, 1, 50, 0.01, 0.01);
                    lrNode.setLearningRate(0.0001);

                    lrNode.setIterationCountThreshold(10000);

                    for (int i = 0; i < 100; i++) {
                        double x = Math.random() * 100;
                        lrNode.set("value", new double[]{x, 2 * x + 1});
                        double coefs[] = lrNode.getCoefficients();
                        assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                    }
                    //assertFalse(lrNode.isInBootstrapMode());

                    graph.disconnect(null);

                    double coefs[] = lrNode.getCoefficients();
                    System.out.print("Final coefficients: ");
                    for (int j = 0; j < coefs.length; j++) {
                        System.out.print(coefs[j] + ", ");
                    }
                    System.out.println();
                    System.out.println("Error: " + lrNode.getBufferError());

                    assertTrue(Math.abs(coefs[0] - 2) < eps);
                    assertTrue(Math.abs(coefs[1] - 1) < eps);
                    assertTrue(lrNode.getBufferError() < eps);
                    assertTrue(lrNode.getL2Regularization() < eps);
                }
            });

        }

    @Test
    public void testNormalBatchGDErrorThresholdStop() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KBatchGradientDescentLinearRegression lrNode = (KBatchGradientDescentLinearRegression) graph.newNode(0, 0, "LinearRegressionBatchGDNode");

                lrNode.initialize(2, 1, 15, 1e-6, 1e-6);
                lrNode.setLearningRate(0.0001);

                lrNode.removeIterationCountThreshold();
                lrNode.setIterationErrorThreshold(1e-11);

                for (int i = 0; i < 20; i++) {
                    double x = Math.random() * 100;
                    lrNode.set("value", new double[]{x, 2 * x + 1});
                    double coefs[] = lrNode.getCoefficients();
                    assertTrue(lrNode.getIntercept() == coefs[1]); //Exactly the same
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                System.out.print("Final coefficients: ");
                for (int j = 0; j < coefs.length; j++) {
                    System.out.print(coefs[j] + ", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0] - 2) < 1e-3);
                assertTrue(Math.abs(coefs[1] - 1) < 1e-3);
                assertTrue(lrNode.getBufferError() < 1e-6);
                assertTrue(lrNode.getL2Regularization() < 1e-6);
            }
        });

    }
}
