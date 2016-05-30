package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.heap.HeapChunkSpace;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.ChunkSpace;
import org.mwg.plugin.ChunkType;

public class ChunkSpaceTest {
    @Test
    public void heapChunkSpaceTest() {
        test(new HeapChunkSpace(10, 10));
    }

    /**
     * @ignore ts
     */
    @Test
    public void offHeapChunkSpaceTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        OffHeapChunkSpace space = new OffHeapChunkSpace(10, 10);
        test(space);

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }


    public void test(ChunkSpace space) {
        StateChunk stateChunk = (StateChunk) space.create(ChunkType.STATE_CHUNK, 0, 0, 0, null, null);
        space.putAndMark(stateChunk);

        WorldOrderChunk worldOrderChunk = (WorldOrderChunk) space.create(ChunkType.WORLD_ORDER_CHUNK, 0, 0, 1, null, null);
        space.putAndMark(worldOrderChunk);

        TimeTreeChunk timeTreeChunk = (TimeTreeChunk) space.create(ChunkType.TIME_TREE_CHUNK, 0, 0, 2, null, null);
        space.putAndMark(timeTreeChunk);

        GenChunk genChunk = (GenChunk) space.create(ChunkType.GEN_CHUNK, 1, 1, 1, null, null);
        space.putAndMark(genChunk);

        space.free();
    }
}
