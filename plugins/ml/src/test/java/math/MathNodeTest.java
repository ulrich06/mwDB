package math;

import org.junit.Test;
import org.mwdb.*;
import org.mwdb.task.NoopScheduler;

public class MathNodeTest {

    @Test
    public void test() {
        KGraph graph = GraphBuilder
                .builder()
                .withFactory(new MathNodeFactory())
                .withScheduler(new NoopScheduler())
                .build();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                KNode node = graph.newNode(0, 0, "MathNode");
                node.attSet("$valueSquare", KType.STRING, "{value} ^ 2 / {min} * 2");
                node.attSet("value", KType.DOUBLE, 3.0);
                node.attSet("min", KType.DOUBLE, 3.0);

                System.out.println(node.att("value"));
                System.out.println(node.att("$valueSquare"));
                System.out.println(node.att("$valueSquare"));

                graph.disconnect(null);
            }
        });


    }

}
