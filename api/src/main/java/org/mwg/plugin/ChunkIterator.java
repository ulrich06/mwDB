package org.mwg.plugin;

public interface ChunkIterator {

    boolean hasNext();

    Chunk next();

    long size();

    void free();

}
