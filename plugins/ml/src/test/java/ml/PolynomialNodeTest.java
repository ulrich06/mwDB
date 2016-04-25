package ml;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.math.matrix.KMatrix;
import org.mwg.math.matrix.blassolver.BlasMatrixEngine;
import org.mwg.math.matrix.blassolver.blas.F2JBlas;
import org.mwg.polynomial.KPolynomialNode;
import org.mwg.polynomial.PolynomialNode;
import org.mwg.core.NoopScheduler;

/**
 * Created by assaad on 08/04/16.
 */
public class PolynomialNodeTest {
    private static final int size = 100;
    private static final double precision = 0.5;

    @Test
    public void testConstant() {
        final Graph graph = GraphBuilder.builder().withFactory(new PolynomialNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                try {
                    BlasMatrixEngine bme = (BlasMatrixEngine) KMatrix.defaultEngine();
                    bme.setBlas(new F2JBlas());
                } catch (Exception ignored) {

                }


                long[] times = new long[size];
                double[] values = new double[size];
                //test degree 0
                for (int i = 0; i < size; i++) {
                    times[i] = i * 10 + 5000;
                    values[i] = 42.0;
                }
                testPoly(times, values, 1, graph);

                //test degree 1
                for (int i = 0; i < size; i++) {
                    values[i] = 3 * i - 20;
                }
                testPoly(times, values, 1, graph);

                //test degree 2
                for (int i = 0; i < size; i++) {
                    values[i] = 3 * i * i - 99 * i - 20;

                }
                testPoly(times, values, 1, graph);

                //test degree 5
                for (int i = 0; i < size; i++) {
                    values[i] = 2 * i * i * i * i * i - 1000 * i - 100000;
                }
                testPoly(times, values, 7, graph);

            }
        });
    }


    public static void testPoly(long[] times, double[] values, int numOfPoly, final Graph graph) {
        PolynomialNode polynomialNode = (PolynomialNode) graph.newNode(0, times[0], "PolynomialNode");
        polynomialNode.setPrecision(precision);

        for (int i = 0; i < size; i++) {
            final int ia = i;
            polynomialNode.jump(times[ia], new Callback<KPolynomialNode>() {
                @Override
                public void on(KPolynomialNode result) {
                    result.set(values[ia]);
                }
            });
        }

        for (int i = 0; i < size; i++) {
            final int ia = i;
            polynomialNode.jump(times[ia], new Callback<KPolynomialNode>() {
                @Override
                public void on(KPolynomialNode result) {
                    double v = result.get();
                    Assert.assertTrue(Math.abs(values[ia] - v) <= precision);
                }
            });
        }

        polynomialNode.timepoints(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, new Callback<long[]>() {
            @Override
            public void on(long[] result) {
                Assert.assertTrue(result.length == numOfPoly);
            }
        });
    }

}
