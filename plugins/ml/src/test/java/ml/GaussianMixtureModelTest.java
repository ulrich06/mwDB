package ml;

import org.junit.Test;
import org.mwdb.GraphBuilder;
import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.KML;
import org.mwdb.gmm.KGaussianNode;
import org.mwdb.task.NoopScheduler;

import java.util.Random;

/**
 * Created by assaad on 01/04/16.
 */
public class GaussianMixtureModelTest {
    @Test
    public void mixtureTest() {
        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                double[] data = new double[3];
                Random rand = new Random();

                KGaussianNode node1 = KML.gaussianNode(graph.newNode(0, 0));
                node1.configMixture(1,10);

                for (int i = 0; i < 1000; i++) {
                    data[0] = 8 + rand.nextDouble() * 4; //avg =10, [8,12]
                    data[1] = 90 + rand.nextDouble() * 20; //avg=100 [90,110]
                    data[2] = -60 + rand.nextDouble() * 20; //avg=-50 [-60,-40]

                    node1.learn(data);
                }
            }
        });
    }
}
