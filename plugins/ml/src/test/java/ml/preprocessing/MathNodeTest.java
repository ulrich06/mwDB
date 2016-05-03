package ml.preprocessing;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.preprocessing.MLMathNode;

public class MathNodeTest {

    @Test
    public void test() {
        Graph graph = GraphBuilder
                .builder()
                .withFactory(new MLMathNode.Factory())
                .withScheduler(new NoopScheduler())
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node node = graph.newNode(0, 0, "Math");
                node.set("$test","10/5");
                node.set("$valueSquare", "{value} ^ 2");
                node.set("value", 3.0);
                node.set("min", 5.0);
                node.set("$ops","({$valueSquare}+1)/{min}");

                node.set("$loopA","$loopB");
                node.set("$loopB","5");

                //System.out.println(node.get("value"));
                //System.out.println(node.get("$valueSquare"));
                //System.out.println(node.get("$valueSquare"));

                Assert.assertTrue(((double)node.get("$test"))==2);
                Assert.assertTrue(((double)node.get("value"))==3.0);
                Assert.assertTrue(((double)node.get("$valueSquare"))==9.0);
                Assert.assertTrue(((double)node.get("$ops"))==2.0);
                Assert.assertTrue(((double)node.get("$loopA"))==5.0);

              //  node.set("$loopB","$loopA"); //here it creates a loop when enabled
                Assert.assertTrue(((double)node.get("$loopA"))==5.0);

                graph.disconnect(null);
            }
        });


    }

}
