package org.mwg.core.chunk;

import org.mwg.struct.Buffer;

public interface Chunk {

    long world();

    long time();

    long id();

    byte chunkType();

    long marks();

    long flags();

    void save(Buffer buffer);

}
