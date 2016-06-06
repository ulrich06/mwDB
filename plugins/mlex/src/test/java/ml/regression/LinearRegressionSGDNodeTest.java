package ml.regression;

import org.mwg.Graph;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.ml.AbstractMLNode;

import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 5/10/2016.
 */
public class LinearRegressionSGDNodeTest extends AbstractLinearRegressionTest{

    @Test
    public void testNormalSGD() {
        Graph graph = GraphBuilder.builder()
                //.withOffHeapMemory()
                //.withMemorySize(20_000)
                //.withAutoSave(10000)
                //.withStorage(new LevelDBStorage("data"))
                .withFactory(new LinearRegressionSGDNode.Factory()).withScheduler(new NoopScheduler())
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionSGDNode lrNode = (LinearRegressionSGDNode) graph.newTypedNode(0, 0, LinearRegressionSGDNode.NAME);

                final int BUFFER_SIZE = 4000;
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFFER_SIZE);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.1);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.001);
                lrNode.setLearningRate(0.01);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                AbstractLinearRegressionTest.RegressionJumpCallback rjc = runRandom(lrNode, BUFFER_SIZE+1000);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.coefs[0] - 2) < 1e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, Math.abs(rjc.intercept - 1) < 2e-3);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bufferError < eps);
                assertTrue(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.l2Reg < eps);
            }
        });
    }
}
