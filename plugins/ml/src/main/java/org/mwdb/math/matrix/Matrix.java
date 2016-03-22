package org.mwdb.math.matrix;

public class Matrix implements KMatrix {

    private final byte _matrixType;

    private final double[] _back;

    private final int _nbRows;
    private final int _nbColumns;

    public Matrix(double[] backend, int p_nbRows, int p_nbColumns, byte p_matrixType) {
        this._matrixType = p_matrixType;
        this._back = backend;
        this._nbRows = p_nbRows;
        this._nbColumns = p_nbColumns;
        if (this._back == null) {
            //TODO init
        }
    }


    @Override
    public double[] data() {
        return _back;
    }

    @Override
    public byte matrixType() {
        return _matrixType;
    }

    @Override
    public double get(int p_rowIndex, int p_columnIndex) {
        //TODO add a if according to  the type
        return this._segment.getDoubleArrayElem(this._segmentIndex, getIndex(p_rowIndex, p_columnIndex), this._metaClass);
    }

    @Override
    public double set(int p_rowIndex, int p_columnIndex, double value) {
        //TODO add a if according to  the type
        this._segment.setDoubleArrayElem(this._segmentIndex, getIndex(p_rowIndex, p_columnIndex), value, this._metaClass);
        return value;
    }

    /**
     * Matrix engine
     */
    /**
     * @native ts
     */
    private static KMatrixEngine _defaultEngine = new BlasMatrixEngine();

    public static KMatrixEngine defaultEngine() {
        return _defaultEngine;
    }

}
