package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.KBuffer;
import org.mwdb.chunk.KChunk;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KWorldOrderChunk;
import org.mwdb.chunk.heap.HeapWorldOrderChunk;
import org.mwdb.chunk.heap.KHeapChunk;
import org.mwdb.chunk.offheap.KOffHeapChunk;
import org.mwdb.chunk.offheap.OffHeapLongArray;
import org.mwdb.chunk.offheap.OffHeapWorldOrderChunk;
import org.mwdb.utility.Buffer;

public class WorldOrderChunkTest implements KChunkListener {

    private int nbCount = 0;

    private interface WorldOrderChunkFactory {
        KWorldOrderChunk create(KBuffer initialPayload);
    }

    @Test
    public void heapTest() {
        final KChunkListener selfPointer = this;
        WorldOrderChunkFactory factory = new WorldOrderChunkFactory() {

            @Override
            public KWorldOrderChunk create(KBuffer initialPayload) {
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
            public KWorldOrderChunk create(KBuffer initialPayload) {
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

        KBuffer buffer = Buffer.newHeapBuffer();
        map.save(buffer);
        KWorldOrderChunk map2 = factory.create(buffer);
        for (long i = 0; i < 10_000; i++) {
            Assert.assertTrue(map2.get(i) == i * 3);
        }
        KBuffer buffer2 = Buffer.newHeapBuffer();
        map2.save(buffer2);
        Assert.assertTrue(compareBuffers(buffer, buffer2));
        buffer.free();
        buffer2.free();
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

}
