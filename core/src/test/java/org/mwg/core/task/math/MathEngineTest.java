package org.mwg.core.task.math;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;

import java.util.HashMap;
import java.util.Map;

public class MathEngineTest {
    @Test
    public void expression() {
        final Graph graph = new GraphBuilder().build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                //Test Plus Operation
                MathExpressionEngine engine = CoreMathExpressionEngine.parse("5+3");
                double d = engine.eval(null, null, null);
                Assert.assertTrue(d == 8);

                //Test Multiply operation and priorities
                engine = CoreMathExpressionEngine.parse("1+5*3+2");
                d = engine.eval(null, null, null);
                Assert.assertTrue(d == 18);

                //Test Division operation and priorities
                engine = CoreMathExpressionEngine.parse("10/5");
                d = engine.eval(null, null, null);
                Assert.assertTrue(d == 2);

                //Test Division by 0
                engine = CoreMathExpressionEngine.parse("10/0");
                d = engine.eval(null, null, null);


                //Test Variables
                engine = CoreMathExpressionEngine.parse("v+5");
                Map<String, Double> hashmap = new HashMap<String, Double>();
                hashmap.put("v", 20.0);
                d = engine.eval(null, null, hashmap);
                Assert.assertTrue(d == 25);


                //Test Time extraction
                engine = CoreMathExpressionEngine.parse("TIME");
                Node context = graph.newNode(0, 200);
                d = engine.eval(context, null, null);
                Assert.assertTrue(d == 200);


                //Test Time extraction
                engine = CoreMathExpressionEngine.parse("f1^2+f2*f1");
                context = graph.newNode(0, 200);
                context.set("f1", 7);
                context.set("f2", 8);
                d = engine.eval(context, null, new HashMap<String, Double>());
                Assert.assertTrue(d == 7 * 7 + 8 * 7);
            }
        });
    }
}
