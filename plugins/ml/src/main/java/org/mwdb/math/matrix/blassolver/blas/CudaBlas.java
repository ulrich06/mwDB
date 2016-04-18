package org.mwdb.math.matrix.blassolver.blas;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;
import org.mwdb.math.matrix.KTransposeType;

public class CudaBlas implements KBlas {

    public CudaBlas() {
        //connect();
    }

    private static final char TRANSPOSE_TYPE_NOTRANSPOSE = 'n';
    private static final char TRANSPOSE_TYPE_TRANSPOSE = 't';

    private static char transTypeToChar(KTransposeType type) {
        if (type.equals(KTransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(KTransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return '0';
    }

    @Override
    public void dgemm(KTransposeType transA, KTransposeType transB, int m, int n, int k, double alpha, double[] matA, int offsetA, int ldA, double[] matB, int offsetB, int ldB, double beta, double[] matC, int offsetC, int ldC) {
        // Allocate memory on the device
        JCublas.cublasInit();


        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        JCublas.cublasAlloc(matA.length, Sizeof.DOUBLE, d_A);
        JCublas.cublasAlloc(matB.length, Sizeof.DOUBLE, d_B);
        JCublas.cublasAlloc(matC.length, Sizeof.DOUBLE, d_C);

        // Copy the memory from the host to the device
        JCublas.cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        JCublas.cublasSetVector(matB.length, Sizeof.DOUBLE, Pointer.to(matB), 1, d_B, 1);
        JCublas.cublasSetVector(matC.length, Sizeof.DOUBLE, Pointer.to(matC), 1, d_C, 1);

        // Execute sgemm
        JCublas.cublasDgemm(transTypeToChar(transA), transTypeToChar(transB), m, n, k, alpha, d_A, ldA, d_B, ldB, beta, d_C, ldC);


        // Copy the result from the device to the host
        JCublas.cublasGetVector(matC.length, Sizeof.DOUBLE, d_C, 1, Pointer.to(matC), 1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clean up
        JCublas.cublasFree(d_A);
        JCublas.cublasFree(d_B);
        JCublas.cublasFree(d_C);

        JCublas.cublasShutdown();

    }

    @Override
    public void dgetrs(KTransposeType transA, int dim, int nrhs, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] matB, int offsetB, int ldB, int[] info) {

    }

    @Override
    public void dgetri(int dim, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, double[] work, int offsetWork, int ldWork, int[] info) {

    }

    @Override
    public void dgetrf(int rows, int columns, double[] matA, int offsetA, int ldA, int[] ipiv, int offsetIpiv, int[] info) {

    }

    @Override
    public void dorgqr(int m, int n, int k, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetWork, int lWork, int[] info) {

    }

    @Override
    public void dgeqrf(int m, int n, double[] matA, int offsetA, int ldA, double[] taw, int offsetTaw, double[] work, int offsetwork, int lWork, int[] info) {

    }

    @Override
    public void dgesdd(String jobz, int m, int n, double[] data, int lda, double[] s, double[] u, int ldu, double[] vt, int ldvt, double[] work, int length, int[] iwork, int[] info) {

    }


    @Override
    public void connect() {
       // JCublas.cublasInit();
    }

    @Override
    public void disconnect() {
      //  JCublas.cublasShutdown();
    }
}
