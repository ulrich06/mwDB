package org.mwdb.chunk;

public interface KStack {

    void enqueue(int index);

    int dequeueTail();

    int dequeue(int index);

    void reenqueue(int index);

}