package ml;

import junit.framework.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.classifier.gaussian.GaussianClassifierNodeFactory;
import org.mwg.classifier.gaussiancommon.KGaussianClassifierNode;
import org.mwg.core.NoopScheduler;
import org.mwg.regression.linear.KLinearRegression;
import org.mwg.regression.linear.LinearRegressionNodeFactory;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by youradmin on 4/28/16.
 */
public class LinearRegressionNodeTest {

    double dummyDataset1[][] = new double[][]{{0,1}, {1,3}, {2,5}, {3,7}, {4,9}, {5,11}};

    final double eps = 0.000001;

    @Test
    public void test() {
        //This test fails only on crash. Otherwise, it is just for
        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KLinearRegression lrNode = (KLinearRegression) graph.newNode(0, 0, "LinearRegressionNode");

                lrNode.initialize(2,1,6,100, 100);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    assertTrue(lrNode.isInBootstrapMode());
                    lrNode.set("value", dummyDataset1[i]);
                    //double coefs[] = lrNode.getCoefficients();
                    //System.out.print("Coefficients: ");
                    //for (int j=0;j<coefs.length;j++){
                    //    System.out.print(coefs[j]+", ");
                    //}
                    //System.out.println();
                }
                assertFalse(lrNode.isInBootstrapMode());

                graph.disconnect(null);

                double coefs[] = lrNode.getCoefficients();
                System.out.print("Final coefficients: ");
                for (int j=0;j<coefs.length;j++){
                    System.out.print(coefs[j]+", ");
                }
                System.out.println();
                System.out.println("Error: " + lrNode.getBufferError());

                assertTrue(Math.abs(coefs[0]-2) < eps);
                assertTrue(Math.abs(coefs[1]-1) < eps);
                assertTrue(lrNode.getBufferError() < eps);
            }
        });
    }
}
