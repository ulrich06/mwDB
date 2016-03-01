package org.mwdb.chunk;

public interface KStack {

    void enqueue(int index);

    int dequeue();

    void reenqueue(int index);

}