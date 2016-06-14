package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.heap.HeapGenChunk;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.BufferBuilder;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.Base64;
import org.mwg.plugin.Chunk;
import org.mwg.struct.Buffer;

public class GenChunkTest implements ChunkListener {

    @Override
    public void declareDirty(Chunk chunk) {

    }

    @Override
    public Graph graph() {
        return null;
    }

    private interface GenChunkFactory {
        GenChunk create(long id, Buffer payload);
    }

    @Test
    public void heapTest() {
        final ChunkListener selfPointer = this;
        GenChunkTest.GenChunkFactory factory = new GenChunkTest.GenChunkFactory() {
            @Override
            public GenChunk create(long id, Buffer payload) {
                return new HeapGenChunk(-1, -1, id, selfPointer, payload);
            }
        };
        genTest(factory);
    }

    /**
     * @ignore ts
     */
    @Test
    public void offHeapTest() {

        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        final ChunkListener selfPointer = this;
        GenChunkTest.GenChunkFactory factory = new GenChunkTest.GenChunkFactory() {
            @Override
            public GenChunk create(long id, Buffer payload) {
                OffHeapGenChunk newly = new OffHeapGenChunk(selfPointer, org.mwg.core.CoreConstants.OFFHEAP_NULL_PTR, payload);
                OffHeapLongArray.set(newly.addr(), CoreConstants.OFFHEAP_CHUNK_INDEX_ID, id);
                return newly;
            }
        };

        genTest(factory);

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);

    }

    /**
     * @native ts
     */
    private void free(GenChunk chunk) {
        if (chunk instanceof OffHeapChunk) {
            OffHeapGenChunk.free(((OffHeapChunk) chunk).addr());
        }
    }

    private void genTest(GenChunkTest.GenChunkFactory factory) {

        GenChunk genChunk = factory.create(0, null);
        Assert.assertEquals(genChunk.newKey(), 1);
        Assert.assertEquals(genChunk.newKey(), 2);
        Assert.assertEquals(genChunk.newKey(), 3);

        free(genChunk);

        Buffer buf = BufferBuilder.newHeapBuffer();
        Base64.encodeLongToBuffer(100, buf);
        GenChunk genChunk2 = factory.create(0, buf);
        buf.free();

        Assert.assertEquals(genChunk2.newKey(), 101);
        Assert.assertEquals(genChunk2.newKey(), 102);
        Assert.assertEquals(genChunk2.newKey(), 103);

        Buffer bufSave = BufferBuilder.newHeapBuffer();
        genChunk2.save(bufSave);
        byte[] flatSaved = bufSave.data();
        Assert.assertEquals(flatSaved.length, 2);
        Assert.assertEquals(flatSaved[0], 68);
        Assert.assertEquals(flatSaved[1], 79);
        Assert.assertEquals(103, Base64.decodeToLongWithBounds(bufSave, 0, 2));

        bufSave.free();
        free(genChunk2);


        GenChunk genChunk_100 = factory.create(100, null);
        Assert.assertEquals(genChunk_100.newKey(), 13743895347201L);
        Assert.assertEquals(genChunk_100.newKey(), 13743895347202L);

        free(genChunk_100);


    }


}
