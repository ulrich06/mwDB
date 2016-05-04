package ml;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.common.AbstractMLNode;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.NodeFactory;

public class AbstractNodeTest implements NodeFactory {

    @Override
    public String name() {
        return "AbstractNodeTest";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        return new ExMLNodeImpl(world, time, id, graph, initialResolution);
    }

    class ExMLNodeImpl extends AbstractMLNode {
        public ExMLNodeImpl(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
            super(p_world, p_time, p_id, p_graph, currentResolution);
        }
    }


    @Test
    public void test() {
        Graph graph = GraphBuilder
                .builder()
                .withFactory(this)
                .withScheduler(new NoopScheduler())
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                final double eps=1e-7;
                ExMLNodeImpl node =  (ExMLNodeImpl) graph.newNode(0,0,"AbstractNodeTest");
                node.set("test","10/5");
                node.set("valueSquare", "{value} ^ 2");
                node.set("value", 3.0);
                node.set("min", 5.0);
                node.set("ops","({$valueSquare}+1)/{min}");

                node.set("loopA","$loopB");
                node.set("loopB","5");

                //System.out.println(node.get("value"));
                //System.out.println(node.get("$valueSquare"));
                //System.out.println(node.get("$valueSquare"));

                Assert.assertTrue(((double)node.get("$test"))==2);
                Assert.assertTrue(((double)node.get("value"))==3.0);
                Assert.assertTrue(((double)node.get("$value"))==3.0);
                Assert.assertTrue(((double)node.get("$valueSquare"))==9.0);
                Assert.assertTrue((node.get("valueSquare")).equals("{value} ^ 2"));
                Assert.assertTrue(((double)node.get("$ops"))==2.0);
                Assert.assertTrue(((double)node.get("$loopA"))==5.0);

                //node.set("loopB","$loopA"); //here it creates a loop when enabled
                Assert.assertTrue(((double)node.get("$loopA"))==5.0);

                node.set("f0",0);
                node.set("f1",1);
                node.set("f2",2);
                node.set("f3",3);
                node.set("f4","f3+1");
                node.set(AbstractMLNode.FROM,"f0;f1;f2;f3;$f4");
                node.extractFeatures(new Callback<double[]>() {
                    @Override
                    public void on(double[] result) {
                        for(int i=0;i<5;i++){
                            Assert.assertTrue(result[i]==i);
                        }
                    }
                });

                node.set(AbstractMLNode.FROM,"f0^2;f1^2;f2^2;f3^2;$f4^2");
                node.extractFeatures(new Callback<double[]>() {
                    @Override
                    public void on(double[] result) {
                        for(int i=0;i<5;i++){
                            //System.out.println(result[i]);
                            Assert.assertTrue(result[i]==i*i);
                        }
                    }
                });

                graph.disconnect(null);
            }
        });


    }

}
