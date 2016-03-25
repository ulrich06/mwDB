package math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.blas.KBlas;
import org.mwdb.math.matrix.blas.NetlibBlas;
import org.mwdb.math.matrix.solver.PInvSVD;
import org.mwdb.math.matrix.solver.SVD;

/**
 * Created by assaad on 25/03/16.
 */
public class GaussianTest {
    @Test
    public void gaussian(){

       int dim=3;
        KBlas blas = new NetlibBlas();

      /*  double[] mt={4,7,3,2};
        KMatrix mtt= new Matrix(mt,2,2);
        PInvSVD pt= new PInvSVD(2,2,new SVD(2,2,blas));
        pt.factor(mtt,false);
        KMatrix mit=pt.getPInv();
        double d0= pt.getDeterminant();*/

    /*    double[] matrix ={504,360,180,360,360,0,180,0,720};


        KMatrix matA= new Matrix(matrix,dim,dim);
        PInvSVD pinv = new PInvSVD(dim,dim,new SVD(dim,dim,blas));
        pinv.factor(matA,false);
        KMatrix matInv= pinv.getPInv();
        double d1= pinv.getDeterminant();



        double[][]covariance = new double[dim][dim];
        for(int i=0; i<dim;i++){
            for (int j=0;j<dim;j++){
                covariance[i][j]=matA.get(i,j);
            }
        }

        Array2DRowRealMatrix covarianceMatrix = new Array2DRowRealMatrix(covariance);

        // Covariance matrix eigen decomposition.
        final EigenDecomposition covMatDec = new EigenDecomposition(covarianceMatrix);

        // Compute and store the inverse.
        RealMatrix covarianceMatrixInverse = covMatDec.getSolver().getInverse();
        // Compute and store the determinant.
        double covarianceMatrixDeterminant = covMatDec.getDeterminant();

*/


        double[] matrix2 ={504,360,180,0,360,360,0,0,180,0,720,0,0,0,0,0};


        KMatrix matA2= new Matrix(matrix2,dim+1,dim+1);
        PInvSVD pinv2 = new PInvSVD(dim+1,dim+1,new SVD(dim+1,dim+1,blas));
        pinv2.factor(matA2,false);
        KMatrix matInv2= pinv2.getPInv();
        double d12= pinv2.getDeterminant();


        double[][]covariance2 = new double[dim+1][dim+1];
        for(int i=0; i<dim+1;i++){
            for (int j=0;j<dim+1;j++){
                covariance2[i][j]=matA2.get(i,j);
            }
        }

        Array2DRowRealMatrix covarianceMatrix2 = new Array2DRowRealMatrix(covariance2);

        // Covariance matrix eigen decomposition.
        final EigenDecomposition covMatDec2 = new EigenDecomposition(covarianceMatrix2);

        // Compute and store the inverse.
        RealMatrix covarianceMatrixInverse2 = covMatDec2.getSolver().getInverse();
        // Compute and store the determinant.
        double covarianceMatrixDeterminant2 = covMatDec2.getDeterminant();


        int x=0;


    }
}
