package org.mwg.core.chunk;

public interface KChunkIterator {

    boolean hasNext();

    KChunk next();

    long size();

    void free();

}
