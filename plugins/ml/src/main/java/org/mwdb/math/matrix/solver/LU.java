package org.mwdb.math.matrix.solver;


import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.blas.KBlas;
import org.mwdb.math.matrix.blas.KBlasTransposeType;

public class LU {

    /**
     * Holds the LU factors
     */
    private KMatrix LU;
    private KBlas _blas;

    public KMatrix getLU() {
        return LU;
    }

    /**
     * Row pivotations
     */
    private int[] piv;

    /**
     * True if the matrix was singular
     */
    private boolean singular;

    /**
     * Constructor for DenseLU
     *
     * @param m Number of rows
     * @param n Number of columns
     */
    public LU(int m, int n, KBlas blas) {
        this._blas = blas;
        LU = new Matrix(null, m, n);
        piv = new int[Math.min(m, n)];
    }

    /**
     * Creates an LU decomposition of the given matrix
     *
     * @param A Matrix to decompose. Not modified
     * @return The current decomposition
     */
    public static LU factorize(KMatrix A, KBlas blas) {
        return new LU(A.rows(), A.columns(), blas).factor(A, false);
    }

    /**
     * Creates an LU decomposition of the given matrix
     *
     * @param A Matrix to decompose. Overwritten with the decomposition
     * @return The current decomposition
     */
    public LU factor(KMatrix A, boolean factorInPlace) {
        if (factorInPlace) {
            singular = false;

            int[] info = new int[1];
            info[0] = 0;
            _blas.dgetrf(A.rows(), A.columns(), A.data(), 0, A.rows(), piv, 0, info);

            if (info[0] > 0)
                singular = true;
            else if (info[0] < 0)
                throw new RuntimeException();

            LU.setData(A.data());
            return this;
        } else {
            singular = false;
            KMatrix B = A.clone();

            int[] info = new int[1];
            info[0] = 0;
            _blas.dgetrf(B.rows(), B.columns(), B.data(), 0, B.rows(), piv, 0, info);

            if (info[0] > 0)
                singular = true;
            else if (info[0] < 0)
                throw new RuntimeException();

            LU.setData(B.data());
            return this;
        }

    }


    public KMatrix getLower() {
        int numRows = LU.rows();
        int numCols = LU.rows() < LU.columns() ? LU.rows() : LU.columns();
        Matrix lower = new Matrix(null, numRows, numCols);


        for (int i = 0; i < numCols; i++) {
            lower.set(i, i, 1.0);

            for (int j = 0; j < i; j++) {
                lower.set(i, j, LU.get(i, j));
            }
        }

        if (numRows > numCols) {
            for (int i = numCols; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    lower.set(i, j, LU.get(i, j));
                }
            }
        }
        return lower;
    }

    public KMatrix getP() {
        return Matrix.fromPartialPivots(piv, true);
    }


    public KMatrix getUpper() {
        int numRows = LU.rows() < LU.columns() ? LU.rows() : LU.columns();
        int numCols = LU.columns();

        KMatrix upper = new Matrix(null, numRows, numCols);


        for (int i = 0; i < numRows; i++) {
            for (int j = i; j < numCols; j++) {
                upper.set(i, j, LU.get(i, j));
            }
        }

        return upper;
    }


    /**
     * Returns the row pivots
     */
    public int[] getPivots() {
        return piv;
    }

    /**
     * Checks for singularity
     */
    public boolean isSingular() {
        return singular;
    }

    /**
     * Computes <code>A\B</code>, overwriting <code>B</code>
     */
    public KMatrix solve(KMatrix B) {
        return transSolve(B, KBlasTransposeType.NOTRANSPOSE);
    }


    public KMatrix transSolve(KMatrix B, KBlasTransposeType trans) {
        /*
        if (singular) {
         //   throw new MatrixSingularException();
        }
        */
        if (B.rows() != LU.rows())
            throw new RuntimeException("B.numRows() != LU.numRows()");

        int[] info = new int[1];
        _blas.dgetrs(trans, LU.rows(),
                B.columns(), LU.data(), 0, LU.rows(), piv, 0,
                B.data(), 0, B.rows(), info);

        if (info[0] < 0)
            throw new RuntimeException();

        return B;
    }

    public boolean invert(KMatrix A) {
        int[] info = new int[1];
        info[0] = 0;
        _blas.dgetrf(A.rows(), A.columns(), A.data(), 0, A.rows(), piv, 0, info);

       /* System.out.println("After f");
        for(int i=0;i<A.rows()*A.columns(); i++){
            System.out.print(A.getAtIndex(i)+" ");
        }
        System.out.println();

        System.out.println("PIV");
        for(int i=0;i<piv.length; i++){
            System.out.print(piv[i]+" ");
        }
        System.out.println();*/

        if (info[0] > 0) {
            singular = true;
            return false;
        } else if (info[0] < 0)
            throw new RuntimeException();

        int lwork = A.rows() * A.rows();
        double[] work = new double[lwork];
        for (int i = 0; i < lwork; i++) {
            work[i] = 0;
        }

        _blas.dgetri(A.rows(), A.data(), 0, A.rows(), piv, 0, work, 0, lwork, info);

        if (info[0] != 0) {
            return false;
        } else {
            return true;
        }

    }
}