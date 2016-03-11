package org.mwdb.chunk.offheap;

public interface KOffHeapChunk {

    long addr();

    void free();

}
