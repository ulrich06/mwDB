package ml;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.task.NoopScheduler;

public class MeanTest {

    @Test
    public void test() {
        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KStatNode meanNode = KML.meanNode(graph.newNode(0, 0));

                meanNode.learn(0.3);
                meanNode.learn(0.7);
                Assert.assertTrue(0.5 == meanNode.avg());
                Assert.assertTrue(0.3 == meanNode.min());
                Assert.assertTrue(0.7 == meanNode.max());

                meanNode.free();

                graph.disconnect(null);
            }
        });

    }

}
