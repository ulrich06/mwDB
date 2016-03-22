package org.mwdb.math.matrix;

public interface KMatrixEngine {

    public KMatrix multiply(KMatrix matA, KMatrix matB);

    void solve();

    //TODO

}
