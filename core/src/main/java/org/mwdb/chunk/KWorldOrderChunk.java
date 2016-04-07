package org.mwdb.chunk;

public interface KWorldOrderChunk extends KChunk, KLongLongMap {

    long magic();

    void lock();

    void unlock();

    long extra();

    void setExtra(long extraValue);

}
