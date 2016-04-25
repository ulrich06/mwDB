package org.mwg.core.chunk;

public interface StateChunkCallBack {

    void on(String attributeName, int elemType, Object elem);

}
