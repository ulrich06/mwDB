package org.mwdb.math.matrix.blassolver.blas;

import org.mwdb.math.matrix.KTransposeType;
import static jcuda.jcublas.JCublas2.*;
import static jcuda.runtime.JCuda.*;

import jcuda.Sizeof;
import jcuda.jcublas.*;
import jcuda.Pointer;


/**
 * Created by assaad on 18/04/16.
 */
public class CudaBlas implements KBlas {
    private cublasHandle handle;

    public CudaBlas(){
        connect();
    }

    private static int transTypeToInt(KTransposeType type) {
        if (type.equals(KTransposeType.NOTRANSPOSE)) {
            return cublasOperation.CUBLAS_OP_N;
        } else if (type.equals(KTransposeType.TRANSPOSE)) {
            return cublasOperation.CUBLAS_OP_T;
        }
        return '0';
    }

    @Override
    public void dgemm(KTransposeType transA, KTransposeType transB, int m, int n, int k, double alpha, double[] matA, int offsetA, int ldA, double[] matB, int offsetB, int ldB, double beta, double[] matC, int offsetC, int ldC) {
        // Allocate memory on the device
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        cudaMalloc(d_A, matA.length * Sizeof.DOUBLE);
        cudaMalloc(d_B, matB.length * Sizeof.DOUBLE);
        cudaMalloc(d_C, matC.length * Sizeof.DOUBLE);

        // Copy the memory from the host to the device
        JCublas2.cublasSetVector(matA.length, Sizeof.DOUBLE, Pointer.to(matA), 1, d_A, 1);
        JCublas2.cublasSetVector(matB.length, Sizeof.DOUBLE, Pointer.to(matB), 1, d_B, 1);
        JCublas2.cublasSetVector(matC.length, Sizeof.DOUBLE, Pointer.to(matC), 1, d_C, 1);

        // Execute sgemm
        Pointer pAlpha = Pointer.to(new double[]{alpha});
        Pointer pBeta = Pointer.to(new double[]{beta});
        cublasDgemm(handle, transTypeToInt(transA), transTypeToInt(transB), m, n, k,
                pAlpha, d_A, ldA, d_B, ldB, pBeta, d_C, ldC);

        // Copy the result from the device to the host
        JCublas2.cublasGetVector(matC.length, Sizeof.DOUBLE, d_C, 1, Pointer.to(matC), 1);

        // Clean up
        cudaFree(d_A);
        cudaFree(d_B);
        cudaFree(d_C);
        cudaFree(pAlpha);
        cudaFree(pBeta);
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
        JCublas.initialize();
        JCublas2.initialize();
        JCublas.setExceptionsEnabled(true);
        JCublas2.setExceptionsEnabled(true);
        handle = new cublasHandle();
        cublasCreate(handle);
        jcuda.jcusolver.JCusolver.initialize();


    }

    @Override
    public void disconnect() {

    }
}
