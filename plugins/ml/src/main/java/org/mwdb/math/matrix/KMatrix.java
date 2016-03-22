package org.mwdb.math.matrix;

public interface KMatrix {

    double[] data();

    byte matrixType();

    int rows();

    int columns();

    double get(int rowIndex, int columnIndex);

    double set(int rowIndex, int columnIndex, double value);

}
