package ml.profiling;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.ml.algorithm.profiling.GaussianGmmNode;
import org.mwg.core.NoopScheduler;

import java.util.Random;

/**
 * Created by assaad on 01/04/16.
 */
public class GaussianMixtureModelTest {
    @Test
    public void mixtureTest() {
        Graph graph = GraphBuilder.builder().withFactory(new GaussianGmmNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                double[] data = new double[3];
                Random rand = new Random();

                GaussianGmmNode node1 = (GaussianGmmNode) graph.newNode(0, 0, "GaussianGmm");
                node1.configMixture(1, 100);

                for (int i = 0; i < 220; i++) {
                    data[0] = 8 + rand.nextDouble() * 4; //avg =10, [8,12]
                    data[1] = 90 + rand.nextDouble() * 20; //avg=100 [90,110]
                    data[2] = -60 + rand.nextDouble() * 20; //avg=-50 [-60,-40]

                    node1.learn(new Callback<Boolean>() {
                        @Override
                        public void on(Boolean result) {

                        }
                    });
                }
            }
        });
    }
}
