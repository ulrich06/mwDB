package org.mwdb.chunk;


public interface KChunkSpace {

    // void setManager(KDataManager dataManager);

    KChunk get(long world, long time, long id);

    KChunk put(long world, long time, long id, KChunk elem);

    void remove(long universe, long time, long obj);

    KChunk create(long world, long time, long id, short type);

    //KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel);

    void clear();

    void free();

    int size();

    KChunkIterator detachDirties();

    // void declareDirty(KChunk dirtyChunk);

    // void printDebug(KMetaModel p_metaModel);

}
