package org.mwg.core.chunk;

@FunctionalInterface
public interface StateChunkCallback {

    void on(String attributeName, int elemType, Object elem);

}
