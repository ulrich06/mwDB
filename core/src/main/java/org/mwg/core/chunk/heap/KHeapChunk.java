package org.mwg.core.chunk.heap;

import org.mwg.core.chunk.KChunk;

public interface KHeapChunk extends KChunk {

    long mark();

    long unmark();

    boolean setFlags(long bitsToEnable, long bitsToDisable);

}
