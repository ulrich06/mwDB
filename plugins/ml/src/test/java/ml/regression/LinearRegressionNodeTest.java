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
public class LinearRegressionNodeTest extends AbstractLinearRegressionTest {

    @Test
    public void testNormalPrecise() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);
                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.intercept - 1) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, rjc.bufferError < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, rjc.l2Reg < eps);
            }
        });

    }


    @Test
    public void testNormalPrecise2() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, true);
                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.intercept + 0.5) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.coefs[0] - 0.5) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, rjc.bufferError < eps);
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
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);

                rjc.value = 6;
                rjc.response = 1013;
                lrNode.jump(dummyDataset1.length, rjc);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, rjc.bootstrapMode);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.intercept - 1) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.bufferError - 166666.6666666) < eps);
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

                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                standardSettings(lrNode);
                lrNode.setL2Regularization(1000000000);

                RegressionJumpCallback rjc = runThroughDummyDataset(lrNode, false);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.coefs[0] - 0) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.intercept - 6) < eps);
                assertTrue(rjc.intercept + "\t" + rjc.coefs[0] + "\t" + rjc.bufferError + "\t" + rjc.l2Reg, Math.abs(rjc.bufferError - (resid / 6)) < eps);
            }
        });
    }

}
