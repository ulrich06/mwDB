package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.KStateChunk;
import org.mwdb.chunk.KTimeTreeChunk;
import org.mwdb.chunk.KWorldOrderChunk;
import org.mwdb.chunk.heap.HeapChunkSpace;
import org.mwdb.chunk.offheap.*;

public class ChunkSpaceTest {
    @Test
    public void heapChunkSpaceTest() {
        test(new HeapChunkSpace(10, 10));
    }

    @Test
    public void offHeapChunkSpaceTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        OffHeapChunkSpace space = new OffHeapChunkSpace(10, 10);
        test(space);

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }


    public void test(KChunkSpace space) {
        KStateChunk stateChunk = (KStateChunk) space.create(0, 0, 0, Constants.STATE_CHUNK, null, null);
        space.putAndMark(stateChunk);

        KWorldOrderChunk worldOrderChunk = (KWorldOrderChunk) space.create(0, 0, 1, Constants.WORLD_ORDER_CHUNK, null, null);
        space.putAndMark(worldOrderChunk);

        KTimeTreeChunk timeTreeChunk = (KTimeTreeChunk) space.create(0, 0, 2, Constants.TIME_TREE_CHUNK, null, null);
        space.putAndMark(timeTreeChunk);

        space.free();
    }
}
