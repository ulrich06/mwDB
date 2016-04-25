package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.struct.Buffer;
import org.mwg.core.Constants;
import org.mwg.core.chunk.heap.HeapWorldOrderChunk;
import org.mwg.core.chunk.heap.HeapChunk;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.Unsafe;

public class WorldOrderChunkTest implements ChunkListener {

    private int nbCount = 0;

    private interface WorldOrderChunkFactory {
        WorldOrderChunk create(Buffer initialPayload);
    }

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
                return new OffHeapWorldOrderChunk(selfPointer, Constants.OFFHEAP_NULL_PTR, initialPayload);
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
        for (long i = 0; i < 10_000; i++) {
            map.put(i, i * 3);
        }
        //mass check
        for (long i = 0; i < 10_000; i++) {
            Assert.assertTrue(map.get(i) == i * 3);
        }
        free(map);
        Assert.assertTrue(nbCount == 1);
    }

    private void saveLoadTest(WorldOrderChunkFactory factory) {
        nbCount = 0;

        WorldOrderChunk map = factory.create(null);
        //mass insert
        for (long i = 0; i < 10_000; i++) {
            map.put(i, i * 3);
        }
        Assert.assertTrue(map.extra() == Constants.NULL_LONG);
        map.setExtra(1_000_000);
        Assert.assertTrue(map.size() == 10_000);
        Assert.assertTrue(map.extra() == 1_000_000);

        Buffer buffer = org.mwg.core.utility.Buffer.newHeapBuffer();
        map.save(buffer);
        WorldOrderChunk map2 = factory.create(buffer);
        for (long i = 0; i < 10_000; i++) {
            Assert.assertTrue(map2.get(i) == i * 3);
        }
        Assert.assertTrue(map2.extra() == 1_000_000);

        Buffer buffer2 = org.mwg.core.utility.Buffer.newHeapBuffer();
        map2.save(buffer2);
        Assert.assertTrue(compareBuffers(buffer, buffer2));
        buffer.free();
        buffer2.free();

        free(map);
        free(map2);

        Assert.assertTrue(nbCount == 1);
    }

    @Override
    public void declareDirty(Chunk chunk) {
        nbCount++;
        //simulate space management
        if (chunk instanceof HeapChunk) {
            ((HeapChunk) chunk).setFlags(Constants.DIRTY_BIT, 0);
        } else if (chunk instanceof OffHeapChunk) {
            long addr = ((OffHeapChunk) chunk).addr();
            OffHeapLongArray.set(addr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS, Constants.DIRTY_BIT);
        }
    }

    @Override
    public Graph graph() {
        return null;
    }

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