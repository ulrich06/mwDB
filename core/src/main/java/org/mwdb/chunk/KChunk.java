package org.mwdb.chunk;

import org.mwdb.plugin.KStorage;

public interface KChunk {

    long world();

    long time();

    long id();

    byte chunkType();

    long marks();

    long flags();

    void save(KStorage.KBuffer buffer);

}
