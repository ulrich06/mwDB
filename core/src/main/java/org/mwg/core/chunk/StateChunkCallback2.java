package org.mwg.core.chunk;

@FunctionalInterface
public interface StateChunkCallback2 {

    void on(String attributeName, int elemType, Object elem);

}
