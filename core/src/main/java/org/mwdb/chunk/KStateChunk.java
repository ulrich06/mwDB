package org.mwdb.chunk;

public interface KStateChunk extends KChunk {

    /* Stateful Management */
    // KObjectChunk clone(long p_universe, long p_time, long p_obj, KMetaModel p_metaClass);

    void set(long index, int elemType, Object elem);

    Object get(long index);

}
