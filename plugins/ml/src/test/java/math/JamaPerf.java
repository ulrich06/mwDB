package math;

import org.junit.Test;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.blassolver.blas.KBlas;
import org.mwdb.math.matrix.blassolver.blas.NetlibBlas;
import org.mwdb.math.matrix.jamasolver.QR;
import org.mwdb.math.matrix.jamasolver.SVD;

/**
 * Created by assaad on 06/04/16.
 */
public class JamaPerf {
    @Test
    public void testPerf(){
        int m=1000;
        int n=1000;

        KBlas blas = new NetlibBlas();

        KMatrix matA = KMatrix.random(m, n, 0, 100);



        long startTime, endTime;
        double d;

        startTime =System.nanoTime();
        org.mwdb.math.matrix.blassolver.QR qrblas= new org.mwdb.math.matrix.blassolver.QR(m,n,blas);
        qrblas.factor(matA,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println(" Blas QR: " + d + " ms");


        startTime =System.nanoTime();
        org.mwdb.math.matrix.blassolver.SVD svd= new org.mwdb.math.matrix.blassolver.SVD(m,n,blas);
        svd.factor(matA,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println(" Blas SVD: " + d + " ms");



        startTime =System.nanoTime();
        QR dec = new QR(matA);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println(" Jama QR: " + d + " ms");

        startTime =System.nanoTime();
        SVD svdjama= new SVD(matA);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println(" Jama SVD: " + d + " ms");

    }

}
