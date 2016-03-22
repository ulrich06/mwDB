package org.mwdb.math.matrix;

public class Matrix implements KMatrix {

    private final byte _matrixType;

    private double[] _data;

    private final int _nbRows;
    private final int _nbColumns;

    public Matrix(double[] backend, int p_nbRows, int p_nbColumns, byte p_matrixType) {
        this._matrixType = p_matrixType;
        this._nbRows = p_nbRows;
        this._nbColumns = p_nbColumns;
        if(backend!=null){
            this._data=backend;
        }
        else {
            this._data=new double[_nbRows*_nbColumns];
        }
    }


    @Override
    public double[] data() {
        return _data;
    }

    @Override
    public void setData(double[] data) {
        System.arraycopy(data,0,this._data,0,data.length);
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
        if(_matrixType==KMatrixType.ROW_BASED){
            return _data[rowIndex+columnIndex*_nbRows];
        }
        else {
            return _data[columnIndex+rowIndex*_nbColumns];
        }
    }

    @Override
    public double set(int rowIndex, int columnIndex, double value) {
        if(_matrixType==KMatrixType.ROW_BASED){
            _data[rowIndex+columnIndex*_nbRows]=value;
        }
        else {
            _data[columnIndex+rowIndex*_nbColumns]=value;
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
            this._data[i]=value;
        }
    }

    @Override
    public double getAtIndex(int index) {
        return this._data[index];
    }

    @Override
    public double setAtIndex(int index, double value) {
        this._data[index]=value;
        return value;
    }

    @Override
    public double addAtIndex(int index, double value) {
        this._data[index]+=value;
        return this._data[index];
    }

    @Override
    public KMatrix clone() {
        return null;
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
