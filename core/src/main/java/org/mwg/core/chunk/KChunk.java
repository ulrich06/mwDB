package org.mwg.core.chunk;

import org.mwg.struct.Buffer;

public interface KChunk {

    long world();

    long time();

    long id();

    byte chunkType();

    long marks();

    long flags();

    void save(Buffer buffer);

}
