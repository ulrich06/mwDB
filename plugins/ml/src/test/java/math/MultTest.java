package math;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;

/**
 * Created by assaad on 25/03/16.
 */
public class MultTest {
    @Test
    public void mult(){
        double[] A= {1,2,3,4};
        double[] B={5,6,7,8};
        double[] C={23,34,31,46};

        KMatrix matA=new Matrix(A,2,2);
        KMatrix matB=new Matrix(B,2,2);

        double eps=1e-7;

        KMatrix matC=Matrix.multiply(matA,matB);

        Assert.assertTrue(Matrix.compare(matC.data(),C,eps));

        int x=0;
    }
}
