package org.mwg.core.chunk.offheap;

import org.mwg.plugin.Chunk;

/**
 * @ignore ts
 */
public interface OffHeapChunk extends Chunk {

    long addr();
    
}
