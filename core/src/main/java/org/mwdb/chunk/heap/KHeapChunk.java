package org.mwdb.chunk.heap;

import org.mwdb.chunk.KChunk;

public interface KHeapChunk extends KChunk {

    long mark();

    long unmark();

    boolean setFlags(long bitsToEnable, long bitsToDisable);

}
