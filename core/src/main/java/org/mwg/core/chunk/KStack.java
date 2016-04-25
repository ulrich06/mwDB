package org.mwg.core.chunk;

public interface KStack {

    boolean enqueue(long index);

    long dequeueTail();

    boolean dequeue(long index);

    void free();

}