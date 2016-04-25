package org.mwg.core.chunk.offheap;

import org.mwg.core.chunk.Chunk;

/**
 * @ignore ts
 */
public interface OffHeapChunk extends Chunk {

    long addr();
    
}
