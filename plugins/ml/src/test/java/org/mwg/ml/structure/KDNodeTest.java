package org.mwg.ml.structure;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.ml.MLPlugin;
import org.mwg.ml.common.structure.KDNode;
import java.util.Random;

/**
 * Created by assaad on 01/07/16.
 */
public class KDNodeTest {
    @Test
    public void KDInsertTest() {
        final Graph graph = new GraphBuilder()
                .withPlugin(new MLPlugin())
                .withMemorySize(10000)
                .withOffHeapMemory()
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KDNode test = (KDNode) graph.newTypedNode(0, 0, KDNode.NAME);
                test.set(KDNode.DISTANCE_THRESHOLD, 1e-30);


                int dim = 3;
                double[] vec = new double[dim];
                Random rand = new Random();
                int num = 1000;
                graph.save(null);

                for (int i = 0; i < num; i++) {
                    double[] valuecop = new double[vec.length];
                    for (int j = 0; j < dim; j++) {
                        vec[j] = rand.nextDouble();
                        valuecop[j] = vec[j];
                    }

                    Node value = graph.newNode(0, 0);
                    value.set("value", valuecop);

                    test.insert(vec, value, null);
                    value.free();
                }

                graph.save(null);
                Assert.assertTrue((int) test.get(KDNode.NUM_NODES) == num);


                double[] key = new double[dim];
                for (int i = 0; i < dim; i++) {
                    key[i] = 0.1 * (i + 1);
                }
                test.nearestN(key, 8, new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result) {
                        Assert.assertTrue(result.length == 8);
                    }
                });

            }
        });
    }
}
