package org.mwdb.chunk;

public interface KChunk {

    long world();

    long time();

    long id();

    byte chunkType();

    int marks();

    long flags();

    String save();
    
}
