package org.mwg.ml.common;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.MatrixEngine;
import org.mwg.ml.common.matrix.SVDDecompose;
import org.mwg.ml.common.matrix.TransposeType;
import org.mwg.ml.common.matrix.blassolver.BlasMatrixEngine;
import org.mwg.ml.common.matrix.jamasolver.JamaMatrixEngine;

public class OpsTest {


    @Test
    public void decompose_blas() {
        MatrixEngine engine = new BlasMatrixEngine();
        MatrixSVD(engine);
        MatrixInvert(engine);
        MatrixLU(engine);
        MatrixQR(engine);
        MatrixPseudoInv(engine);
    }

    @Test
    public void decompose_jama() {
        MatrixEngine engine = new JamaMatrixEngine();
        MatrixSVD(engine);
        MatrixInvert(engine);
        MatrixLU(engine);
        MatrixQR(engine);
        MatrixPseudoInv(engine);
    }


    public void MatrixInvert(MatrixEngine engine) {
        int m = 100;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, m, 0, 100);
        Matrix res = engine.invert(matA, false);
        Matrix id = Matrix.multiply(matA, res);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double x;
                if (i == j) {
                    x = 1;
                } else {
                    x = 0;
                }
                Assert.assertTrue(Math.abs(id.get(i, j) - x) < eps);
            }
        }
    }

    public void MatrixLU(MatrixEngine engine) {
        int m = 20;
        int n = 20;
        int p = 20;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);
        Matrix matB = Matrix.random(m, p, 0, 100);

        Matrix res = engine.solveQR(matA, matB, false, TransposeType.NOTRANSPOSE);
        Matrix temp = Matrix.multiply(matA, res);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Assert.assertTrue(Math.abs(matB.get(i, j) - temp.get(i, j)) < eps);
            }
        }
    }


    public void MatrixQR(MatrixEngine engine) {
        int m = 7;
        int n = 7;
        int p = 2;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);
        Matrix matB = Matrix.random(m, p, 0, 100);

        //Matrix matA = new Matrix(new double[]{1,20,3,4,5,  6,7, 20 ,5, 1 ,  8,9,10,11,12},m,n);
        //Matrix matB =  new Matrix(new double[]{5,6,7,8,10,1,2,3,5,4},m,p);

        Matrix res = engine.solveQR(matA, matB, false, TransposeType.NOTRANSPOSE);
        Matrix temp = Matrix.multiply(matA, res);


        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                Assert.assertTrue(Math.abs(matB.get(i, j) - temp.get(i, j)) < eps);
            }
        }
    }


    public void MatrixPseudoInv(MatrixEngine engine) {
        int m = 7;
        int n = 7;
        double eps = 1e-6;

        Matrix matA = Matrix.random(m, n, 0, 100);
        Matrix res = engine.pinv(matA, false);
        Matrix id = Matrix.multiply(res, matA);

        for (int i = 0; i < id.rows(); i++) {
            for (int j = 0; j < id.columns(); j++) {
                double x;
                if (i == j) {
                    x = 1;
                } else {
                    x = 0;
                }
                Assert.assertTrue(Math.abs(id.get(i, j) - x) < eps);
            }
        }
    }


    public void MatrixSVD(MatrixEngine engine) {
        int m = 30;
        int n = 20;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);

        SVDDecompose svd = engine.decomposeSVD(matA, false);

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
