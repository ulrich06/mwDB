package org.mwdb.chunk;

import org.mwdb.KGraph;

public interface KChunkSpace {

    /**
     * Create KChunk, out of the space, not marked, used as a factory
     */
    KChunk create(byte type, long world, long time, long id, KBuffer initialPayload, KChunk origin);

    /**
     * Get and mark chunk for the association of keys
     */
    KChunk getAndMark(byte type, long world, long time, long id);

    /**
     * Insert the chunk into the space and mark it before as used
     */
    KChunk putAndMark(KChunk elem);

    /**
     * UnMark chunk for the association of keys
     */
    void unmark(byte type, long world, long time, long id);

    /**
     * UnMark chunk
     */
    void unmarkChunk(KChunk chunk);

    void freeChunk(KChunk chunk);

    /**
     * Declare the chunk as dirty
     */
    void declareDirty(KChunk elem);

    /**
     * Declare the chunk as clean
     */
    void declareClean(KChunk elem);

    /**
     * Set current working space
     */
    void setGraph(KGraph graph);

    void clear();

    void free();

    long size();

    KChunkIterator detachDirties();


}
