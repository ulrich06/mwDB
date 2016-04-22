package org.mwdb.chunk;

import org.mwdb.KGraph;

public interface KChunkSpace {

    /**
     * Create KChunk, out of the space, not marked, used asVar a factory
     */
    KChunk create(byte type, long world, long time, long id, KBuffer initialPayload, KChunk origin);

    /**
     * Get and mark chunk for the association of keys
     */
    KChunk getAndMark(byte type, long world, long time, long id);

    /**
     * Insert the chunk into the space and mark it before asVar used
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
     * Declare the chunk asVar dirty
     */
    void declareDirty(KChunk elem);

    /**
     * Declare the chunk asVar clean
     */
    void declareClean(KChunk elem);

    /**
     * Set current working graph
     */
    void setGraph(KGraph graph);

    /**
     * Get current working graph
     *
     * @return current graph
     */
    KGraph graph();

    void clear();

    void free();

    long size();

    KChunkIterator detachDirties();


}
