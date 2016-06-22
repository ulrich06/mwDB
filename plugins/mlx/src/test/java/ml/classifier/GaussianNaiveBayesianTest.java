package ml.classifier;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.MLXPlugin;
import org.mwg.ml.algorithm.classifier.GaussianNaiveBayesianNode;

import static org.junit.Assert.assertTrue;

public class GaussianNaiveBayesianTest extends AbstractClassifierTest {

    @Test
    public void test() {
        //This test fails if there are too many errors

        final Graph graph = new GraphBuilder()
                .withPlugin(new MLXPlugin())
                .withScheduler(new NoopScheduler())
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                GaussianNaiveBayesianNode gaussianNBNode = (GaussianNaiveBayesianNode) graph.newTypedNode(0, 0, GaussianNaiveBayesianNode.NAME);
                standardSettings(gaussianNBNode);
                ClassificationJumpCallback cjc = runThroughDummyDataset(gaussianNBNode);
                gaussianNBNode.free();
                graph.disconnect(null);
                assertTrue(cjc.errors <= 3);
            }
        });
    }

}

