package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.Constants;
import org.mwg.core.chunk.heap.HeapChunk;
import org.mwg.core.utility.BufferBuilder;
import org.mwg.plugin.Chunk;
import org.mwg.struct.*;
import org.mwg.core.chunk.heap.HeapTimeTreeChunk;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.Unsafe;

public class TimeTreeTest implements ChunkListener {

    private interface KTimeTreeChunkFactory {
        TimeTreeChunk create(Buffer payload);
    }

    private int nbCount = 0;

    @Test
    public void heapTest() {
        final ChunkListener selfPointer = this;
        KTimeTreeChunkFactory factory = new KTimeTreeChunkFactory() {
            @Override
            public TimeTreeChunk create(Buffer payload) {
                return new HeapTimeTreeChunk(-1, -1, -1, selfPointer, payload);
            }
        };
        previousOrEqualsTest(factory);
        saveLoad(factory);
        massiveTest(factory);
        emptyHalf(factory);
        //savePrint(factory);

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
        KTimeTreeChunkFactory factory = new KTimeTreeChunkFactory() {
            @Override
            public TimeTreeChunk create(Buffer payload) {
                return new OffHeapTimeTreeChunk(selfPointer, org.mwg.core.CoreConstants.OFFHEAP_NULL_PTR, payload);
            }
        };
        previousOrEqualsTest(factory);
        saveLoad(factory);
        massiveTest(factory);
        emptyHalf(factory);

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);

    }

    /*
    private void savePrint(KTimeTreeChunkFactory factory) {
        TimeTreeChunk tree = factory.create(null);
        int nbElements = 10;
        Random random = new Random();
        for (int i = 0; i < nbElements; i++) {
            tree.insert(random.nextLong());
        }
        Buffer buffer = Buffer.newOffHeapBuffer();
        tree.save(buffer);

        System.out.println(new String(buffer.data()));

    }*/


    private void emptyHalf(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        TimeTreeChunk tree = factory.create(null);

        int nbElements = 10;

        for (int i = 0; i < nbElements; i++) {
            tree.insert(i);
        }

        final long[] nbCall = {0};
        tree.range(org.mwg.Constants.BEGINNING_OF_TIME, org.mwg.Constants.END_OF_TIME, tree.size() / 2, new TreeWalker() {
            @Override
            public void elem(long t) {
                nbCall[0]++;
            }
        });
        Assert.assertTrue((nbElements / 2) == nbCall[0]);

        final long[] median = new long[1];
        nbCall[0] = 0;
        tree.range(org.mwg.Constants.BEGINNING_OF_TIME, org.mwg.Constants.END_OF_TIME, tree.size() / 2, new TreeWalker() {
            @Override
            public void elem(long t) {
                median[0] = 5;
                nbCall[0]++;
            }
        });

        Assert.assertTrue(median[0] == 5);
        Assert.assertTrue(nbCall[0] == 5);

        tree.clearAt(median[0]);
        nbCall[0] = 0;
        tree.range(org.mwg.Constants.BEGINNING_OF_TIME, org.mwg.Constants.END_OF_TIME, org.mwg.Constants.END_OF_TIME, new TreeWalker() {
            @Override
            public void elem(long t) {
                nbCall[0]++;
            }
        });
        Assert.assertTrue(nbCall[0] == 5);

        free(tree);
        Assert.assertTrue(nbCount == 1);
    }

    private void previousOrEqualsTest(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        TimeTreeChunk tree = factory.create(null);
        for (long i = 0; i <= 6; i++) {
            tree.insert(i);
        }
        tree.insert(8L);
        tree.insert(10L);
        tree.insert(11L);
        tree.insert(13L);

        Assert.assertEquals(tree.previousOrEqual(-1), Constants.NULL_LONG);
        Assert.assertEquals(tree.previousOrEqual(0), 0L);
        Assert.assertEquals(tree.previousOrEqual(1), 1L);
        Assert.assertEquals(tree.previousOrEqual(7), 6L);
        Assert.assertEquals(tree.previousOrEqual(8), 8L);
        Assert.assertEquals(tree.previousOrEqual(9), 8L);
        Assert.assertEquals(tree.previousOrEqual(10), 10L);
        Assert.assertEquals(tree.previousOrEqual(13), 13L);
        Assert.assertEquals(tree.previousOrEqual(14), 13L);

        free(tree);
        Assert.assertTrue(nbCount == 1);
    }

    private void saveLoad(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        TimeTreeChunk tree = factory.create(null);
        for (long i = 0; i <= 2; i++) {
            tree.insert(i);
        }

        Buffer buffer = BufferBuilder.newOffHeapBuffer();
        tree.save(buffer);
        Assert.assertTrue(compareWithString(buffer, "A,C,E"));
        Assert.assertTrue(tree.size() == 3);

        TimeTreeChunk tree2 = factory.create(buffer);
        Assert.assertTrue(tree2.size() == 3);

        Buffer buffer2 = BufferBuilder.newOffHeapBuffer();
        tree2.save(buffer2);
        Assert.assertTrue(compareBuffers(buffer, buffer2));

        tree2.insert(10000);

        free(tree);
        free(tree2);

        buffer2.free();
        buffer.free();

        Assert.assertTrue(nbCount == 2);
    }

    private boolean compareWithString(Buffer buffer, String content) {
        for (int i = 0; i < content.length(); i++) {
            if (buffer.read(i) != content.codePointAt(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean compareBuffers(Buffer buffer, Buffer buffer2) {
        if (buffer.length() != buffer2.length()) {
            return false;
        }
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.read(i) != buffer2.read(i)) {
                return false;
            }
        }
        return true;
    }


    private void massiveTest(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        TimeTreeChunk tree = factory.create(null);
        long max = 24;
        for (long i = 0; i <= max; i = i + 2) {
            tree.insert(i);
        }
        for (long i = 1; i <= max; i = i + 2) {
            Assert.assertTrue(tree.previousOrEqual(i) == i - 1);
        }
        free(tree);
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
            ((HeapChunk) chunk).setFlags(org.mwg.core.CoreConstants.DIRTY_BIT, 0);
        } else if (chunk instanceof OffHeapChunk) {
            long addr = ((OffHeapChunk) chunk).addr();
            OffHeapLongArray.set(addr, org.mwg.core.CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS, org.mwg.core.CoreConstants.DIRTY_BIT);
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
            OffHeapTimeTreeChunk.free(((OffHeapChunk) chunk).addr());
        }
    }

}
