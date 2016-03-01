package org.mwdb.chunk;

public interface KChunkIterator {

    boolean hasNext();

    KChunk next();

    int size();

}
