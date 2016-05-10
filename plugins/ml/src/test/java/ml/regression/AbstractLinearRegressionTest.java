package ml.regression;

import org.mwg.Callback;
import org.mwg.Type;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.common.AbstractMLNode;

import java.util.Random;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 5/10/2016.
 */
public class AbstractLinearRegressionTest {
    protected static final String FEATURE = "f1";

    protected double dummyDataset1[][] = new double[][]{{0, 1}, {1, 3}, {2, 5}, {3, 7}, {4, 9}, {5, 11}};

    final double eps = 0.000001;

    protected  class RegressionJumpCallback implements Callback<AbstractLinearRegressionNode> {
        double coefs[] = new double[0];
        double intercept = Double.NaN;
        double bufferError = Double.NaN;
        boolean bootstrapMode = true;
        double l2Reg = Double.NaN;

        public double value;
        public double response;

        Callback<Boolean> cb = new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                //Nothing so far
            }
        };

        @Override
        public void on(AbstractLinearRegressionNode result) {
            result.set(FEATURE, value);
            result.learn(response, cb);
            coefs = result.getCoefficients();
            intercept = result.getIntercept();
            bufferError = result.getBufferError();
            bootstrapMode = result.isInBootstrapMode();
            l2Reg = result.getL2Regularization();
            result.free();
        }
    };

    protected void standardSettings(AbstractLinearRegressionNode lrNode){
        lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 6);
        lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
        lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
        lrNode.set(AbstractMLNode.FROM, FEATURE);
    }

    protected RegressionJumpCallback runRandom(AbstractLinearRegressionNode lrNode, int rounds){
        Random rng = new Random();
        rng.setSeed(1);

        RegressionJumpCallback rjc = new RegressionJumpCallback();

        for (int i = 0; i < rounds; i++) {
            double x = rng.nextDouble() * 10;
            rjc.value = x;
            rjc.response = 2*x+1;
            lrNode.jump(i, rjc);
        }
        assertFalse(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bootstrapMode);

        return rjc;
    }

    protected RegressionJumpCallback runThroughDummyDataset(LinearRegressionNode lrNode, boolean swapResponse) {
        RegressionJumpCallback rjc = new RegressionJumpCallback();
        for (int i = 0; i < dummyDataset1.length; i++) {
            assertTrue(rjc.bootstrapMode);
            rjc.value = dummyDataset1[i][swapResponse?1:0];
            rjc.response = dummyDataset1[i][swapResponse?0:1];
            lrNode.jump(i, rjc);
        }
        assertFalse(rjc.intercept+"\t"+rjc.coefs[0]+"\t"+rjc.bufferError+"\t"+rjc.l2Reg, rjc.bootstrapMode);
        return rjc;
    }
}
