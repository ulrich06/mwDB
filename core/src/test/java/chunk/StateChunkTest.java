package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KConstants;
import org.mwdb.KType;
import org.mwdb.chunk.*;
import org.mwdb.chunk.heap.HeapStateChunk;
import org.mwdb.chunk.heap.KHeapChunk;
import org.mwdb.chunk.offheap.KOffHeapChunk;
import org.mwdb.chunk.offheap.OffHeapLongArray;
import org.mwdb.utility.PrimitiveHelper;

public class StateChunkTest implements KChunkListener {

    private int nbCount = 0;

    public interface StateChunkFactory {
        KStateChunk create(KChunkListener listener, String payload, KChunk origin);
    }

    @Test
    public void heapStateChunkTest() {

        StateChunkFactory factory = new StateChunkFactory() {

            @Override
            public KStateChunk create(KChunkListener listener, String payload, KChunk origin) {
                return new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, listener, payload, origin);
            }
        };

        saveLoadTest(factory);
        protectionTest(factory);
    }

    private void saveLoadTest(StateChunkFactory factory) {
        //reset nb count
        nbCount = 0;

        KStateChunk chunk = factory.create(this, null, null);

        //init chunk with primitives
        chunk.set(0, KType.BOOL, true);
        chunk.set(1, KType.STRING, "hello");
        chunk.set(2, KType.DOUBLE, 1.0);
        chunk.set(3, KType.LONG, 1000l);
        chunk.set(4, KType.INT, 100);

        String savedChunk = chunk.save();
        KStateChunk chunk2 = factory.create(this, savedChunk, null);
        String savedChunk2 = chunk2.save();

        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk2));

        for (int i = 0; i < 5; i++) {
            if (i == 1) {
                Assert.assertTrue(PrimitiveHelper.equals(chunk.get(i).toString(), chunk2.get(i).toString()));
            } else {
                Assert.assertTrue(chunk.get(i).equals(chunk2.get(i)));
            }
        }

        //init chunk with arrays
        chunk.set(5, KType.LONG_ARRAY, new long[]{0, 1, 2, 3, 4});
        chunk.set(6, KType.DOUBLE_ARRAY, new double[]{0.1, 1.1, 2.1, 3.1, 4.1});
        chunk.set(7, KType.INT_ARRAY, new int[]{0, 1, 2, 3, 4});

        savedChunk = chunk.save();
        free(chunk2);
        chunk2 = factory.create(this, savedChunk, null);
        savedChunk2 = chunk2.save();

        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk2));

        //init chunk with some maps
        KLongLongMap long2longMap = (KLongLongMap) chunk.getOrCreate(8, KType.LONG_LONG_MAP);
        long2longMap.put(1, 1);
        long2longMap.put(Constants.END_OF_TIME, Constants.END_OF_TIME);
        long2longMap.put(Constants.BEGINNING_OF_TIME, Constants.BEGINNING_OF_TIME);

        KStringLongMap string2longMap = (KStringLongMap) chunk.getOrCreate(9, KType.STRING_LONG_MAP);
        string2longMap.put("1", 1);
        string2longMap.put(Constants.END_OF_TIME + "", Constants.END_OF_TIME);
        string2longMap.put(Constants.BEGINNING_OF_TIME + "", Constants.BEGINNING_OF_TIME);

        KLongLongArrayMap long2longArrayMap = (KLongLongArrayMap) chunk.getOrCreate(10, KType.LONG_LONG_ARRAY_MAP);
        long2longArrayMap.put(1, 1);
        long2longArrayMap.put(Constants.END_OF_TIME, Constants.END_OF_TIME);
        long2longArrayMap.put(Constants.BEGINNING_OF_TIME, Constants.BEGINNING_OF_TIME);
        long2longArrayMap.put(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME);

        savedChunk = chunk.save();
        free(chunk2);
        chunk2 = factory.create(this, savedChunk, null);
        savedChunk2 = chunk2.save();

        Assert.assertTrue(((KStringLongMap) chunk2.get(9)).size() == 3);
        Assert.assertTrue(((KLongLongMap) chunk2.get(8)).size() == 3);
        Assert.assertTrue(((KLongLongArrayMap) chunk2.get(10)).size() == 4);

        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk2));
        Assert.assertTrue(1 == nbCount);

        //force reHash
        for (int i = 0; i < 10; i++) {
            chunk.set(1000 + i, KType.INT, i);
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(chunk.get(1000 + i).equals(i));
        }

        KStateChunk chunk3 = factory.create(this, null, chunk);
        String savedChunk3 = chunk3.save();
        savedChunk = chunk.save();
        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk3));

        free(chunk3);
        free(chunk2);
        free(chunk);

    }

    private void free(KStateChunk chunk) {
        if (chunk instanceof KOffHeapChunk) {
            ((KOffHeapChunk) chunk).free();
        }
    }

    private void protectionTest(StateChunkFactory factory) {

        KStateChunk chunk = factory.create(this, null, null);

        //boolean protection test
        protectionMethod(chunk, KType.BOOL, null, true);
        protectionMethod(chunk, KType.BOOL, true, false);
        protectionMethod(chunk, KType.BOOL, "Hello", true);

        protectionMethod(chunk, KType.DOUBLE, null, true);
        protectionMethod(chunk, KType.DOUBLE, 0.5d, false);
        protectionMethod(chunk, KType.DOUBLE, "Hello", true);

        protectionMethod(chunk, KType.LONG, null, true);
        protectionMethod(chunk, KType.LONG, 100000000l, false);
        protectionMethod(chunk, KType.LONG, "Hello", true);

        protectionMethod(chunk, KType.INT, null, true);
        protectionMethod(chunk, KType.INT, 10, false);
        protectionMethod(chunk, KType.INT, "Hello", true);

        protectionMethod(chunk, KType.STRING, null, false);
        protectionMethod(chunk, KType.STRING, "Hello", false);
        protectionMethod(chunk, KType.STRING, true, true);

        //arrays
        protectionMethod(chunk, KType.DOUBLE_ARRAY, new double[]{0.1d, 0.2d, 0.3d}, false);
        protectionMethod(chunk, KType.DOUBLE_ARRAY, "hello", true);

        protectionMethod(chunk, KType.LONG_ARRAY, new long[]{10l, 100l, 1000l}, false);
        protectionMethod(chunk, KType.LONG_ARRAY, "hello", true);

        protectionMethod(chunk, KType.INT_ARRAY, new int[]{10, 100, 1000}, false);
        protectionMethod(chunk, KType.INT_ARRAY, "hello", true);

        //maps
        protectionMethod(chunk, KType.STRING_LONG_MAP, "hello", true);
        protectionMethod(chunk, KType.LONG_LONG_MAP, "hello", true);
        protectionMethod(chunk, KType.LONG_LONG_ARRAY_MAP, "hello", true);

        free(chunk);

    }

    private void protectionMethod(KStateChunk chunk, byte elemType, Object elem, boolean shouldCrash) {
        boolean hasCrash = false;
        try {
            chunk.set(0, elemType, elem);
        } catch (Throwable e) {
            hasCrash = true;
        }
        Assert.assertTrue(hasCrash == shouldCrash);
    }

    @Override
    public void declareDirty(KChunk chunk) {
        nbCount++;
        //simulate space management

        if(chunk instanceof KHeapChunk){
            ((KHeapChunk) chunk).setFlags(Constants.DIRTY_BIT,0);
        } else if(chunk instanceof KOffHeapChunk){
            long addr = ((KOffHeapChunk) chunk).addr();
            OffHeapLongArray.set(addr,Constants.OFFHEAP_CHUNK_INDEX_FLAGS,Constants.DIRTY_BIT);
        }

    }
}
