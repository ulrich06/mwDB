package org.mwdb.chunk.offheap;

import org.mwdb.chunk.KChunk;

/**
 * @ignore ts
 */
public interface KOffHeapChunk extends KChunk {

    long addr();
    
}
