package math.matrix;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.math.matrix.KMatrix;
import org.mwg.math.matrix.blassolver.BlasMatrixEngine;
import org.mwg.math.matrix.blassolver.blas.F2JBlas;
import org.mwg.math.matrix.blassolver.blas.KBlas;
import org.mwg.math.matrix.blassolver.blas.NetlibBlas;

/**
 * Created by assaad on 25/03/16.
 */
public class MatrixMultTest {


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
        int o = 400;
        int p = 200;
        KMatrix matA = KMatrix.random(r, o, 0, 100);
        KMatrix matB = KMatrix.random(o, p, 0, 100);
        long startTime, endTime;
        double d;

        KBlas blas = new NetlibBlas();
        BlasMatrixEngine blasengine = (BlasMatrixEngine) KMatrix.defaultEngine();
        blasengine.setBlas(blas);


        startTime = System.nanoTime();
        KMatrix matNetlib = KMatrix.multiply(matA, matB);
        endTime = System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        //System.out.println("Netlib mult: " + d + " ms");


        blas = new F2JBlas();
        blasengine.setBlas(blas);
        startTime = System.nanoTime();
        KMatrix matF2J = KMatrix.multiply(matA, matB);
        endTime = System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        //System.out.println("F2J mult: " + d + " ms");

/*
        blas = new CudaBlas();
        blasengine.setBlas(blas);
        startTime = System.nanoTime();
        KMatrix matCuda = KMatrix.multiply(matA, matB);
        endTime = System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Cuda mult: " + d + " ms");
*/

        startTime = System.nanoTime();
        KMatrix matD = manualMultpily(matA, matB);
        endTime = System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        //System.out.println("Traditional mult: " + d + " ms");

        double eps = 1e-7;

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < p; j++) {
                Assert.assertTrue(Math.abs(matNetlib.get(i, j) - matD.get(i, j)) < eps);
                Assert.assertTrue(Math.abs(matF2J.get(i, j) - matD.get(i, j)) < eps);
                // Assert.assertTrue(Math.abs(matCuda.get(i, j) - matD.get(i, j)) < eps);
            }
        }
    }
}

