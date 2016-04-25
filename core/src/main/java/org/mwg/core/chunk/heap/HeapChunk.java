package org.mwg.core.chunk.heap;

import org.mwg.core.chunk.Chunk;

public interface HeapChunk extends Chunk {

    long mark();

    long unmark();

    boolean setFlags(long bitsToEnable, long bitsToDisable);

}
