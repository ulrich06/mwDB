package ml;

import org.junit.Test;
import org.mwg.*;
import org.mwg.gmm.GaussianNode;
import org.mwg.core.NoopScheduler;

import java.util.Random;

public class GaussianNonInvertTest {
    @Test
    public void Singularity() {
        Graph graph = GraphBuilder.builder().withFactory(new GaussianNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                double[] data = new double[3];
                double[] datan = new double[4];

                Random rand = new Random();

                GaussianNode node1 =  (GaussianNode) graph.newNode(0,0,"GaussianNode");
                GaussianNode node2 =  (GaussianNode) graph.newNode(0,0,"GaussianNode");

                for (int i = 0; i < 1000; i++) {
                    data[0] = 8 + rand.nextDouble() * 4; //avg =10, [8,12]
                    data[1] = 90 + rand.nextDouble() * 20; //avg=100 [90,110]
                    data[2] = -60 + rand.nextDouble() * 20; //avg=-50 [-60,-40]

                    datan[0] = data[0];
                    datan[1] = data[1];
                    datan[2] = data[2];
                    datan[3] = 0*data[0]+0*data[1]+0*data[2];

                    node1.learn(data);
                    node2.learn(datan);
                }

                double[] avg= node1.getAvg();
                double[] avg2= node2.getAvg();

                printd(avg);
                printd(avg2);

                data[0]=10;
                data[1]=100;
                data[2]=-60;

                datan[0] = data[0];
                datan[1] = data[1];
                datan[2] = data[2];
                datan[3] =  0*data[0]+0*data[1]+0*data[2];

                double p=node1.getProbability(avg,null,false);
                double p2= node2.getProbability(avg2,null,false);
                System.out.println("p1: "+p);
                System.out.println("p2: "+p2);


            }

            private void printd(double[] avg) {
                for(double d: avg){
                    System.out.print(d+" ");
                }
                System.out.println();

            }
        });
    }
}
