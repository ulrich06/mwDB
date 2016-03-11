package org.mwdb.chunk;

public interface KChunkIterator {

    boolean hasNext();

    KChunk next();

    long size();

}
