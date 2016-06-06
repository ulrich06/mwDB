package ml.regression;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.ml.common.AbstractMLNode;

import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 5/10/2016.
 */
public class LinearRegressionBatchGDNodeTest extends AbstractLinearRegressionTest{
    @Test
    public void testNormalBatchGDIterationCountStop() {
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionBatchGDNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionBatchGDNode lrNode = (LinearRegressionBatchGDNode) graph.newTypedNode(0, 0, LinearRegressionBatchGDNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 50);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.01);
                lrNode.setLearningRate(0.0001);
                lrNode.setIterationCountThreshold(10000);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                AbstractLinearRegressionTest.RegressionJumpCallback rjc = runRandom(lrNode, 100);

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
                LinearRegressionBatchGDNode lrNode = (LinearRegressionBatchGDNode) graph.newTypedNode(0, 0, LinearRegressionBatchGDNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 10);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1e-6);
                lrNode.setLearningRate(0.0001);
                lrNode.removeIterationCountThreshold();
                lrNode.setIterationErrorThreshold(1e-11);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                AbstractLinearRegressionTest.RegressionJumpCallback rjc = runRandom(lrNode, 16);

                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < 1e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 1) < 1e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bufferError < 1e-6);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.l2Reg < 1e-6);
            }
        });

    }
}
