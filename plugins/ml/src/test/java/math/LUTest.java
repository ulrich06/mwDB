package math;

import org.junit.Test;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.blassolver.BlasMatrixEngine;
import org.mwdb.math.matrix.blassolver.blas.F2JBlas;
import org.mwdb.math.matrix.KTransposeType;

/**
 * Created by assaad on 24/03/16.
 */
public class LUTest {
    @Test
    public void LUt() {
        double[] dataA = {1.8, 5.25, 1.58, -1.11,
                2.88, -2.95, -2.69, -0.66,
                2.05, -0.95, -2.90, -0.59,
                -0.89, -3.80, -1.04, 0.8};
        KMatrix A = new KMatrix(dataA, 4, 4);

        double[] dataB = {9.52, 24.35, 0.77, -6.22};
        KMatrix B = new KMatrix(dataB, 4, 1);

        BlasMatrixEngine blasengine = (BlasMatrixEngine) KMatrix.defaultEngine();
        blasengine.setBlas(new F2JBlas());

        KMatrix C = KMatrix.defaultEngine().solveLU(A, B, true, KTransposeType.NOTRANSPOSE);

        int x = 0;


    }
}
