package math;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.NoopScheduler;

public class MathNodeTest {

    @Test
    public void test() {
        Graph graph = GraphBuilder
                .builder()
                .withFactory(new MathNodeFactory())
                .withScheduler(new NoopScheduler())
                .build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                Node node = graph.newNode(0, 0, "MathNode");
                node.set("$valueSquare", Type.STRING, "{value} ^ 2 / {min} * 2");
                node.set("value", Type.DOUBLE, 3.0);
                node.set("min", Type.DOUBLE, 3.0);

                System.out.println(node.get("value"));
                System.out.println(node.get("$valueSquare"));
                System.out.println(node.get("$valueSquare"));

                graph.disconnect(null);
            }
        });


    }

}
