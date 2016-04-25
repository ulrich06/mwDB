package org.mwg.util.matrix.blassolver;


import org.mwg.util.matrix.KMatrix;
import org.mwg.util.matrix.blassolver.blas.KBlas;
import org.mwg.util.matrix.KTransposeType;

public class QR {

    /**
     * The orthogonal matrix
     */
    private KMatrix Q;
    private KMatrix R;
    private KBlas _blas;

    /**
     * Factorisation sizes
     */
    int m, n, k;

    /**
     * Work arrays
     */
    double[] work, workGen;

    /**
     * Scales for the reflectors
     */
    double[] tau;

    /**
     * Constructs an empty QR decomposition
     *
     * @param rows    Number of rows. Must be larger than or equal the number of
     *                columns
     * @param columns Number of columns
     */
    public QR(int rows, int columns, KBlas blas) {
        this._blas = blas;
        if (columns > rows)
            throw new RuntimeException("n > m");

        this.m = rows;
        this.n = columns;
        this.k = Math.min(m, n);
        tau = new double[k];
        R = new KMatrix(null, n, n);

    }

    /**
     * Convenience method to compute a QR decomposition
     *
     * @param A Matrix to decompose. Not modified
     * @return Newly allocated decomposition
     */
    public static QR factorize(KMatrix A, boolean workInPlace, KBlas blas) {
        return new QR(A.rows(), A.columns(), blas).factor(A, workInPlace);
    }

    public QR factor(KMatrix matA, boolean workInPlace) {
        KMatrix A;
        if (!workInPlace) {
            A = matA.clone();
        } else {
            A = matA;
        }

        int lwork;

        // Query optimal workspace. First for computing the factorization
        {
            work = new double[1];
            int[] info = new int[1];
            info[0] = 0;
            _blas.dgeqrf(m, n, new double[0], 0, m,
                    new double[0], 0, work, 0, -1, info);

            if (info[0] != 0)
                lwork = n;
            else
                lwork = (int) work[0];
            lwork = Math.max(1, lwork);
            work = new double[lwork];
        }

        // Workspace needed for generating an explicit orthogonal matrix
        {
            workGen = new double[1];
            int[] info = new int[1];
            info[0] = 0;
            _blas.dorgqr(m, n, k, new double[0], 0, m, new double[0], 0, workGen, 0, -1, info);

            if (info[0] != 0)
                lwork = n;
            else
                lwork = (int) workGen[0];
            lwork = Math.max(1, lwork);
            workGen = new double[lwork];
        }

        /*
         * Calculate factorisation, and extract the triangular factor
         */
        int[] info = new int[1];
        info[0] = 0;
        _blas.dgeqrf(m, n, A.data(), 0, m, tau, 0, work, 0, work.length, info);

        if (info[0] < 0)
            throw new RuntimeException("" + info[0]);

        for (int col = 0; col < A.columns(); col++) {
            for (int row = 0; row <= col; row++) {
                R.set(row, col, A.get(row, col));
            }
        }

        /*
         * Generate the orthogonal matrix
         */
        info[0] = 0;
        _blas.dorgqr(m, n, k, A.data(), 0, m, tau, 0, workGen, 0, workGen.length, info);

        if (info[0] < 0)
            throw new RuntimeException();

        Q = A;

        return this;
    }


    public void solve(KMatrix B, KMatrix X) {
        int BnumCols = B.columns();
        KMatrix Y = new KMatrix(null, m, 1);
        KMatrix Z;

        // solve each column one by one
        for (int colB = 0; colB < BnumCols; colB++) {
            // make a copy of this column in the vector
            for (int i = 0; i < m; i++) {
                Y.setAtIndex(i, B.get(i, colB));
            }
            // Solve Qa=b
            // a = Q'b
            Z = KMatrix.multiplyTransposeAlphaBeta(KTransposeType.TRANSPOSE, 1.0, Q, KTransposeType.NOTRANSPOSE, 1.0, Y);

            // solve for Rx = b using the standard upper triangular blassolver
            solveU(R, Z.data(), n, m);
            // save the results
            for (int i = 0; i < n; i++) {
                X.set(i, colB, Z.getAtIndex(i));
            }
        }
    }

    private void solveU(KMatrix U, double[] b, int n, int m) {
        for (int i = n - 1; i >= 0; i--) {
            double sum = b[i];
            for (int j = i + 1; j < n; j++) {
                sum -= U.get(i, j) * b[j];
            }
            b[i] = sum / U.get(i, i);
        }
    }


    /**
     * Returns the upper triangular factor
     */
    public KMatrix getR() {
        return R;
    }

    public KMatrix getQ() {
        return Q;
    }
}
