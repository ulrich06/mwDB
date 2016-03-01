package org.mwdb.chunk;

public interface KStateChunk extends KChunk {

    /* Stateful Management */
    // KObjectChunk clone(long p_universe, long p_time, long p_obj, KMetaModel p_metaClass);

    void set(String name, int elemType, Object elem);

    void get(String name);
    
}
