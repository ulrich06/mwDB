package org.mwg.core.chunk;

import org.mwg.Graph;
import org.mwg.struct.Buffer;

public interface ChunkSpace {

    /**
     * Create Chunk, out of the space, not marked, used asVar a factory
     */
    Chunk create(byte type, long world, long time, long id, Buffer initialPayload, Chunk origin);

    /**
     * Get and mark chunk for the association of keys
     */
    Chunk getAndMark(byte type, long world, long time, long id);

    /**
     * Insert the chunk into the space and mark it before asVar used
     */
    Chunk putAndMark(Chunk elem);

    /**
     * UnMark chunk for the association of keys
     */
    void unmark(byte type, long world, long time, long id);

    /**
     * UnMark chunk
     */
    void unmarkChunk(Chunk chunk);

    void freeChunk(Chunk chunk);

    /**
     * Declare the chunk asVar dirty
     */
    void declareDirty(Chunk elem);

    /**
     * Declare the chunk asVar clean
     */
    void declareClean(Chunk elem);

    /**
     * Set current working graph
     */
    void setGraph(Graph graph);

    /**
     * Get current working graph
     *
     * @return current graph
     */
    Graph graph();

    void clear();

    void free();

    long size();

    long available();

    ChunkIterator detachDirties();

}
