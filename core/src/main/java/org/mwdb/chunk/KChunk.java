package org.mwdb.chunk;

public interface KChunk {

    /**
     * Identification management
     */
    long world();

    long time();

    long id();

    /** Life cycle management */

    /**
     * format: definition repeat all entry ...
     * KTree: ]=>red right [=>red left }=>black right {=>black left
     * KLongTree: size,root_index ... SEP key,parent_index ...]
     * KLongLongTree: size,root_index[... SEP key,parent_index,value ...]
     * KMemorySegment: {... ,"name":value ...}
     * KUniverseOrderMap: className, size{... ,"key":value ...}
     */
    String serialize();

    void init(String payload);

    /**
     * Memory marks management
     */
    int marks();

    int mark();

    int unmark();

    void free();

    /**
     * Flags management
     */
    long getFlags();

    void setFlags(long bitsToEnable, long bitsToDisable);

    /**
     * Utility methods
     */
    short chunkType();

    /*
    KChunkSpace space();
    */
}
