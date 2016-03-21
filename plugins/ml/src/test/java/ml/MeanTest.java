package ml;

import org.mwdb.KGraph;

public class MeanTest {

    public void test() {
        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
    }

}
