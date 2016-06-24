package ml.regression;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.AbstractMLNode;
import org.mwg.mlx.MLXPlugin;
import org.mwg.mlx.algorithm.AbstractLinearRegressionNode;
import org.mwg.mlx.algorithm.regression.LinearRegressionWithPeriodicityNode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinearRegressionPeriodicNodeTest extends AbstractLinearRegressionTest {

    protected RegressionJumpCallback runThroughDummyDatasetWithPeriodicComponent(LinearRegressionWithPeriodicityNode lrNode, boolean swapResponse, double periods[], double sinPhaseShifts[], double amplitudes[]) {
        RegressionJumpCallback rjc = new RegressionJumpCallback(new String[]{FEATURE});
        for (int i = 0; i < 10; i++) {
            //assertTrue(rjc.bootstrapMode);
            rjc.value = new double[]{i};
            rjc.response = 2*i+1;
            for (int j=0;j<periods.length;j++){
                rjc.response = rjc.response + amplitudes[j]*Math.sin(2*Math.PI*rjc.value[0]/periods[j] + sinPhaseShifts[j]);
            }
            lrNode.jump(i, new Callback<Node>() {
                @Override
                public void on(Node result) {
                    rjc.on((AbstractLinearRegressionNode) result);
                }
            });
        }
        //assertFalse(rjc.bootstrapMode);
        return rjc;
    }

    @Test
    public void testNormalPreciseNoPhaseShift() {
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionWithPeriodicityNode lrNode = (LinearRegressionWithPeriodicityNode) graph.newTypedNode(0, 0, LinearRegressionWithPeriodicityNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 80);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.set(AbstractMLNode.FROM, FEATURE);
                lrNode.setProperty(LinearRegressionWithPeriodicityNode.PERIODS_LIST_KEY, Type.DOUBLE_ARRAY, new double[]{5.0});
                lrNode.setProperty(LinearRegressionWithPeriodicityNode.TIME_FEATURE_KEY, Type.STRING, FEATURE);

                RegressionJumpCallback rjc = runThroughDummyDatasetWithPeriodicComponent(lrNode, false, new double[]{5.0}, new double[]{0.0}, new double[]{10.0});
                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.sinComponents.length==1);
                assertTrue(rjc.cosComponents.length==1);
                assertTrue(Math.abs(rjc.sinComponents[0] - 10) < eps);
                assertTrue(Math.abs(rjc.cosComponents[0] - 0) < eps);

                assertTrue(Math.abs(rjc.coefs[0] - 2) < eps);
                assertTrue(Math.abs(rjc.intercept - 1) < eps);

                assertTrue(rjc.bufferError+"\t"+rjc.sinComponents[0]+"\t"+rjc.cosComponents[0], rjc.bufferError < eps);
                assertTrue(rjc.l2Reg < eps);
            }
        });

    }

    //@Test
    public void testNormalPrecisePhaseShiftToCos() {
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionWithPeriodicityNode lrNode = (LinearRegressionWithPeriodicityNode) graph.newTypedNode(0, 0, LinearRegressionWithPeriodicityNode.NAME);
                standardSettings(lrNode);
                //RegressionJumpCallback rjc = runThroughDummyDatasetWithPeriodicComponent(lrNode, false);
                RegressionJumpCallback rjc = null;
                lrNode.free();
                graph.disconnect(null);

                assertTrue(Math.abs(rjc.coefs[0] - 2) < eps);
                assertTrue(Math.abs(rjc.intercept - 1) < eps);
                assertTrue(rjc.bufferError < eps);
                assertTrue(rjc.l2Reg < eps);
            }
        });

    }

    //@Test
    public void testNormalPrecise2() {
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionWithPeriodicityNode lrNode = (LinearRegressionWithPeriodicityNode) graph.newTypedNode(0, 0, LinearRegressionWithPeriodicityNode.NAME);
                standardSettings(lrNode);
                //RegressionJumpCallback rjc = runThroughDummyDatasetWithPeriodicComponent(lrNode, true);
                RegressionJumpCallback rjc = null;
                lrNode.free();
                graph.disconnect(null);

                assertTrue(Math.abs(rjc.intercept + 0.5) < eps);
                assertTrue(Math.abs(rjc.coefs[0] - 0.5) < eps);
                assertTrue(rjc.bufferError < eps);
            }
        });

    }


    //@Test
    public void testSuddenError() {
        //This test fails only on crash. Otherwise, it is just for
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionWithPeriodicityNode lrNode = (LinearRegressionWithPeriodicityNode) graph.newTypedNode(0, 0, LinearRegressionWithPeriodicityNode.NAME);
                standardSettings(lrNode);
                //RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);
                RegressionJumpCallback rjc = null;

                rjc.value = new double[]{6};
                rjc.response = 1013;
                lrNode.jump(dummyDataset1.length, new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        rjc.on((AbstractLinearRegressionNode) result);
                    }
                });
                assertTrue(rjc.bootstrapMode);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(Math.abs(rjc.coefs[0]  - 144.9) < 1);
                assertTrue(Math.abs(rjc.intercept + 332.8) < 1);
                assertTrue(Math.abs(rjc.bufferError - 79349.32) < 20);
            }
        });
    }

    //@Test
    public void testTooLargeRegularization() {
        //This test fails only on crash. Otherwise, it is just for
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                double resid = 0;
                for (int i = 0; i < dummyDataset1.length; i++) {
                    resid += (dummyDataset1[i][1] - 6) * (dummyDataset1[i][1] - 6);
                }

                LinearRegressionWithPeriodicityNode lrNode = (LinearRegressionWithPeriodicityNode) graph.newTypedNode(0, 0, LinearRegressionWithPeriodicityNode.NAME);
                standardSettings(lrNode);
                lrNode.setProperty(LinearRegressionWithPeriodicityNode.L2_COEF_KEY, Type.DOUBLE, 1000000000.0);

                //RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);
                RegressionJumpCallback rjc = null;

                lrNode.free();
                graph.disconnect(null);

                assertTrue(Math.abs(rjc.coefs[0] - 0) < eps);
                assertTrue(Math.abs(rjc.intercept - 6) < eps);
                assertTrue(Math.abs(rjc.bufferError - (resid / 6)) < eps);
            }
        });
    }

}
