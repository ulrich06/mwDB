package ml.classifier;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.algorithm.classifier.BatchDecisionTreeNode;
import org.mwg.ml.algorithm.classifier.GaussianClassifierNode;

import static org.junit.Assert.assertTrue;

/**
 * Created by andrey.boytsov on 13/05/16.
 */
public class BatchDecisionTreeNodeTest extends AbstractClassifierTest{

    @Test
    public void test1DdescisionTree() {
        //This test fails if there are too many errors
        Graph graph = GraphBuilder.builder().withFactory(new BatchDecisionTreeNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                BatchDecisionTreeNode bdtNode = (BatchDecisionTreeNode) graph.newNode(0, 0, BatchDecisionTreeNode.NAME);
                standardSettings(bdtNode);

                ClassificationJumpCallback cjc = new ClassificationJumpCallback();
                for (int i = 0; i < dummyDataset1.length; i++) {
                    cjc.value = dummyDataset1[i][0];
                    cjc.expectedClass = (int) dummyDataset1[i][1];
                    cjc.expectedBootstrap = ((i>=59)?false:true);
                    bdtNode.jump(i, cjc);
                    assertTrue(i+". Expected: "+((i>=59)?false:true), cjc.errors == 0);
                }

                bdtNode.free();
                graph.disconnect(null);
            }
        });
    }



}
