package org.mwg.ml.algorithm.regression;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.TransposeType;
import org.mwg.ml.common.matrix.operation.PInvSVD;
import org.mwg.plugin.NodeFactory;

/**
 * Created by andre on 4/26/2016.
 */
public class LinearRegressionNode extends AbstractLinearRegressionNode {

    public static final String NAME = "LinearRegressionBatch";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            LinearRegressionNode newNode = new LinearRegressionNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }

    public LinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected void updateModelParameters(double[] value) {
        //Value should be already added to buffer by that time
        final double currentBuffer[] = getValueBuffer();
        final double reshapedValue[] = new double[currentBuffer.length];
        final int dims = getInputDimensions();
        final int bufferLength = getCurrentBufferLength();
        final int respIndex = getResponseIndex();

        final double y[] = new double[bufferLength];

        final double l2 = getL2Regularization();

        //Step 1. Re-arrange to column-based format.
        for (int i=0;i<bufferLength;i++){
            for (int j=0;j<dims;j++){
                //Intercept goes instead of response value of the matrix
                if (j==respIndex){
                    reshapedValue[j*bufferLength+i] = 1;
                    y[i] = currentBuffer[i*dims+j];
                }else{
                    reshapedValue[j*bufferLength+i] = currentBuffer[i*dims+j];
                }
            }
        }

        Matrix xMatrix = new Matrix(reshapedValue, bufferLength, dims);
        Matrix yVector = new Matrix(y, bufferLength, 1);

        // inv(Xt * X - lambda*I) * Xt * ys
        // I - almost identity, but with 0 for intercept term
        Matrix xtMulX = Matrix.multiplyTransposeAlphaBeta
                (TransposeType.TRANSPOSE, 1, xMatrix, TransposeType.NOTRANSPOSE, 0, xMatrix);

        for (int i=0;i<dims;i++){
            xtMulX.add(i,i,(i!=respIndex)?l2:0);
        }

        PInvSVD pinvsvd = new PInvSVD();
        pinvsvd.factor(xtMulX,false);
        Matrix pinv=pinvsvd.getPInv();

        Matrix invMulXt = Matrix.multiplyTransposeAlphaBeta
                (TransposeType.NOTRANSPOSE, 1, pinv, TransposeType.TRANSPOSE, 0, xMatrix);

        Matrix result = Matrix.multiplyTransposeAlphaBeta
                (TransposeType.NOTRANSPOSE, 1, invMulXt, TransposeType.NOTRANSPOSE, 0, yVector);

        final double newCoefficients[] = new double[dims];
        for (int i=0;i<dims;i++){
            newCoefficients[i] = result.get(i, 0);
        }
        setCoefficients(newCoefficients);
    }
}
