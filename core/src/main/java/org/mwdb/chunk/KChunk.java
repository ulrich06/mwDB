package org.mwdb.chunk;

public interface KChunk {

    /**
     * Identification management
     */
    long world();

    long time();

    long id();

    String save();

    void load(String payload);

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
    long flags();

    void setFlags(long bitsToEnable, long bitsToDisable);

    /**
     * Utility methods
     */
    byte chunkType();

}
