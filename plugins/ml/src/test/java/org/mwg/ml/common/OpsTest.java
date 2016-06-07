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

    int exec=1000;
    boolean enablebench=false;
    int dim=3;

    @Test
    public void decompose_blas() {
        MatrixEngine engine = new BlasMatrixEngine();
        MatrixSVD(engine);
        MatrixInvert(engine);
        MatrixLU(engine);
        MatrixQR(engine);
        MatrixPseudoInv(engine);

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixSVD(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("NETLIB SVD "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixInvert(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("NETLIB Invert "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixLU(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("NETLIB LU "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixQR(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("NETLIB QR "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixPseudoInv(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("NETLIB PseudoInv "+ res);
            System.out.println();
        }

    }

    @Test
    public void decompose_jama() {
        MatrixEngine engine = new JamaMatrixEngine();
        MatrixSVD(engine);
        MatrixInvert(engine);
        MatrixLU(engine);
        MatrixQR(engine);
        MatrixPseudoInv(engine);

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixSVD(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("JAMA SVD "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixInvert(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("JAMA Invert "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixLU(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("JAMA LU "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixQR(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("JAMA QR "+ res);
        }

        if(enablebench) {
            long start = System.currentTimeMillis();
            for (int z = 0; z < exec; z++) {
                MatrixPseudoInv(engine);
            }
            long res = System.currentTimeMillis() - start;
            System.out.println("JAMA PseudoInv "+ res);
        }
    }


    public void MatrixInvert(MatrixEngine engine) {
        double eps = 1e-7;

        Matrix matA = Matrix.random(dim, dim, 0, 100);
        Matrix res = engine.invert(matA, false);


        Matrix id = Matrix.multiply(matA, res);


        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
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
        int m = dim;
        int n = dim;
        int p = dim;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);
        Matrix matB = Matrix.random(m, p, 0, 100);

        Matrix res = engine.solveLU(matA, matB, false, TransposeType.NOTRANSPOSE);
        Matrix temp = Matrix.multiply(matA, res);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Assert.assertTrue(Math.abs(matB.get(i, j) - temp.get(i, j)) < eps);
            }
        }
    }


    public void MatrixQR(MatrixEngine engine) {
        int m = dim;
        int n = dim;
        int p = dim;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);
        Matrix matB = Matrix.random(m, p, 0, 100);


        Matrix res = engine.solveQR(matA, matB, false, TransposeType.NOTRANSPOSE);
        Matrix temp = Matrix.multiply(matA, res);


        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                Assert.assertTrue(Math.abs(matB.get(i, j) - temp.get(i, j)) < eps);
            }
        }
    }


    public void MatrixPseudoInv(MatrixEngine engine) {
        int m = dim;
        int n = dim;
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
        int m = dim;
        int n = dim;
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
