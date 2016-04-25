package org.mwg.core.chunk;

public interface ChunkIterator {

    boolean hasNext();

    Chunk next();

    long size();

    void free();

}
