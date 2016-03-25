package ml;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.task.NoopScheduler;

import java.util.Random;

/**
 * Created by assaad on 25/03/16.
 */
public class GaussianProbaTest {

    @Test
    public void test1() {
        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                KGaussianNode gaussianNodeLive = KML.gaussianNode(graph.newNode(0, 0));
                KGaussianNode gaussianNodeBatch = KML.gaussianNode(graph.newNode(0, 0));

                int test=100;
                int feat=2;

                double[][] b=new double[test][feat];
                double[] v= new double[feat];
                Random random=new Random();

                for(int i=0;i<test;i++){
                    for(int j=0;j<feat;j++){
                        v[j]=random.nextDouble()*(1+100*j);
                        b[i][j]=v[j];
                    }
                    gaussianNodeLive.learn(v);
                }
                gaussianNodeBatch.learnBatch(b);

                double[] ravg=gaussianNodeBatch.getAvg();
                double[][] rcovData = gaussianNodeBatch.getCovariance(ravg);


                //Test probability calculation.
                MultivariateNormalDistribution apache = new MultivariateNormalDistribution(ravg, rcovData);
                double d = apache.density(v);
                System.out.println("apache: " + d);

                double y = gaussianNodeLive.getProbability(v, null);
                System.out.println("live: " + y);

                double z = gaussianNodeBatch.getProbability(v, null);
                System.out.println("batch: " + z);





                double[] sumLive= gaussianNodeLive.getSum();
                double[] sumsqLive = gaussianNodeLive.getSumSquares();

                double[] sumBatch = gaussianNodeBatch.getSum();
                double[] sumSqBatch = gaussianNodeBatch.getSumSquares();

                int x=0;


            }
        });
    }
}
