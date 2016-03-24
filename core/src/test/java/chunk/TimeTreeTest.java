package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KConstants;
import org.mwdb.chunk.*;
import org.mwdb.chunk.heap.HeapTimeTreeChunk;
import org.mwdb.chunk.heap.KHeapChunk;
import org.mwdb.chunk.offheap.*;
import org.mwdb.utility.Buffer;

public class TimeTreeTest implements KChunkListener {

    private interface KTimeTreeChunkFactory {
        KTimeTreeChunk create(KBuffer payload);
    }

    private int nbCount = 0;

    @Test
    public void heapTest() {
        KChunkListener selfPointer = this;
        KTimeTreeChunkFactory factory = new KTimeTreeChunkFactory() {
            @Override
            public KTimeTreeChunk create(KBuffer payload) {
                return new HeapTimeTreeChunk(-1, -1, -1, selfPointer, payload);
            }
        };
        previousOrEqualsTest(factory);
        saveLoad(factory);
        massiveTest(factory);
        emptyHalf(factory);
    }

    @Test
    public void offHeapTest() {

        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        KChunkListener selfPointer = this;
        KTimeTreeChunkFactory factory = new KTimeTreeChunkFactory() {
            @Override
            public KTimeTreeChunk create(KBuffer payload) {
                return new OffHeapTimeTreeChunk(selfPointer, Constants.OFFHEAP_NULL_PTR, payload);
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

    private void emptyHalf(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        KTimeTreeChunk tree = factory.create(null);

        int nbElements = 10;

        for (int i = 0; i < nbElements; i++) {
            tree.insert(i);
        }

        final long[] nbCall = {0};
        tree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, tree.size() / 2, new KTreeWalker() {
            @Override
            public void elem(long t) {
                nbCall[0]++;
            }
        });
        Assert.assertTrue((nbElements / 2) == nbCall[0]);

        final long[] median = new long[1];
        nbCall[0] = 0;
        tree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, tree.size() / 2, new KTreeWalker() {
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
        tree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, Constants.END_OF_TIME, new KTreeWalker() {
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
        KTimeTreeChunk tree = factory.create(null);
        for (long i = 0; i <= 6; i++) {
            tree.insert(i);
        }
        tree.insert(8L);
        tree.insert(10L);
        tree.insert(11L);
        tree.insert(13L);

        Assert.assertEquals(tree.previousOrEqual(-1), KConstants.NULL_LONG);
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
        KTimeTreeChunk tree = factory.create(null);
        for (long i = 0; i <= 2; i++) {
            tree.insert(i);
        }

        KBuffer buffer = Buffer.newOffHeapBuffer();
        tree.save(buffer);
        Assert.assertTrue(compareWithString(buffer, "G,C{A,C]C,}E,C"));
        Assert.assertTrue(tree.size() == 3);

        KTimeTreeChunk tree2 = factory.create(buffer);
        Assert.assertTrue(tree2.size() == 3);

        KBuffer buffer2 = Buffer.newOffHeapBuffer();
        tree2.save(buffer2);
        Assert.assertTrue(compareBuffers(buffer, buffer2));

        tree2.insert(10_000);

        free(tree);
        free(tree2);

        buffer2.free();
        buffer.free();

        Assert.assertTrue(nbCount == 2);
    }

    private boolean compareWithString(KBuffer buffer, String content) {
        for (int i = 0; i < content.length(); i++) {
            if (buffer.read(i) != content.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean compareBuffers(KBuffer buffer, KBuffer buffer2) {
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


    private void massiveTest(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        KTimeTreeChunk tree = factory.create(null);
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


    @Override
    public void declareDirty(KChunk chunk) {
        nbCount++;
        //simulate space management
        if (chunk instanceof KHeapChunk) {
            ((KHeapChunk) chunk).setFlags(Constants.DIRTY_BIT, 0);
        } else if (chunk instanceof KOffHeapChunk) {
            long addr = ((KOffHeapChunk) chunk).addr();
            OffHeapLongArray.set(addr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS, Constants.DIRTY_BIT);
        }
    }

    private void free(KChunk chunk) {
        if (chunk instanceof KOffHeapChunk) {
            OffHeapTimeTreeChunk.free(((KOffHeapChunk) chunk).addr());
        }
    }

}
