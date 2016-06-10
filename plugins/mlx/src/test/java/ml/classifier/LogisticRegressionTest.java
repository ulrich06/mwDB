package ml.classifier;

/**
 * Created by andrey.boytsov on 17/05/16.
 */
/**
 * Created by Andrey Boytsov on 4/18/2016.
 */

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.algorithm.classifier.LogisticRegressionClassifierNode;
import org.mwg.ml.AbstractMLNode;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by Andrey Boytsov on 4/18/2016.
 */
public class LogisticRegressionTest extends AbstractClassifierTest{
    //TODO Changing parameters on the fly

    //@Test
    public void test() {
        //This test fails if there are too many errors
        Graph graph = new GraphBuilder().addNodeType(new LogisticRegressionClassifierNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LogisticRegressionClassifierNode lrNode = (LogisticRegressionClassifierNode) graph.newTypedNode(0, 0, LogisticRegressionClassifierNode.NAME);
                lrNode.setProperty(LogisticRegressionClassifierNode.BUFFER_SIZE_KEY, Type.INT, 60);
                lrNode.setProperty(LogisticRegressionClassifierNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.2);
                lrNode.setProperty(LogisticRegressionClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.3);
                lrNode.setLearningRate(0.001); //0.001 - looks correct
                lrNode.setIterationCountThreshold(1000000);
                lrNode.set(AbstractMLNode.FROM, FEATURE);
                //lrNode.setL2Regularization(10);

                ClassificationJumpCallback cjc = runThroughDummyDataset(lrNode);
                lrNode.free();
                graph.disconnect(null);
                assertTrue("Errors: "+cjc.errors, cjc.errors <= 1);
            }
        });
    }

    @Test
    public void testRandomGen1D() {
        //This test fails if there are too many errors
        Graph graph = new GraphBuilder().addNodeType(new LogisticRegressionClassifierNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LogisticRegressionClassifierNode lrNode = (LogisticRegressionClassifierNode) graph.newTypedNode(0, 0, LogisticRegressionClassifierNode.NAME);
                lrNode.setProperty(LogisticRegressionClassifierNode.BUFFER_SIZE_KEY, Type.INT, 60);
                lrNode.setProperty(LogisticRegressionClassifierNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.2);
                lrNode.setProperty(LogisticRegressionClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.3);
                lrNode.setLearningRate(0.01); //0.001 - looks correct
                lrNode.setIterationCountThreshold(1000);
                lrNode.set(AbstractMLNode.FROM, FEATURE);
                //lrNode.setL2Regularization(10);

                ClassificationJumpCallback cjc = new ClassificationJumpCallback();

                Random rng = new Random(1);

                for (int i = 0; i < 1000; i++) {
                    double x = rng.nextDouble()*10;

                    cjc.value = new double[]{x};
                    cjc.expectedClass = (x > 5)?1:0;
                    cjc.expectedBootstrap = (i>=59)?false:true;
                    lrNode.jump(i, cjc);
                    assertTrue(i+ ". Errors: "+cjc.errors, cjc.errors == 0);
                }


                lrNode.free();
                graph.disconnect(null);
            }
        });
    }

}
