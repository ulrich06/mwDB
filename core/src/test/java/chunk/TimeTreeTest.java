package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KConstants;
import org.mwdb.chunk.KChunk;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KTimeTreeChunk;
import org.mwdb.chunk.heap.HeapTimeTreeChunk;
import org.mwdb.chunk.heap.KHeapChunk;
import org.mwdb.chunk.offheap.KOffHeapChunk;
import org.mwdb.chunk.offheap.OffHeapLongArray;
import org.mwdb.chunk.offheap.OffHeapTimeTreeChunk;
import org.mwdb.utility.PrimitiveHelper;

public class TimeTreeTest implements KChunkListener {

    private interface KTimeTreeChunkFactory {
        KTimeTreeChunk create(String payload);
    }

    private int nbCount = 0;

    @Test
    public void heapTest() {
        KChunkListener selfPointer = this;
        KTimeTreeChunkFactory factory = new KTimeTreeChunkFactory() {
            @Override
            public KTimeTreeChunk create(String payload) {
                return new HeapTimeTreeChunk(-1, -1, -1, selfPointer, payload);
            }
        };
        previousOrEqualsTest(factory);
        saveLoad(factory);
        massiveTest(factory);
    }

    @Test
    public void offHeapTest() {
        KChunkListener selfPointer = this;
        KTimeTreeChunkFactory factory = new KTimeTreeChunkFactory() {
            @Override
            public KTimeTreeChunk create(String payload) {
                return new OffHeapTimeTreeChunk(selfPointer, Constants.OFFHEAP_NULL_PTR, payload);
            }
        };
        previousOrEqualsTest(factory);
        saveLoad(factory);
        massiveTest(factory);
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
        String saved0 = tree.save();
        Assert.assertEquals("G,C{A,C]C,}E,C", saved0);
        Assert.assertTrue(tree.size() == 3);

        KTimeTreeChunk tree2 = factory.create(saved0);
        Assert.assertTrue(tree2.size() == 3);
        String saved2 = tree2.save();
        Assert.assertTrue(PrimitiveHelper.equals(saved0, saved2));

        tree2.insert(10_000);

        free(tree);
        free(tree2);

        Assert.assertTrue(nbCount == 2);
    }

    private void massiveTest(KTimeTreeChunkFactory factory) {
        nbCount = 0;
        KTimeTreeChunk tree = factory.create(null);
        //  long beforeTime = System.currentTimeMillis();
        for (long i = 0; i <= 1_000_000; i = i + 2) {
            tree.insert(i);
        }
        // System.out.println(System.currentTimeMillis() - beforeTime);

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
            ((KOffHeapChunk) chunk).free();
        }
    }

}
