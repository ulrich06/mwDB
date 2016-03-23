package math;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.KMatrixType;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.task.NoopScheduler;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 * Created by assaad on 23/03/16.
 */
public class MatrixTest {

    public KMatrix manualMultpily(KMatrix matA, KMatrix matB){
        KMatrix matC=new Matrix(null,matA.rows(),matB.columns(),matA.matrixType());

        for(int i=0;i<matA.rows();i++){
            for(int j=0;j<matB.columns();j++){
                for(int k=0;k<matA.columns();k++){
                    matC.add(i,j,matA.get(i,k)*matB.get(k,j));
                }
            }
        }

        return matC;

    }

    @Test
    public void MatrixType() {
        //test row major, column major
        Matrix m = new Matrix(null,3,5, KMatrixType.ROW_BASED);
        for(int i=0;i<15;i++){
            m.setAtIndex(i,i);
        }

        int k=0;
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                Assert.assertTrue(m.get(i,j)==k);
                k++;
            }
        }
        m = new Matrix(null,3,5, KMatrixType.COLUMN_BASED);
        for(int i=0;i<15;i++){
            m.setAtIndex(i,i);
        }
        k=0;
        for(int j=0;j<5;j++){
            for(int i=0;i<3;i++){
                Assert.assertTrue(m.get(i,j)==k);
                k++;
            }
        }
    }

    @Test
    public void MatrixMult(){
        //Test matrix mult
        int r=500;
        int o=300;
        int p= 700;
        KMatrix matA= Matrix.random(r,o, KMatrixType.COLUMN_BASED,0,100);
        KMatrix matB= Matrix.random(o,p, KMatrixType.COLUMN_BASED,0,100);
        long startTime, endTime;
        double d;

        startTime=System.nanoTime();
        KMatrix matC= Matrix.multiply(matA,matB);
        endTime = System.nanoTime();
        d=(endTime-startTime);
        d=d/1000000;
        System.out.println("Netlib mult: "+d+" ms");

        startTime=System.nanoTime();
        KMatrix matD= manualMultpily(matA,matB);
        endTime = System.nanoTime();
        d=(endTime-startTime);
        d=d/1000000;
        System.out.println("Traditional mult: "+d+" ms");

        double eps=1e-7;

        for(int i=0;i<r;i++){
            for(int j=0;j<p;j++){
                Assert.assertTrue(Math.abs(matC.get(i,j)-matD.get(i,j))<eps);
            }
        }
    }

    @Test
    public void MatrixInvert(){
        int r = 1000;
        int times = 1;
        int[] dimA = {r, r};
        double eps = 1e-7;

        KMatrix matA = Matrix.random(r,r,KMatrixType.COLUMN_BASED,0,100);

        KMatrix res=null;

        long timestart, timeend;
        // SimpleMatrix resEjml =new SimpleMatrix(1,1);

        timestart = System.currentTimeMillis();
        for (int k = 0; k < times; k++) {
            res = Matrix.invert(matA,false);
        }
        timeend = System.currentTimeMillis();
        System.out.println("Netlib blas invert " + ((double) (timeend - timestart)) / (1000 * times) + " s");

        KMatrix id= Matrix.multiply(matA,res);

        for (int i=0;i<r;i++){
            for(int j=0;j<r;j++){
                double x;
                if(i==j){
                    x=1;
                }
                else {
                    x=0;
                }
                Assert.assertTrue(Math.abs(id.get(i,j)-x)<eps);
            }
        }


    }



}
