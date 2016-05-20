package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.BufferBuilder;
import org.mwg.plugin.Chunk;
import org.mwg.struct.Buffer;
import org.mwg.core.chunk.heap.HeapWorldOrderChunk;
import org.mwg.core.chunk.heap.HeapChunk;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.Unsafe;

public class WorldOrderChunkTest implements ChunkListener {

    private int nbCount = 0;

    private interface WorldOrderChunkFactory {
        WorldOrderChunk create(Buffer initialPayload);
    }

    /**
     * @ignore ts
     */
    @Test
    public void heapTest() {
        final ChunkListener selfPointer = this;
        WorldOrderChunkFactory factory = new WorldOrderChunkFactory() {

            @Override
            public WorldOrderChunk create(Buffer initialPayload) {
                return new HeapWorldOrderChunk(-1, -1, -1, selfPointer, initialPayload);
            }
        };
        orderTest(factory);
        saveLoadTest(factory);
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
        WorldOrderChunkFactory factory = new WorldOrderChunkFactory() {

            @Override
            public WorldOrderChunk create(Buffer initialPayload) {
                return new OffHeapWorldOrderChunk(selfPointer, CoreConstants.OFFHEAP_NULL_PTR, initialPayload);
            }
        };
        orderTest(factory);
        saveLoadTest(factory);

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);

    }

    private void orderTest(WorldOrderChunkFactory factory) {
        nbCount = 0;
        WorldOrderChunk map = factory.create(null);
        //mass insert
        for (long i = 0; i < 10000; i++) {
            map.put(i, i * 3);
        }
        //mass check
        for (long i = 0; i < 10000; i++) {
            Assert.assertTrue(map.get(i) == i * 3);
        }
        free(map);
        Assert.assertTrue(nbCount == 1);
    }

    private void saveLoadTest(WorldOrderChunkFactory factory) {
        nbCount = 0;

        WorldOrderChunk map = factory.create(null);
        //mass insert
        for (long i = 0; i < 10000; i++) {
            map.put(i, i * 3);
        }
        Assert.assertTrue(map.extra() == CoreConstants.NULL_LONG);
        map.setExtra(1000000);
        Assert.assertTrue(map.size() == 10000);
        Assert.assertTrue(map.extra() == 1000000);

        Buffer buffer = BufferBuilder.newHeapBuffer();
        map.save(buffer);
        WorldOrderChunk map2 = factory.create(buffer);
        for (long i = 0; i < 10000; i++) {
            Assert.assertTrue(map2.get(i) == i * 3);
        }
        Assert.assertTrue(map2.extra() == 1000000);

        Buffer buffer2 = BufferBuilder.newHeapBuffer();
        map2.save(buffer2);
        Assert.assertTrue(compareBuffers(buffer, buffer2));
        buffer.free();
        buffer2.free();

        free(map);
        free(map2);

        Assert.assertTrue(nbCount == 1);
    }

    /**
     * @native ts
     * this.nbCount++;
     * (<org.mwg.core.chunk.heap.HeapChunk>chunk).setFlags(org.mwg.core.CoreConstants.DIRTY_BIT, 0);
     */
    @Override
    public void declareDirty(Chunk chunk) {
        nbCount++;
        //simulate space management
        if (chunk instanceof HeapChunk) {
            ((HeapChunk) chunk).setFlags(CoreConstants.DIRTY_BIT, 0);
        } else if (chunk instanceof OffHeapChunk) {
            long addr = ((OffHeapChunk) chunk).addr();
            OffHeapLongArray.set(addr, CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS, CoreConstants.DIRTY_BIT);
        }
    }

    @Override
    public Graph graph() {
        return null;
    }

    /**
     * @native ts
     */
    private void free(Chunk chunk) {
        if (chunk instanceof OffHeapChunk) {
            OffHeapWorldOrderChunk.free(((OffHeapChunk) chunk).addr());
        }
    }

    private boolean compareBuffers(Buffer buffer, Buffer buffer2) {
        if (buffer.size() != buffer2.size()) {
            return false;
        }
        for (int i = 0; i < buffer.size(); i++) {
            if (buffer.read(i) != buffer2.read(i)) {
                return false;
            }
        }
        return true;
    }

}
