package ml.regression;

import org.mwg.Graph;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.mlx.MLXPlugin;
import org.mwg.mlx.algorithm.AbstractLinearRegressionNode;
import org.mwg.mlx.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.mlx.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.mlx.AbstractMLNode;

import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 5/10/2016.
 */
public class LinearRegressionSGDNodeTest extends AbstractLinearRegressionTest{

    @Test
    public void testNormalSGD() {
        final Graph graph = new GraphBuilder()
                //.withOffHeapMemory()
                //.withMemorySize(20_000)
                //.withAutoSave(10000)
                //.withStorage(new LevelDBStorage("data"))
                .withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler())
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionSGDNode lrNode = (LinearRegressionSGDNode) graph.newTypedNode(0, 0, LinearRegressionSGDNode.NAME);

                final int BUFFER_SIZE = 3000;
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFFER_SIZE);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.1);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.001);
                lrNode.setProperty(LinearRegressionBatchGDNode.LEARNING_RATE_KEY, Type.DOUBLE, 0.01);
                lrNode.set(AbstractMLNode.FROM, FEATURE);

                AbstractLinearRegressionTest.RegressionJumpCallback rjc = runRandom(lrNode, BUFFER_SIZE+500);

                lrNode.free();
                graph.disconnect(null);

                assertTrue(Math.abs(rjc.coefs[0] - 2) < 1e-3);
                assertTrue(Math.abs(rjc.intercept - 1) < 2e-3);
                assertTrue(rjc.bufferError < eps);
                assertTrue(rjc.l2Reg < eps);
            }
        });
    }
}
