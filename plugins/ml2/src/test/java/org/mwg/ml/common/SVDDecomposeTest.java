package org.mwg.ml.common;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.MatrixEngine;
import org.mwg.ml.common.matrix.SVDDecompose;
import org.mwg.ml.common.matrix.blassolver.BlasMatrixEngine;
import org.mwg.ml.common.matrix.blassolver.SVD;
import org.mwg.ml.common.matrix.jamasolver.JamaMatrixEngine;


public class SVDDecomposeTest {

    /**
     * @ignore ts
     */
    @Test
    public void decompose_blas() {
        internal_decompose(new BlasMatrixEngine());
    }

    @Test
    public void decompose_jama() {
        internal_decompose(new JamaMatrixEngine());
    }

    public void internal_decompose(MatrixEngine engine) {
        int m = 20;
        int n = 30;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);

        // double[] xx = {1,0,0,0,   0,0,0,2,   0,3,0,0,    0,0,0,0   ,2,0,0,0};
        // KMatrix matA = new Matrix(xx,m,n,KMatrixType.COLUMN_BASED);

        long timestart, timeend;

        timestart = System.currentTimeMillis();
        SVDDecompose svd = engine.decomposeSVD(matA, false);
        timeend = System.currentTimeMillis();
        //System.out.println(" SVD Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");


        Matrix U = svd.getU();
        Matrix S = svd.getSMatrix();
        Matrix Vt = svd.getVt();

        Matrix res = Matrix.multiply(U, S);
        res = Matrix.multiply(res, Vt);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Assert.assertTrue(Math.abs(res.get(i, j) - matA.get(i, j)) < eps);
            }
        }
    }
}
