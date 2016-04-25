package math;

import org.apache.commons.math3.linear.*;
import org.junit.Assert;
import org.junit.Test;
import org.mwg.math.matrix.KMatrix;
import org.mwg.math.matrix.blassolver.LU;
import org.mwg.math.matrix.blassolver.blas.KBlas;
import org.mwg.math.matrix.blassolver.blas.NetlibBlas;
import org.mwg.math.matrix.jamasolver.QR;
import org.mwg.math.matrix.jamasolver.SVD;

/**
 * Created by assaad on 06/04/16.
 */
public class JamaPerf {
    @Test
    public void testPerf(){
        int m=500;
        int n=300;

        double eps=1e-8;

        KBlas blas = new NetlibBlas();

        KMatrix matA = KMatrix.random(m, n, 0, 100);
        KMatrix matASq = KMatrix.random(m, m, 0, 100);

        KMatrix matAcopy = matA.clone();
        KMatrix matAsqCopy= matASq.clone();

        Array2DRowRealMatrix matAapache = new Array2DRowRealMatrix(m,n);
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                matAapache.setEntry(i,j,matA.get(i,j));
            }
        }

        Array2DRowRealMatrix matAsqapache = new Array2DRowRealMatrix(m,m);
        for(int i=0;i<m;i++){
            for(int j=0;j<m;j++){
                matAsqapache.setEntry(i,j,matASq.get(i,j));
            }
        }

        double err;

        err=KMatrix.compareMatrix(matA,matAcopy);
        Assert.assertTrue(err<eps);
        err=KMatrix.compareMatrix(matASq,matAsqCopy);
        Assert.assertTrue(err<eps);

        long startTime, endTime;
        double d;

        startTime =System.nanoTime();
        LU lublas = new LU(m,m,blas);
        lublas.factor(matASq,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Blas LU: " + d + " ms");

        startTime =System.nanoTime();
        org.mwg.math.matrix.jamasolver.LU lujama= new org.mwg.math.matrix.jamasolver.LU(matASq);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Jama LU: " + d + " ms");


        startTime =System.nanoTime();
        LUDecomposition luapache= new LUDecomposition(matAsqapache);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Apache LU: " + d + " ms");
        System.out.println();

        startTime =System.nanoTime();
        org.mwg.math.matrix.blassolver.QR qrblas= new org.mwg.math.matrix.blassolver.QR(m,n,blas);
        qrblas.factor(matA,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Blas QR: " + d + " ms");


        startTime =System.nanoTime();
        QR qrjama = new QR(matA);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Jama QR: " + d + " ms");

        startTime =System.nanoTime();
        QRDecomposition qrApache= new QRDecomposition(matAapache);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Apache QR: " + d + " ms");
        System.out.println();

        startTime =System.nanoTime();
        org.mwg.math.matrix.blassolver.SVD svdblas= new org.mwg.math.matrix.blassolver.SVD(m,n,blas);
        svdblas.factor(matA,false);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Blas SVD: " + d + " ms");

        startTime =System.nanoTime();
        SVD svdjama= new SVD(matA);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Jama SVD: " + d + " ms");



        startTime =System.nanoTime();
        SingularValueDecomposition svdapache = new SingularValueDecomposition(matAapache);
        endTime=System.nanoTime();
        d = (endTime - startTime);
        d = d / 1000000;
        System.out.println("Apache SVD: " + d + " ms");
        System.out.println();





        //Validate integrity of matrices that they didn't change
        err=KMatrix.compareMatrix(matA,matAcopy);
        Assert.assertTrue(err<eps);
        err=KMatrix.compareMatrix(matASq,matAsqCopy);
        Assert.assertTrue(err<eps);

        //Validate LU
        KMatrix Lblas= lublas.getL();
        KMatrix Ljama= lujama.getL();
        err=KMatrix.compareMatrix(Lblas,Ljama);
        System.out.println("Error in matrix L: "+err);
        Assert.assertTrue(err<eps);

        KMatrix Ublas= lublas.getU();
        KMatrix Ujama= lujama.getU();
        err=KMatrix.compareMatrix(Ublas,Ujama);
        System.out.println("Error in matrix U: "+err);
        Assert.assertTrue(err<eps);

        //Validate QR
        KMatrix Qblas= qrblas.getQ();
        KMatrix Qjama= qrjama.getQ();
        err=KMatrix.compareMatrix(Qblas,Qjama);
        System.out.println("Error in matrix Q: "+err);
        Assert.assertTrue(err<eps);

        KMatrix Rblas= qrblas.getR();
        KMatrix Rjama= qrjama.getR();
        err=KMatrix.compareMatrix(Rblas,Rjama);
        System.out.println("Error in matrix R: "+err);
        Assert.assertTrue(err<eps);


        //Validate SVD
        KMatrix Sblas= svdblas.getSMatrix();
        RealMatrix SApache= svdapache.getS();
        KMatrix Sjama= svdjama.getSMatrix();


        KMatrix Vblas= svdblas.getVt();
        RealMatrix VApache= svdapache.getVT();
        KMatrix Vjama= svdjama.getVt();


        KMatrix Dblas= svdblas.getU();
        RealMatrix DApache= svdapache.getU();
        KMatrix Djama= svdjama.getU();



        double[] errS=calcerr(Sblas,SApache,Sjama);
        double[] errV=calcerr(Vblas,VApache,Vjama);
        double[] errD=calcerr(Dblas,DApache,Djama);
        System.out.println("Error in matrix S: "+errS[0]+" , "+ errS[1]);
        System.out.println("Error in matrix V: "+errV[0]+" , "+ errV[1]);
        System.out.println("Error in matrix D: "+errD[0]+" , "+ errD[1]);

        Assert.assertTrue(errS[0]<eps);
        Assert.assertTrue(errS[1]<eps);
        Assert.assertTrue(errV[0]<eps);
        Assert.assertTrue(errV[1]<eps);
        Assert.assertTrue(errD[0]<eps);
        Assert.assertTrue(errD[1]<eps);






        //Validate SVD
     /*   KMatrix Sblas= svdblas.getSMatrix();
        KMatrix Sjama= svdjama.getSMatrix();
        err=KMatrix.compareMatrix(Sblas,Sjama);
        System.out.println("Error in matrix S: "+err);
        Assert.assertTrue(err<eps);


        KMatrix Dblas= svdblas.getU();
        KMatrix Djama= svdjama.getU();
        err=KMatrix.compareMatrix(Dblas,Djama);
        System.out.println("Error in matrix D: "+err);
        Assert.assertTrue(err<eps);

        KMatrix Vblas= svdblas.getVt();
        KMatrix Vjama= svdjama.getVt();
        err=KMatrix.compareMatrix(Vblas,Vjama);
        System.out.println("Error in matrix V: "+err);
        Assert.assertTrue(err<eps);*/
    }


    public double[] calcerr(KMatrix blas, RealMatrix apache, KMatrix jama){
        double[] err=new double[2];
        int m1=Math.min(blas.rows(),apache.getRowDimension());
        int n1=Math.min(blas.columns(),apache.getColumnDimension());
        m1=Math.min(m1,jama.rows());
        n1=Math.min(n1,jama.columns());

        double err1,err2;
        for(int i=0;i<m1;i++){
            for(int j=0;j<n1;j++){
                err1=Math.abs(blas.get(i,j))-Math.abs(apache.getEntry(i,j));
                err2=Math.abs(apache.getEntry(i,j))-Math.abs(jama.get(i,j));

                err[0]=Math.max(err[0],err1);
                err[1]=Math.max(err[1],err2);
            }
        }

        return err;

    }


}
