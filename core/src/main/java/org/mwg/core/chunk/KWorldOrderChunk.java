package org.mwg.core.chunk;

import org.mwg.struct.LongLongMap;

public interface KWorldOrderChunk extends KChunk, LongLongMap {

    long magic();

    void lock();

    void unlock();

    long extra();

    void setExtra(long extraValue);

}
