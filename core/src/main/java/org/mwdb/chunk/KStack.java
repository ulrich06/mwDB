package org.mwdb.chunk;

public interface KStack {

    boolean enqueue(long index);

    long dequeueTail();

    boolean dequeue(long index);

    void free();

}