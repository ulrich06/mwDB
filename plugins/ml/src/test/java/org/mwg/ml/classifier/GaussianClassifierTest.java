package org.mwg.ml.classifier;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.algorithm.classifier.GaussianClassifierNode;

import static org.junit.Assert.assertTrue;


/**
 * Created by Andrey Boytsov on 4/18/2016.
 */
public class GaussianClassifierTest extends AbstractClassifierTest{
    //TODO Changing parameters on the fly

    @Test
    public void test() {
        //This test fails if there are too many errors
        Graph graph = new GraphBuilder().addNodeType(new GaussianClassifierNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                GaussianClassifierNode gaussianClassifierNode = (GaussianClassifierNode) graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);
                standardSettings(gaussianClassifierNode);
                ClassificationJumpCallback cjc = runThroughDummyDataset(gaussianClassifierNode);
                gaussianClassifierNode.free();
                graph.disconnect(null);
                assertTrue("Errors: "+cjc.errors, cjc.errors <= 1);
            }
        });
    }

}
