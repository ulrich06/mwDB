package org.mwg.core.chunk;

import org.mwg.plugin.Chunk;
import org.mwg.struct.LongLongMap;

public interface WorldOrderChunk extends Chunk, LongLongMap {

    long magic();

    void lock();

    void unlock();

    long extra();

    void setExtra(long extraValue);

}
