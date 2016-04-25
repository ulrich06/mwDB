package math;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.math.matrix.KMatrix;
import org.mwg.math.matrix.blassolver.blas.F2JBlas;
import org.mwg.math.matrix.blassolver.blas.KBlas;
import org.mwg.math.matrix.blassolver.blas.NetlibBlas;

/**
 * Created by assaad on 13/04/16.
 */
public class TestF2J {
    @Test
    public void testF2J(){



        int m=500;
        int n=300;
        double eps=1e-8;

        KBlas blas = new NetlibBlas();
        KBlas blasF2J= new F2JBlas();

        KMatrix matA = KMatrix.random(m, n, 0, 100);
        KMatrix matASq = KMatrix.random(m, m, 0, 100);

        KMatrix matAcopy = matA.clone();
        KMatrix matAsqCopy= matASq.clone();



        double err;

        long startTime, endTime;
        double d;
        startTime =System.nanoTime();
        org.mwg.math.matrix.blassolver.QR qrblas= new org.mwg.math.matrix.blassolver.QR(m,n,blas);
        qrblas.factor(matA,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Blas QR: " + d + " ms");


        startTime =System.nanoTime();
        org.mwg.math.matrix.blassolver.QR qrblasF2j= new org.mwg.math.matrix.blassolver.QR(m,n,blasF2J);
        qrblasF2j.factor(matAcopy,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Blas F2J QR: " + d + " ms");


        //Validate QR
        KMatrix Qblas= qrblas.getQ();
        KMatrix Qjama= qrblasF2j.getQ();
        err=KMatrix.compareMatrix(Qblas,Qjama);
        System.out.println("Error in matrix Q: "+err);
        Assert.assertTrue(err<eps);

        KMatrix Rblas= qrblas.getR();
        KMatrix Rjama= qrblasF2j.getR();
        err=KMatrix.compareMatrix(Rblas,Rjama);
        System.out.println("Error in matrix R: "+err);
        Assert.assertTrue(err<eps);


    }

}
