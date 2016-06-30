package org.mwg.ml.structure;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.MLPlugin;
import org.mwg.ml.common.structure.KDNode;

import java.util.Random;


/**
 * Created by assaad on 30/06/16.
 */
public class KDNodeTest {
    @Test
    public void KDInsertTest() {
        final Graph graph = new GraphBuilder()
                .withPlugin(new MLPlugin())
                .withScheduler(new NoopScheduler())
                .withMemorySize(300000)
                .withOffHeapMemory()
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KDNode test= (KDNode) graph.newTypedNode(0,0,KDNode.NAME);
                test.set(KDNode.DISTANCE_THRESHOLD,3.1);

                Node value= graph.newNode(0,0);

                int dim=3;
                double[] vec=new double[dim];
                Random rand=new Random();
                int num=10000;
                graph.save(null);

                for(int i=0;i<num;i++){
                    for(int j=0;j<dim;j++){
                        vec[j]=rand.nextDouble();
                    }

                    test.insert(vec,value,null);
                    if(i%1000==0) {
                        graph.save(null);
                        System.out.println(i+", cache :"+graph.space().available()+", nodes: "+test.get(KDNode.NUM_NODES));
                    }
                }


            }
        });
    }
}