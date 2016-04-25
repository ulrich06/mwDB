package org.mwg.core.chunk.offheap;

import org.mwg.core.chunk.KChunk;

/**
 * @ignore ts
 */
public interface KOffHeapChunk extends KChunk {

    long addr();
    
}
