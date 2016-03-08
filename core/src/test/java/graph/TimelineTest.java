package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.manager.NoopScheduler;
import org.mwdb.utility.PrimitiveHelper;

public class TimelineTest {

    @Test
    public void mwHeapTimelineTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph());
    }

    private void test(KGraph graph) {
        final int[] counter = {0};
        graph.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KNode node_t0 = graph.createNode(0, 0);
                //timeTree should be already filled
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        counter[0]++;
                        Assert.assertTrue(longs.length == 1);
                        Assert.assertTrue(longs[0] == 0);
                    }
                });
                //do a simple modification
                node_t0.attSet("name", KType.STRING, "MyName");
                Assert.assertTrue(node_t0.timeDephasing() == 0);
                //check the unmodified time tree
                node_t0.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new KCallback<long[]>() {
                    @Override
                    public void on(long[] longs) {
                        counter[0]++;
                        Assert.assertTrue(longs.length == 1);
                        Assert.assertTrue(longs[0] == 0);
                    }
                });


                graph.lookup(node_t0.world(), 1, node_t0.id(), new KCallback<KNode>() {
                    @Override
                    public void on(KNode node_t1) {
                        counter[0]++;
                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"data\": {\"name\": \"MyName\"}}", node_t1.toString()));
                        Assert.assertTrue(node_t1.timeDephasing() == 1); //node has a dephasing of 1 with last known state
                        node_t1.undephase();
                        Assert.assertTrue(node_t1.timeDephasing() == 0); //node should be in phase now

                        //TODO
                        System.out.println(node_t1.toString());

                        Assert.assertTrue(PrimitiveHelper.equals("{\"world\":0,\"time\":1,\"id\":1,\"data\": {\"name\": \"MyName\"}}", node_t1.toString()));
                    }
                });


            }
        });
        Assert.assertTrue(counter[0] == 3);
    }

}
