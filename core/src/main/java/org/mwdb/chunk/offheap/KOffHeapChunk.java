package org.mwdb.chunk.offheap;

import org.mwdb.chunk.KChunk;

public interface KOffHeapChunk extends KChunk {

    long addr();
    
}
