package ml;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.GaussianNodeFactory;
import org.mwdb.GraphBuilder;
import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.gmm.KGaussianNode;
import org.mwdb.manager.NoopScheduler;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.operation.Gaussian1D;

import java.util.Random;

/**
 * Created by assaad on 22/04/16.
 */
public class Gaussian1DTest {

    @Test
    public void Test() {
        KGraph graph = GraphBuilder.builder().withFactory(new GaussianNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KGaussianNode gaussianNodeBatch = (KGaussianNode) graph.newNode(0, 0, "GaussianNode");
                double eps = 1e-7;


                int total = 16;
                double[][] train = new double[total][1];
                Random rand = new Random();

                double sum = 0;
                double sumsquare = 0;
                for (int i = 0; i < 16; i++) {
                    train[i][0] = rand.nextDouble() * 100;
                    sum += train[i][0];
                    sumsquare += train[i][0] * train[i][0];
                }


                gaussianNodeBatch.learnBatch(train);

                double[] avgBatch = gaussianNodeBatch.getAvg();
                double[][] covBatch = gaussianNodeBatch.getCovariance(avgBatch);

                System.out.println("Avg: " + avgBatch[0] + " " + sum / total);
                System.out.println("Var: " + covBatch[0][0] + " " + Gaussian1D.getCovariance(sum, sumsquare, total));
                Assert.assertTrue(Math.abs(avgBatch[0] - sum / total) < eps);
                Assert.assertTrue(Math.abs(covBatch[0][0] - Gaussian1D.getCovariance(sum, sumsquare, total)) < eps);

                double testvec = rand.nextDouble() * 100;
                System.out.println("Prob: " + Gaussian1D.getDensity(sum, sumsquare, total, testvec) + " " + gaussianNodeBatch.getProbability(new double[]{testvec}, null, false));
                Assert.assertTrue(Math.abs(Gaussian1D.getDensity(sum, sumsquare, total, testvec) - gaussianNodeBatch.getProbability(new double[]{testvec}, null, false)) < eps);


            }
        });

    }
}
