package org.mwdb.chunk;

public interface KStack {

    boolean enqueue(int index);

    int dequeueTail();

    boolean dequeue(int index);

    // void reenqueue(int index);

}