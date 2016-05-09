package ml.regression;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.ml.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.ml.common.AbstractMLNode;
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

    double coefs[] = new double[0];
    double intercept = Double.NaN;
    double bufferError = Double.NaN;
    boolean bootstrapMode = true;
    double l2Reg = Double.NaN;

    @Test
    public void testNormalPrecise() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newNode(0, 0, LinearRegressionNode.NAME);

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);

                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    final double val = dummyDataset1[i][0];
                    final double response = dummyDataset1[i][1];
                    lrNode.jump(i, new Callback<LinearRegressionNode>() {
                        @Override
                        public void on(LinearRegressionNode result) {
                            result.set("f1", val);
                            result.learn(response, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(coefs[0] - 2) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(intercept - 1) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bufferError < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, l2Reg < eps);
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

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);

                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    final double val = dummyDataset1[i][1];
                    final double response = dummyDataset1[i][0];
                    lrNode.jump(i, new Callback<LinearRegressionNode>() {
                        @Override
                        public void on(LinearRegressionNode result) {
                            result.set("f1", val);
                            result.learn(response, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(intercept + 0.5) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(coefs[0] - 0.5) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bufferError < eps);
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

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);

                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    final double val = dummyDataset1[i][0];
                    final double response = dummyDataset1[i][1];
                    lrNode.jump(i, new Callback<LinearRegressionNode>() {
                        @Override
                        public void on(LinearRegressionNode result) {
                            result.set("f1", val);
                            result.learn(response, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError, bootstrapMode);
                lrNode.jump(dummyDataset1.length, new Callback<LinearRegressionNode>() {
                    @Override
                    public void on(LinearRegressionNode result) {
                        result.set("f1", 6);
                        result.learn(1013, cb);
                        coefs = result.getCoefficients();
                        intercept = result.getIntercept();
                        bufferError = result.getBufferError();
                        bootstrapMode = result.isInBootstrapMode();
                        result.free();
                    }
                });
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(coefs[0] - 2) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(intercept - 1) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(bufferError - 166666.6666666) < eps);
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

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100);

                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };

                double resid = 0;
                lrNode.setL2Regularization(1000000000);
                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    final double val = dummyDataset1[i][0];
                    final double response = dummyDataset1[i][1];
                    lrNode.jump(i, new Callback<LinearRegressionNode>() {
                        @Override
                        public void on(LinearRegressionNode result) {
                            result.set("f1", val);
                            result.learn(response, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                    resid += (dummyDataset1[i][1] - 6) * (dummyDataset1[i][1] - 6);
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(coefs[0] - 0) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(intercept - 6) < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(bufferError - (resid / 6)) < eps);
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
                Random rng = new Random();
                rng.setSeed(1);

                LinearRegressionSGDNode lrNode = (LinearRegressionSGDNode) graph.newNode(0, 0, LinearRegressionSGDNode.NAME);

                final int BUFFER_SIZE = 8100;

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFFER_SIZE);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.1);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.001);
                lrNode.setLearningRate(0.003);

                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };

                for (int i = 0; i < BUFFER_SIZE+1000; i++) {
                    double x = rng.nextDouble() * 10;
                    lrNode.jump(i, new Callback<LinearRegressionSGDNode>() {
                        @Override
                        public void on(LinearRegressionSGDNode result) {
                            result.set("f1", x);
                            result.learn(2*x+1, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(coefs[0] - 2) < 1e-3);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(intercept - 1) < 2e-3);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bufferError < eps);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, l2Reg < eps);
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

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 50);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setLearningRate(0.0001);

                lrNode.setIterationCountThreshold(10000);
                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };


                for (int i = 0; i < 100; i++) {
                    double x = rng.nextDouble() * 100;
                    lrNode.jump(i, new Callback<LinearRegressionBatchGDNode>() {
                        @Override
                        public void on(LinearRegressionBatchGDNode result) {
                            result.set("f1", x);
                            result.learn(2*x+1, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg,Math.abs(coefs[0] - 2) < 1e-4);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg,Math.abs(intercept - 1) < 1e-4);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg,bufferError < 1e-4);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg,l2Reg < 1e-4);
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

                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 10);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setLearningRate(0.0001);

                lrNode.removeIterationCountThreshold();
                lrNode.setIterationErrorThreshold(1e-11);

                lrNode.set(AbstractMLNode.FROM, "f1");

                Callback<Boolean> cb = new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        //Nothing so far
                    }
                };

                for (int i = 0; i < 16; i++) {
                    double x = rng.nextDouble() * 100;
                    lrNode.jump(i, new Callback<LinearRegressionBatchGDNode>() {
                        @Override
                        public void on(LinearRegressionBatchGDNode result) {
                            result.set("f1", x);
                            result.learn(2*x+1, cb);
                            coefs = result.getCoefficients();
                            intercept = result.getIntercept();
                            bufferError = result.getBufferError();
                            bootstrapMode = result.isInBootstrapMode();
                            l2Reg = result.getL2Regularization();
                            result.free();
                        }
                    });
                }
                assertFalse(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bootstrapMode);

                graph.disconnect(null);

                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(coefs[0] - 2) < 1e-3);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, Math.abs(intercept - 1) < 1e-3);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, bufferError < 1e-6);
                assertTrue(intercept+"\t"+coefs[0]+"\t"+bufferError+"\t"+l2Reg, l2Reg < 1e-6);
            }
        });

    }
}
