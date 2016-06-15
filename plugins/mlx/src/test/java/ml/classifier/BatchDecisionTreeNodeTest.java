package ml.classifier;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.algorithm.classifier.BatchDecisionTreeNode;
import org.mwg.ml.AbstractMLNode;
import org.mwg.ml.common.AbstractSlidingWindowManagingNode;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by andrey.boytsov on 13/05/16.
 */
public class BatchDecisionTreeNodeTest extends AbstractClassifierTest{

    @Test
    public void test1DdescisionTree() {
        //This test fails if there are too many errors
        final Graph graph = new GraphBuilder().addNodeType(new BatchDecisionTreeNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                BatchDecisionTreeNode bdtNode = (BatchDecisionTreeNode) graph.newTypedNode(0, 0, BatchDecisionTreeNode.NAME);
                standardSettings(bdtNode);

                ClassificationJumpCallback cjc = new ClassificationJumpCallback();
                for (int i = 0; i < dummyDataset1.length; i++) {
                    cjc.value = new double[]{dummyDataset1[i][0]};
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


    @Test
    public void test2DRandomDescisionTree() {
        //This test fails if there are too many errors
        final Graph graph = new GraphBuilder().addNodeType(new BatchDecisionTreeNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                BatchDecisionTreeNode bdtNode = (BatchDecisionTreeNode) graph.newTypedNode(0, 0, BatchDecisionTreeNode.NAME);

                bdtNode.setProperty(AbstractSlidingWindowManagingNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 0.2);
                bdtNode.setProperty(AbstractSlidingWindowManagingNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.3);
                bdtNode.set(AbstractMLNode.FROM, "f1"+ AbstractMLNode.FROM_SEPARATOR+"f2");
                bdtNode.setProperty(AbstractSlidingWindowManagingNode.BUFFER_SIZE_KEY, Type.INT, 60);

                Random rng = new Random(1);

                //Classification:
                //f1 >= 0.5: 0
                //f1 < 0.5: f2 >= 0.5: 1
                //          f2 < 0.5: 2

                ClassificationJumpCallback cjc = new ClassificationJumpCallback(new String[]{"f1", "f2"});
                for (int i = 0; i < 1000; i++) {
                    final double f1 = rng.nextDouble();
                    final double f2 = rng.nextDouble();
                    cjc.value = new double[]{f1,f2};
                    cjc.expectedClass = (f1>=0.5)?0:((f2>=0.5)?1:2);
                    cjc.expectedBootstrap = ((i>=59)?false:true);
                    bdtNode.jump(i, cjc);
                    assertTrue(i+". Expected: "+((i>=59)?false:true), cjc.errors == 0);
                }
                //Now starting to make errors:
                for (int i = 1001; i < 1019; i++) {
                    final double f1 = rng.nextDouble();
                    final double f2 = rng.nextDouble();
                    cjc.value = new double[]{f1,f2};
                    cjc.expectedClass = (f1>=0.5)?2:((f2>=0.5)?0:1);
                    cjc.expectedBootstrap = false;
                    bdtNode.jump(i, cjc);
                    assertTrue(i+". Expected: false", cjc.errors == 0);
                }
                //This should be the last drop: 19 errors of 60 value buffer (0.31...>0.3) should push us back into bootstrap
                final double f1 = rng.nextDouble();
                final double f2 = rng.nextDouble();
                cjc.value = new double[]{f1,f2};
                cjc.expectedClass = (f1>=0.5)?3:((f2>=0.5)?4:5);
                cjc.expectedBootstrap = true;
                bdtNode.jump(1019, cjc);
                assertTrue("Last: expected - true", cjc.errors == 0);


                bdtNode.free();
                graph.disconnect(null);
            }
        });
    }



}
