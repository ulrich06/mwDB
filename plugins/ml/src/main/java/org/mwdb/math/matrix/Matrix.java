package org.mwdb.math.matrix;


import org.mwdb.math.matrix.blas.KBlasTransposeType;

import java.util.BitSet;
import java.util.Random;

//Most of the time we will be using column based matrix due to blas.
public class Matrix implements KMatrix {
    private final byte _matrixType;

    private double[] _data;

    private final int _nbRows;
    private final int _nbColumns;

    public Matrix(double[] backend, int p_nbRows, int p_nbColumns, byte p_matrixType) {
        this._matrixType = p_matrixType;
        this._nbRows = p_nbRows;
        this._nbColumns = p_nbColumns;
        if (backend != null) {
            this._data = backend;
        } else {
            this._data = new double[_nbRows * _nbColumns];
        }
    }


    @Override
    public double[] data() {
        return _data;
    }

    @Override
    public void setData(double[] data) {
        System.arraycopy(data, 0, this._data, 0, data.length);
    }

    @Override
    public byte matrixType() {
        return _matrixType;
    }

    @Override
    public int rows() {
        return _nbRows;
    }

    @Override
    public int columns() {
        return _nbColumns;
    }

    @Override
    public double get(int rowIndex, int columnIndex) {
        if (_matrixType == KMatrixType.COLUMN_BASED) {
            return _data[rowIndex + columnIndex * _nbRows];
        } else {
            return _data[columnIndex + rowIndex * _nbColumns];
        }
    }

    @Override
    public double set(int rowIndex, int columnIndex, double value) {
        if (_matrixType == KMatrixType.COLUMN_BASED) {
            _data[rowIndex + columnIndex * _nbRows] = value;
        } else {
            _data[columnIndex + rowIndex * _nbColumns] = value;
        }
        return value;
    }

    @Override
    public double add(int rowIndex, int columnIndex, double value) {
        return set(rowIndex, columnIndex, get(rowIndex, columnIndex) + value);
    }

    @Override
    public void setAll(double value) {
        for (int i = 0; i < _nbColumns * _nbRows; i++) {
            this._data[i] = value;
        }
    }

    @Override
    public double getAtIndex(int index) {
        return this._data[index];
    }

    @Override
    public double setAtIndex(int index, double value) {
        this._data[index] = value;
        return value;
    }

    @Override
    public double addAtIndex(int index, double value) {
        this._data[index] += value;
        return this._data[index];
    }

    @Override
    public KMatrix clone() {
        double[] newback = new double[_data.length];
        System.arraycopy(_data, 0, newback, 0, _data.length);

        Matrix res = new Matrix(newback, this._nbRows, this._nbColumns, this._matrixType);
        return res;

    }

    /**
     * Matrix engine
     */
    /**
     * @native ts
     */
    private static KMatrixEngine _defaultEngine = new org.mwdb.math.matrix.blas.BlasMatrixEngine();

    public static KMatrixEngine defaultEngine() {
        return _defaultEngine;
    }


    public static KMatrix multiply(KMatrix matA, KMatrix matB) {
        return _defaultEngine.multiplyTransposeAlphaBeta(KBlasTransposeType.NOTRANSPOSE, 1, matA, KBlasTransposeType.NOTRANSPOSE, 1, matB);
    }

    public static KMatrix multiplyTransposeAlphaBeta(KBlasTransposeType transA, double alpha, KMatrix matA, KBlasTransposeType transB, double beta, KMatrix matB) {
        return _defaultEngine.multiplyTransposeAlphaBeta(transA, alpha, matA, transB, beta, matB);
    }

    public static KMatrix invert(KMatrix mat, boolean invertInPlace) {
        return _defaultEngine.invert(mat, invertInPlace);
    }

    public static KMatrix solve(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB) {
        return _defaultEngine.solve(matA, matB, workInPlace, transB);
    }

    public static int leadingDimension(KMatrix matA) {
        return Math.max(matA.columns(), matA.rows());
    }

    public static KMatrix random(int rows, int columns, byte matrixType, double min, double max) {
        KMatrix res = new Matrix(null, rows, columns, matrixType);
        Random rand = new Random();
        for (int i = 0; i < rows * columns; i++) {
            res.setAtIndex(i, rand.nextDouble() * (max - min) + min);
        }
        return res;
    }

    public static void scale(double alpha, KMatrix matA) {
        if (alpha == 0) {
            matA.setAll(0);
            return;
        }
        for (int i = 0; i < matA.rows() * matA.columns(); i++) {
            matA.setAtIndex(i, alpha * matA.getAtIndex(i));
        }
    }

    public static KMatrix transpose(KMatrix matA) {
        KMatrix result = new Matrix(null, matA.columns(), matA.rows(), matA.matrixType());
        int TRANSPOSE_SWITCH = 375;
        if (matA.columns() == matA.rows()) {
            transposeSquare(matA, result);
        } else if (matA.columns() > TRANSPOSE_SWITCH && matA.rows() > TRANSPOSE_SWITCH) {
            transposeBlock(matA, result);
        } else {
            transposeStandard(matA, result);
        }
        return result;
    }

    private static void transposeSquare(KMatrix matA, KMatrix result) {
        int index = 1;
        int indexEnd = matA.columns();
        for (int i = 0; i < matA.rows(); i++) {
            int indexOther = (i + 1) * matA.columns() + i;
            int n = i * (matA.columns() + 1);
            result.setAtIndex(n, matA.getAtIndex(n));
            for (; index < indexEnd; index++) {
                result.setAtIndex(index, matA.getAtIndex(indexOther));
                result.setAtIndex(indexOther, matA.getAtIndex(index));
                indexOther += matA.columns();
            }
            index += i + 2;
            indexEnd += matA.columns();
        }
    }

    private static void transposeStandard(KMatrix matA, KMatrix result) {
        int index = 0;
        for (int i = 0; i < result.columns(); i++) {
            int index2 = i;
            int end = index + result.rows();
            while (index < end) {
                result.setAtIndex(index++, matA.getAtIndex(index2));
                index2 += matA.rows();
            }
        }
    }

    private static void transposeBlock(KMatrix matA, KMatrix result) {
        int BLOCK_WIDTH = 60;
        for (int j = 0; j < matA.columns(); j += BLOCK_WIDTH) {
            int blockWidth = Math.min(BLOCK_WIDTH, matA.columns() - j);
            int indexSrc = j * matA.rows();
            int indexDst = j;

            for (int i = 0; i < matA.rows(); i += BLOCK_WIDTH) {
                int blockHeight = Math.min(BLOCK_WIDTH, matA.rows() - i);
                int indexSrcEnd = indexSrc + blockHeight;

                for (; indexSrc < indexSrcEnd; indexSrc++) {
                    int colSrc = indexSrc;
                    int colDst = indexDst;
                    int end = colDst + blockWidth;
                    for (; colDst < end; colDst++) {
                        result.setAtIndex(colDst, matA.getAtIndex(colSrc));
                        colSrc += matA.rows();
                    }
                    indexDst += result.rows();
                }
            }
        }
    }

    public static KMatrix createIdentity(int width, byte matrixType) {
        KMatrix ret = new Matrix(null, width, width, matrixType);
        for (int i = 0; i < width; i++) {
            ret.set(i, i, 1);
        }
        return ret;
    }

    public static double compareMatrix(KMatrix matA, KMatrix matB) {
        double err = 0;

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                if (err < Math.abs(matA.get(i, j) - matB.get(i, j))) {
                    err = Math.abs(matA.get(i, j) - matB.get(i, j));
                    // System.out.println(i+" , "+ j+" , "+ err);
                }

            }
        }
        return err;
    }

    public static KMatrix fromPartialPivots(int[] pivots, byte type, boolean transposed) {
        int[] permutations = new int[pivots.length];
        for (int i = 0; i < pivots.length; i++) {
            permutations[i] = i;
        }

        for (int i = 0; i < pivots.length; i++) {
            int j = pivots[i] - 1;
            if (j == i)
                continue;
            int tmp = permutations[i];
            permutations[i] = permutations[j];
            permutations[j] = tmp;
        }

        BitSet bitset = new BitSet();
        for (int i : permutations) {
            if (bitset.get(i))
                throw new IllegalArgumentException("non-unique permutations: " + i);
            bitset.set(i);
        }

        Matrix m = new Matrix(null, permutations.length, permutations.length, type);

        double x;
        for (int i = 0; i < permutations.length; i++) {
            for (int j = 0; j < permutations.length; j++) {
                if ((!transposed && permutations[i] == j) || (transposed && permutations[j] == i)) {
                    x = 1;
                } else {
                    x = 0;
                }
                m.set(i, j, x);
            }
        }
        return m;
    }
}
