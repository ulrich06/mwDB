package math.matrix;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.blassolver.BlasMatrixEngine;
import org.mwg.ml.common.matrix.blassolver.LU;
import org.mwg.ml.common.matrix.blassolver.QR;
import org.mwg.ml.common.matrix.blassolver.SVD;
import org.mwg.ml.common.matrix.blassolver.blas.F2JBlas;
import org.mwg.ml.common.matrix.blassolver.blas.NetlibBlas;

/**
 * Created by assaad on 23/03/16.
 */
public class MatrixOpsTest {

    private String blas = "Netlib";

    public MatrixOpsTest() {
        BlasMatrixEngine be = (BlasMatrixEngine) Matrix.defaultEngine();
        if (blas.equals("Netlib")) {
            be.setBlas(new NetlibBlas());
        } else {
            be.setBlas(new F2JBlas());
        }
    }


    @Test
    public void MatrixInvert() {
        int m = 1000;
        int times = 1;
        int[] dimA = {m, m};
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, m, 0, 100);

        Matrix res = null;

        long timestart, timeend;
        // SimpleMatrix resEjml =new SimpleMatrix(1,1);

        timestart = System.currentTimeMillis();
        for (int k = 0; k < times; k++) {
            res = Matrix.invert(matA, false);
        }
        timeend = System.currentTimeMillis();
        //System.out.println(blas + " invert " + ((double) (timeend - timestart)) / (1000 * times) + " s");

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

    @Test
    public void MatrixLU() {
        int m = 300;
        int n = 500;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);

        //double[] xx = {1,2,3,2,-4,-9,3,6,-3};
        //KMatrix matA = new Matrix(xx,m,n,KMatrixType.COLUMN_BASED);


        LU dlu = new LU(m, n, ((BlasMatrixEngine) Matrix.defaultEngine()).getBlas());
        long timestart, timeend;

        timestart = System.currentTimeMillis();
        dlu.factor(matA, false);
        timeend = System.currentTimeMillis();
        //System.out.println(blas + " LU Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");

        Matrix P = dlu.getP();
        Matrix L = dlu.getL();
        Matrix U = dlu.getU();
        Matrix res1 = Matrix.multiply(P, L);
        Matrix res = Matrix.multiply(res1, U);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Assert.assertTrue(Math.abs(res.get(i, j) - matA.get(i, j)) < eps);
            }
        }
    }

    @Test
    public void MatrixQR() {
        int m = 500;
        int n = 300;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);

        //double[] xx = {1,2,3,2,-4,-9,3,6,-3};
        //KMatrix matA = new Matrix(xx,r,p,KMatrixType.COLUMN_BASED);


        QR qr = new QR(m, n, ((BlasMatrixEngine) Matrix.defaultEngine()).getBlas());
        long timestart, timeend;

        timestart = System.currentTimeMillis();
        qr.factor(matA, false);
        timeend = System.currentTimeMillis();
        //System.out.println(blas + " QR Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");

        Matrix Q = qr.getQ();
        Matrix R = qr.getR();
        Matrix res = Matrix.multiply(Q, R);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Assert.assertTrue(Math.abs(res.get(i, j) - matA.get(i, j)) < eps);
            }
        }
    }

    @Test
    public void MatrixSVD() {
        int m = 400;
        int n = 500;
        double eps = 1e-7;

        Matrix matA = Matrix.random(m, n, 0, 100);

        // double[] xx = {1,0,0,0,   0,0,0,2,   0,3,0,0,    0,0,0,0   ,2,0,0,0};
        // KMatrix matA = new Matrix(xx,m,n,KMatrixType.COLUMN_BASED);


        SVD svd = new SVD(m, n, ((BlasMatrixEngine) Matrix.defaultEngine()).getBlas());


        long timestart, timeend;

        timestart = System.currentTimeMillis();
        svd.factor(matA, false);
        timeend = System.currentTimeMillis();
        //System.out.println(blas + " SVD Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");


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

    @Test
    public void MatrixPseudoInv() {
        int m = 3;
        int n = 2;
        int[] dimA = {m, m};
        double eps = 1e-7;

        //KMatrix matA = Matrix.random(m, n, 0, 100);

        //double[] dataA={4,3,3,2};
        double[] dataA = {4, 3, 6, 3, 2, 4};
        Matrix matA = new Matrix(dataA, m, n);

        Matrix res = null;

        long timestart, timeend;
        // SimpleMatrix resEjml =new SimpleMatrix(1,1);

        timestart = System.currentTimeMillis();
        res = Matrix.pinv(matA, false);
        timeend = System.currentTimeMillis();
        //System.out.println(blas + " pseudo inv " + ((double) (timeend - timestart)) / (1000) + " s");

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

}
