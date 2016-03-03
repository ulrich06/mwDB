package org.mwdb.chunk;

public interface KStateChunkCallBack {

    void on(String attributeName, int elemType, Object elem);

}
