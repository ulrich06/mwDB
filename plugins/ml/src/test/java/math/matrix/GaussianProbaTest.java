package math.matrix;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.core.NoopScheduler;
import org.mwg.maths.matrix.operation.Gaussian1D;
import org.mwg.ml.profiling.MLGaussianGmmNode;

import java.util.Random;

/**
 * Created by assaad on 03/05/16.
 */
public class GaussianProbaTest {
    @Test
    public void Gaussian1D() {
        Graph graph = GraphBuilder.builder().withFactory(new MLGaussianGmmNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                MLGaussianGmmNode gaussianNodeBatch = (MLGaussianGmmNode) graph.newNode(0, 0, "GaussianGmm");
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

                //System.out.println("Avg: " + avgBatch[0] + " " + sum / total);
                //System.out.println("Var: " + covBatch[0][0] + " " + Gaussian1D.getCovariance(sum, sumsquare, total));
                Assert.assertTrue(Math.abs(avgBatch[0] - sum / total) < eps);
                Assert.assertTrue(Math.abs(covBatch[0][0] - Gaussian1D.getCovariance(sum, sumsquare, total)) < eps);

                double testvec = rand.nextDouble() * 100;
                //System.out.println("Prob: " + Gaussian1D.getDensity(sum, sumsquare, total, testvec) + " " + gaussianNodeBatch.getProbability(new double[]{testvec}, null, false));
                Assert.assertTrue(Math.abs(Gaussian1D.getDensity(sum, sumsquare, total, testvec) - gaussianNodeBatch.getProbability(new double[]{testvec}, null, false)) < eps);


            }
        });

    }

    @Test
    public void MultinomialTest() {
        Graph graph = GraphBuilder.builder().withFactory(new MLGaussianGmmNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                MLGaussianGmmNode gaussianNodeLive = (MLGaussianGmmNode) graph.newNode(0, 0, "GaussianGmm");
                MLGaussianGmmNode gaussianNodeBatch = (MLGaussianGmmNode) graph.newNode(0, 0, "GaussianGmm");

                int test = 100;
                int feat = 2;

                double[][] b = new double[test][feat];
                double[] v = new double[feat];
                Random random = new Random();

                for (int i = 0; i < test; i++) {
                    for (int j = 0; j < feat; j++) {
                        v[j] = random.nextDouble() * (1 + 100 * j);
                        b[i][j] = v[j];
                    }
                    gaussianNodeLive.learn(v);
                }
                gaussianNodeBatch.learnBatch(b);

                double[] ravg = gaussianNodeBatch.getAvg();
                double[][] rcovData = gaussianNodeBatch.getCovariance(ravg);


                //Test probability calculation.
                MultivariateNormalDistribution apache = new MultivariateNormalDistribution(ravg, rcovData);

                double eps = 1e-8;
                double d = apache.density(v);
                //System.out.println("apache: " + d);

                double y = gaussianNodeLive.getProbability(v, null, false);
                //System.out.println("live: " + y);

                double z = gaussianNodeBatch.getProbability(v, null, false);
                //System.out.println("batch: " + z);


                Assert.assertTrue(Math.abs(d - y) < eps);
                Assert.assertTrue(Math.abs(d - z) < eps);


            }
        });
    }

    @Test
    public void Singularity() {
        Graph graph = GraphBuilder.builder().withFactory(new MLGaussianGmmNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                double[] data = new double[3];
                double[] datan = new double[4];

                Random rand = new Random();

                MLGaussianGmmNode node1 = (MLGaussianGmmNode) graph.newNode(0, 0, "GaussianGmm");
                MLGaussianGmmNode node2 = (MLGaussianGmmNode) graph.newNode(0, 0, "GaussianGmm");

                for (int i = 0; i < 1000; i++) {
                    data[0] = 8 + rand.nextDouble() * 4; //avg =10, [8,12]
                    data[1] = 90 + rand.nextDouble() * 20; //avg=100 [90,110]
                    data[2] = -60 + rand.nextDouble() * 20; //avg=-50 [-60,-40]

                    datan[0] = data[0];
                    datan[1] = data[1];
                    datan[2] = data[2];
                    datan[3] = 0 * data[0] + 0 * data[1] + 0 * data[2];

                    node1.learn(data);
                    node2.learn(datan);
                }

                double[] avg = node1.getAvg();
                double[] avg2 = node2.getAvg();

               // printd(avg);
                //printd(avg2);

                data[0] = 10;
                data[1] = 100;
                data[2] = -60;

                datan[0] = data[0];
                datan[1] = data[1];
                datan[2] = data[2];
                datan[3] = 0 * data[0] + 0 * data[1] + 0 * data[2];

                double p = node1.getProbability(avg, null, false);
                double p2 = node2.getProbability(avg2, null, false);
                //System.out.println("p1: " + p);
                //System.out.println("p2: " + p2);


            }

            private void printd(double[] avg) {
                for (double d : avg) {
                    System.out.print(d + " ");
                }
                System.out.println();

            }
        });
    }
}
