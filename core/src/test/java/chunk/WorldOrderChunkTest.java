package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.KChunk;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KWorldOrderChunk;
import org.mwdb.chunk.heap.HeapWorldOrderChunk;
import org.mwdb.chunk.heap.KHeapChunk;
import org.mwdb.chunk.offheap.KOffHeapChunk;
import org.mwdb.chunk.offheap.OffHeapLongArray;
import org.mwdb.chunk.offheap.OffHeapWorldOrderChunk;
import org.mwdb.utility.PrimitiveHelper;

public class WorldOrderChunkTest implements KChunkListener {

    private int nbCount = 0;

    private interface WorldOrderChunkFactory {
        KWorldOrderChunk create(String initialPayload);
    }

    @Test
    public void heapTest() {
        final KChunkListener selfPointer = this;
        WorldOrderChunkFactory factory = new WorldOrderChunkFactory() {

            @Override
            public KWorldOrderChunk create(String initialPayload) {
                return new HeapWorldOrderChunk(-1, -1, -1, selfPointer, initialPayload);
            }
        };
        orderTest(factory);
        saveLoadTest(factory);
    }

    @Test
    public void offHeapTest() {
        final KChunkListener selfPointer = this;
        WorldOrderChunkFactory factory = new WorldOrderChunkFactory() {

            @Override
            public KWorldOrderChunk create(String initialPayload) {
                return new OffHeapWorldOrderChunk(selfPointer, Constants.OFFHEAP_NULL_PTR, initialPayload);
            }
        };
        orderTest(factory);
        saveLoadTest(factory);
    }

    private void orderTest(WorldOrderChunkFactory factory) {
        nbCount = 0;
        KWorldOrderChunk map = factory.create(null);
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

        KWorldOrderChunk map = factory.create(null);
        //mass insert
        for (long i = 0; i < 10_000; i++) {
            map.put(i, i * 3);
        }
        Assert.assertTrue(map.size() == 10_000);

        String saved = map.save();
        KWorldOrderChunk map2 = factory.create(saved);
        for (long i = 0; i < 10_000; i++) {
            Assert.assertTrue(map2.get(i) == i * 3);
        }
        String saved2 = map2.save();
        Assert.assertTrue(PrimitiveHelper.equals(saved, saved2));
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
            OffHeapWorldOrderChunk.free(((KOffHeapChunk) chunk).addr());
        }
    }

}
