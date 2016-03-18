package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.KStateChunk;
import org.mwdb.chunk.offheap.*;

public class ChunkSpaceTest {
    @Test
    public void heapChunkSpaceTest() {
    }

    @Test
    public void offHeapChunkSpaceTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        OffHeapChunkSpace space = new OffHeapChunkSpace(10, 10);
        test(space);

        System.out.println(OffHeapLongArray.alloc_counter);

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }


    public void test(KChunkSpace space) {
        KStateChunk chunk = (KStateChunk) space.create(0, 0, 0, Constants.STATE_CHUNK, null, null);
        space.putAndMark(chunk);

//        chunk.set(0, KType.BOOL, true);

        space.free();
    }
}
