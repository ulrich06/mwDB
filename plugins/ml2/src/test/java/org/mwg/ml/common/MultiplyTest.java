package org.mwg.ml.common;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.MatrixEngine;
import org.mwg.ml.common.matrix.TransposeType;
import org.mwg.ml.common.matrix.blassolver.BlasMatrixEngine;
import org.mwg.ml.common.matrix.jamasolver.JamaMatrixEngine;

/**
 * Created by assaad on 03/06/16.
 */
public class MultiplyTest {




    @Test
    public void MatrixMultBlas() {
        InternalManualMult(new BlasMatrixEngine());
    }

    @Test
    public void MatrixMultJama() {
        InternalManualMult(new JamaMatrixEngine());
    }
    

    public Matrix manualMultpily(Matrix matA, Matrix matB) {
        Matrix matC = new Matrix(null, matA.rows(), matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matB.columns(); j++) {
                for (int k = 0; k < matA.columns(); k++) {
                    matC.add(i, j, matA.get(i, k) * matB.get(k, j));
                }
            }
        }

        return matC;

    }


    public void InternalManualMult(MatrixEngine engine) {
        //Test matrix mult
        int r = 50;
        int o = 40;
        int p = 20;
        Matrix matA = Matrix.random(r, o, 0, 100);
        Matrix matB = Matrix.random(o, p, 0, 100);


        Matrix result = engine.multiplyTransposeAlphaBeta(TransposeType.NOTRANSPOSE,1,matA,TransposeType.NOTRANSPOSE,1,matB);
        Matrix matD = manualMultpily(matA, matB);

        double eps = 1e-7;

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < p; j++) {
                Assert.assertTrue(Math.abs(result.get(i, j) - matD.get(i, j)) < eps);
            }
        }
    }
}
