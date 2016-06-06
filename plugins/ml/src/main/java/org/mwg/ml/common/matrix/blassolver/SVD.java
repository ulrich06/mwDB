package org.mwg.ml.common.matrix.blassolver;

import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.SVDDecompose;
import org.mwg.ml.common.matrix.blassolver.blas.Blas;

/**
 * Computes singular value decompositions
 */
public class SVD implements SVDDecompose {

    /**
     * Work array
     */
    private final double[] work;

    /**
     * Work array
     */
    private final int[] iwork;

    /**
     * Matrix dimension
     */
    private final int m, n;

    /**
     * Compute the singular vectors fully?
     */
    private final boolean vectors;

    /**
     * Job to do
     */
    private final JobSVD job;

    /**
     * The singular values
     */
    private final double[] S;

    /**
     * Singular vectors
     */
    private final Matrix U, Vt;

    private Blas _blas;


    /**
     * Creates an empty SVD
     *
     * @param m Number of rows
     * @param n Number of columns
     */
    public SVD(int m, int n, Blas blas) {
        this.m = m;
        this.n = n;
        this._blas = blas;
        this.vectors = true;

        // Allocate space for the decomposition
        S = new double[Math.min(m, n)];
        if (vectors) {
            U = new Matrix(null, m, m);
            Vt = new Matrix(null, n, n);
        } else
            U = Vt = null;

        job = vectors ? JobSVD.All : JobSVD.None;

        // Find workspace requirements
        iwork = new int[8 * Math.min(m, n)];

        // CoreQuery optimal workspace
        double[] worksize = new double[1];
        int[] info = new int[1];
        _blas.dgesdd(job.netlib(), m, n, new double[0],
                Math.max(1, m), new double[0], new double[0], Math.max(1, m),
                new double[0], Math.max(1, n), worksize, -1, iwork, info);

        // Allocate workspace
        int lwork = -1;
        if (info[0] != 0) {
            if (vectors)
                lwork = 3
                        * Math.min(m, n)
                        * Math.min(m, n)
                        + Math.max(
                        Math.max(m, n),
                        4 * Math.min(m, n) * Math.min(m, n) + 4
                                * Math.min(m, n));
            else
                lwork = 3
                        * Math.min(m, n)
                        * Math.min(m, n)
                        + Math.max(
                        Math.max(m, n),
                        5 * Math.min(m, n) * Math.min(m, n) + 4
                                * Math.min(m, n));
        } else
            lwork = (int) worksize[0];

        lwork = Math.max(lwork, 1);
        work = new double[lwork];
    }

    /**
     * Computes an SVD
     *
     * @param A Matrix to decompose. Size must conform, and it will be
     *          overwritten on return. Pass a copy to avoid this
     * @return The current decomposition
     */
    @Override
    public SVD factor(Matrix A, boolean workInPlace) {
        if (A.rows() != m)
            throw new IllegalArgumentException("A.numRows() != m");
        else if (A.columns() != n)
            throw new IllegalArgumentException("A.numColumns() != n");

        int[] info = new int[1];
        info[0] = 0;
        if(workInPlace) {
            _blas.dgesdd(job.netlib(), m, n, A.data(),
                    Math.max(1, m), S, vectors ? U.data() : new double[0],
                    Math.max(1, m), vectors ? Vt.data() : new double[0],
                    Math.max(1, n), work, work.length, iwork, info);
        }
        else {
            double[] Adata = A.data();
            double[] cloned = new double[Adata.length];
            System.arraycopy(Adata,0,cloned,0,Adata.length);
            _blas.dgesdd(job.netlib(), m, n, cloned,
                    Math.max(1, m), S, vectors ? U.data() : new double[0],
                    Math.max(1, m), vectors ? Vt.data() : new double[0],
                    Math.max(1, n), work, work.length, iwork, info);
        }

        if (info[0] > 0)
            throw new RuntimeException("NotConvergedException.Reason.Iterations");
        else if (info[0] < 0)
            throw new IllegalArgumentException();

        return this;
    }

    /**
     * True if singular vectors are stored
     */
    public boolean hasSingularVectors() {
        return U != null;
    }

    /**
     * Returns the left singular vectors, column-wise. Not available for partial
     * decompositions
     *
     * @return Matrix of size m*m
     */
    @Override
    public Matrix getU() {
        return U;
    }

    /**
     * Returns the right singular vectors, row-wise. Not available for partial
     * decompositions
     *
     * @return Matrix of size n*n
     */
    @Override
    public Matrix getVt() {
        return Vt;
    }

    /**
     * Returns the singular values (stored in descending order)
     *
     * @return Array of size getMin(m,n)
     */
    @Override
    public double[] getS() {
        return S;
    }

    @Override
    public Matrix getSMatrix() {
        Matrix matS = new Matrix(null, m, n);
        for (int i = 0; i < S.length; i++) {
            matS.set(i, i, S[i]);
        }
        return matS;
    }
}
