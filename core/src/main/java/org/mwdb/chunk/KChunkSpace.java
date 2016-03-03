package org.mwdb.chunk;


import org.mwdb.KNode;

public interface KChunkSpace {

    /**
     * Create KChunk, out of the space, not marked, used as a factory
     */
    KChunk create(long world, long time, long id, short type);

    ///** Get the chunk, warning no garanty */
    //KChunk get(long world, long time, long id);

    KChunk getAndMark(long world, long time, long id);

    void unmark(long world, long time, long id);

    void unmarkChunk(KChunk elem);


    /**
     * ;
     * <p>
     * void unmark(long universe, long time, long obj);
     */

    KChunk putAndMark(KChunk elem);

    //void remove(long universe, long time, long obj);
    //KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel);

    void clear();

    void free();

    int size();

    void declareDirty(KChunk elem);

    KChunkIterator detachDirties();

    // void declareDirty(KChunk dirtyChunk);

    // void printDebug(KMetaModel p_metaModel);

}
