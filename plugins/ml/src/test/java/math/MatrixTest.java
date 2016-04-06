package math;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.blassolver.BlasMatrixEngine;
import org.mwdb.math.matrix.blassolver.LU;
import org.mwdb.math.matrix.blassolver.blas.F2JBlas;
import org.mwdb.math.matrix.blassolver.blas.NetlibBlas;
import org.mwdb.math.matrix.blassolver.QR;
import org.mwdb.math.matrix.blassolver.SVD;

/**
 * Created by assaad on 23/03/16.
 */
public class MatrixTest {

    private String blas = "Netlib";

    public MatrixTest() {
        BlasMatrixEngine be = (BlasMatrixEngine) KMatrix.defaultEngine();
        if (blas.equals("Netlib")) {
            be.setBlas(new NetlibBlas());
        } else {
            be.setBlas(new F2JBlas());
        }
    }

    public KMatrix manualMultpily(KMatrix matA, KMatrix matB) {
        KMatrix matC = new KMatrix(null, matA.rows(), matB.columns());

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matB.columns(); j++) {
                for (int k = 0; k < matA.columns(); k++) {
                    matC.add(i, j, matA.get(i, k) * matB.get(k, j));
                }
            }
        }

        return matC;

    }



    @Test
    public void MatrixMult() {
        //Test matrix mult
        int r = 500;
        int o = 300;
        int p = 700;
        KMatrix matA = KMatrix.random(r, o, 0, 100);
        KMatrix matB = KMatrix.random(o, p, 0, 100);
        long startTime, endTime;
        double d;

        startTime = System.nanoTime();
        KMatrix matC = KMatrix.multiply(matA, matB);
        endTime = System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println(blas + " mult: " + d + " ms");

        startTime = System.nanoTime();
        KMatrix matD = manualMultpily(matA, matB);
        endTime = System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Traditional mult: " + d + " ms");

        double eps = 1e-7;

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < p; j++) {
                Assert.assertTrue(Math.abs(matC.get(i, j) - matD.get(i, j)) < eps);
            }
        }
    }

    @Test
    public void MatrixInvert() {
        int m = 1000;
        int times = 1;
        int[] dimA = {m, m};
        double eps = 1e-7;

        KMatrix matA = KMatrix.random(m, m, 0, 100);

        KMatrix res = null;

        long timestart, timeend;
        // SimpleMatrix resEjml =new SimpleMatrix(1,1);

        timestart = System.currentTimeMillis();
        for (int k = 0; k < times; k++) {
            res = KMatrix.invert(matA, false);
        }
        timeend = System.currentTimeMillis();
        System.out.println(blas + " invert " + ((double) (timeend - timestart)) / (1000 * times) + " s");

        KMatrix id = KMatrix.multiply(matA, res);

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

        KMatrix matA = KMatrix.random(m, n, 0, 100);

        //double[] xx = {1,2,3,2,-4,-9,3,6,-3};
        //KMatrix matA = new Matrix(xx,m,n,KMatrixType.COLUMN_BASED);


        LU dlu = new LU(m, n, ((BlasMatrixEngine) KMatrix.defaultEngine()).getBlas());
        long timestart, timeend;

        timestart = System.currentTimeMillis();
        dlu.factor(matA, false);
        timeend = System.currentTimeMillis();
        System.out.println(blas + " LU Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");

        KMatrix P = dlu.getP();
        KMatrix L = dlu.getLower();
        KMatrix U = dlu.getUpper();
        KMatrix res1 = KMatrix.multiply(P, L);
        KMatrix res = KMatrix.multiply(res1, U);

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

        KMatrix matA = KMatrix.random(m, n, 0, 100);

        //double[] xx = {1,2,3,2,-4,-9,3,6,-3};
        //KMatrix matA = new Matrix(xx,r,p,KMatrixType.COLUMN_BASED);


        QR qr = new QR(m, n, ((BlasMatrixEngine) KMatrix.defaultEngine()).getBlas());
        long timestart, timeend;

        timestart = System.currentTimeMillis();
        qr.factor(matA, false);
        timeend = System.currentTimeMillis();
        System.out.println(blas + " QR Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");

        KMatrix Q = qr.getQ();
        KMatrix R = qr.getR();
        KMatrix res = KMatrix.multiply(Q, R);

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

        KMatrix matA = KMatrix.random(m, n, 0, 100);

        // double[] xx = {1,0,0,0,   0,0,0,2,   0,3,0,0,    0,0,0,0   ,2,0,0,0};
        // KMatrix matA = new Matrix(xx,m,n,KMatrixType.COLUMN_BASED);


        SVD svd = new SVD(m, n, ((BlasMatrixEngine) KMatrix.defaultEngine()).getBlas());


        long timestart, timeend;

        timestart = System.currentTimeMillis();
        svd.factor(matA,false);
        timeend = System.currentTimeMillis();
        System.out.println(blas + " SVD Factorizarion " + ((double) (timeend - timestart)) / 1000 + " s");


        KMatrix U = svd.getU();
        KMatrix S = svd.getSMatrix();
        KMatrix Vt = svd.getVt();

        KMatrix res = KMatrix.multiply(U, S);
        res = KMatrix.multiply(res, Vt);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Assert.assertTrue(Math.abs(res.get(i, j) - matA.get(i, j)) < eps);
            }
        }
    }

    @Test
    public void MatrixPseudoInv(){
        int m = 3;
        int n= 2;
        int[] dimA = {m, m};
        double eps = 1e-7;

        //KMatrix matA = Matrix.random(m, n, 0, 100);

        //double[] dataA={4,3,3,2};
        double[] dataA={4,3,6,3,2,4};
        KMatrix matA= new KMatrix(dataA,m,n);

        KMatrix res = null;

        long timestart, timeend;
        // SimpleMatrix resEjml =new SimpleMatrix(1,1);

        timestart = System.currentTimeMillis();
            res = KMatrix.pinv(matA, false);
        timeend = System.currentTimeMillis();
        System.out.println(blas + " pseudo inv " + ((double) (timeend - timestart)) / (1000 ) + " s");

        KMatrix id = KMatrix.multiply(res, matA);

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
