package org.mwdb.chunk;

public interface KStack {

    boolean enqueue(int index);

    long dequeueTail();

    boolean dequeue(int index);

    // void reenqueue(int index);

}