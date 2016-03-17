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
import org.mwdb.chunk.offheap.OffHeapStateChunk;
import org.mwdb.plugin.KStorage;
import org.mwdb.utility.Buffer;
import org.mwdb.utility.PrimitiveHelper;

public class StateChunkTest implements KChunkListener {

    private int nbCount = 0;

    public interface StateChunkFactory {
        KStateChunk create(KChunkListener listener, KStorage.KBuffer payload, KChunk origin);
    }

    @Test
    public void heapStateChunkTest() {
        StateChunkFactory factory = new StateChunkFactory() {

            @Override
            public KStateChunk create(KChunkListener listener, KStorage.KBuffer payload, KChunk origin) {
                return new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, listener, payload, origin);
            }
        };
        saveLoadTest(factory);
        protectionTest(factory);
        typeSwitchTest(factory);
        cloneTest(factory);
    }


    @Test
    public void offHeapStateChunkTest() {
        StateChunkFactory factory = new StateChunkFactory() {

            @Override
            public KStateChunk create(KChunkListener listener, KStorage.KBuffer payload, KChunk origin) {
                return new OffHeapStateChunk(listener, Constants.OFFHEAP_NULL_PTR, payload, origin);
            }
        };
        saveLoadTest(factory);
        protectionTest(factory);
        typeSwitchTest(factory);
        cloneTest(factory);
    }

    private boolean compareBuffers(KStorage.KBuffer buffer, KStorage.KBuffer buffer2) {
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

        KStorage.KBuffer buffer = Buffer.newHeapBuffer();
        chunk.save(buffer);
        KStateChunk chunk2 = factory.create(this, buffer, null);
        KStorage.KBuffer buffer2 = Buffer.newHeapBuffer();
        chunk2.save(buffer2);

        Assert.assertTrue(compareBuffers(buffer, buffer2));

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

        buffer.free();
        buffer = Buffer.newHeapBuffer();

        chunk.save(buffer);
        free(chunk2);
        chunk2 = factory.create(this, buffer, null);

        buffer2.free();
        buffer2 = Buffer.newHeapBuffer();
        chunk2.save(buffer2);

        Assert.assertTrue(compareBuffers(buffer, buffer2));

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

        buffer.free();
        buffer = Buffer.newHeapBuffer();
        chunk.save(buffer);
        free(chunk2);
        chunk2 = factory.create(this, buffer, null);

        buffer2.free();
        buffer2 = Buffer.newHeapBuffer();
        chunk2.save(buffer2);

        Assert.assertTrue(((KStringLongMap) chunk2.get(9)).size() == 3);
        Assert.assertTrue(((KLongLongMap) chunk2.get(8)).size() == 3);
        Assert.assertTrue(((KLongLongArrayMap) chunk2.get(10)).size() == 4);

        Assert.assertTrue(compareBuffers(buffer, buffer2));
        Assert.assertTrue(1 == nbCount);

        //force reHash
        for (int i = 0; i < 10; i++) {
            chunk.set(1000 + i, KType.INT, i);
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(chunk.get(1000 + i).equals(i));
        }

        KStateChunk chunk3 = factory.create(this, null, chunk);
        KStorage.KBuffer buffer3 = Buffer.newHeapBuffer();
        chunk3.save(buffer3);

        buffer.free();
        buffer = Buffer.newHeapBuffer();
        chunk.save(buffer);
        Assert.assertTrue(compareBuffers(buffer, buffer3));

        buffer3.free();
        buffer2.free();
        buffer.free();

        free(chunk3);
        free(chunk2);
        free(chunk);

    }

    private void free(KStateChunk chunk) {
        if (chunk instanceof KOffHeapChunk) {
            OffHeapStateChunk.free(((KOffHeapChunk) chunk).addr());
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

    private void typeSwitchTest(StateChunkFactory factory) {
        KStateChunk chunk = factory.create(this, null, null);

        //init primitives
        chunk.set(0, KType.BOOL, true);
        chunk.set(1, KType.STRING, "hello");
        chunk.set(2, KType.LONG, 1000l);
        chunk.set(3, KType.INT, 100);
        chunk.set(4, KType.DOUBLE, 1.0);
        //init arrays
        chunk.set(5, KType.DOUBLE_ARRAY, new double[]{1.0, 2.0, 3.0});
        chunk.set(6, KType.LONG_ARRAY, new long[]{1, 2, 3});
        chunk.set(7, KType.INT_ARRAY, new int[]{1, 2, 3});
        //init maps
        ((KLongLongMap) chunk.getOrCreate(8, KType.LONG_LONG_MAP)).put(100, 100);
        ((KLongLongArrayMap) chunk.getOrCreate(9, KType.LONG_LONG_ARRAY_MAP)).put(100, 100);
        ((KStringLongMap) chunk.getOrCreate(10, KType.STRING_LONG_MAP)).put("100", 100);

        //ok now switch all types

        //switch primitives
        chunk.set(10, KType.BOOL, true);
        Assert.assertTrue(chunk.getType(10) == KType.BOOL);
        Assert.assertTrue((boolean) chunk.get(10));

        chunk.set(0, KType.STRING, "hello");
        Assert.assertTrue(chunk.getType(0) == KType.STRING);
        Assert.assertTrue(PrimitiveHelper.equals(chunk.get(0).toString(), "hello"));

        chunk.set(1, KType.LONG, 1000l);
        Assert.assertTrue(chunk.getType(1) == KType.LONG);
        Assert.assertTrue((long) chunk.get(1) == 1000l);

        chunk.set(2, KType.INT, 100);
        Assert.assertTrue(chunk.getType(2) == KType.INT);
        Assert.assertTrue((int) chunk.get(2) == 100);

        chunk.set(3, KType.DOUBLE, 1.0);
        Assert.assertTrue(chunk.getType(3) == KType.DOUBLE);
        Assert.assertTrue((double) chunk.get(3) == 1.0);

        //switch arrays
        chunk.set(4, KType.DOUBLE_ARRAY, new double[]{1.0, 2.0, 3.0});
        Assert.assertTrue(chunk.getType(4) == KType.DOUBLE_ARRAY);
        Assert.assertTrue(((double[]) chunk.get(4))[0] == 1.0);

        chunk.set(5, KType.LONG_ARRAY, new long[]{1, 2, 3});
        Assert.assertTrue(chunk.getType(5) == KType.LONG_ARRAY);
        Assert.assertTrue(((long[]) chunk.get(5))[0] == 1);

        chunk.set(6, KType.INT_ARRAY, new int[]{1, 2, 3});
        Assert.assertTrue(chunk.getType(6) == KType.INT_ARRAY);
        Assert.assertTrue(((int[]) chunk.get(6))[0] == 1);

        //switch maps
        ((KLongLongMap) chunk.getOrCreate(7, KType.LONG_LONG_MAP)).put(100, 100);
        ((KLongLongArrayMap) chunk.getOrCreate(8, KType.LONG_LONG_ARRAY_MAP)).put(100, 100);
        ((KStringLongMap) chunk.getOrCreate(9, KType.STRING_LONG_MAP)).put("100", 100);

        free(chunk);
    }

    private void cloneTest(StateChunkFactory factory) {
        KStateChunk chunk = factory.create(this, null, null);

        //init primitives
        chunk.set(0, KType.BOOL, true);
        chunk.set(1, KType.STRING, "hello");
        chunk.set(2, KType.LONG, 1000l);
        chunk.set(3, KType.INT, 100);
        chunk.set(4, KType.DOUBLE, 1.0);
        //init arrays
        chunk.set(5, KType.DOUBLE_ARRAY, new double[]{1.0, 2.0, 3.0});
        chunk.set(6, KType.LONG_ARRAY, new long[]{1, 2, 3});
        chunk.set(7, KType.INT_ARRAY, new int[]{1, 2, 3});
        //init maps
        ((KLongLongMap) chunk.getOrCreate(8, KType.LONG_LONG_MAP)).put(100, 100);
        ((KLongLongArrayMap) chunk.getOrCreate(9, KType.LONG_LONG_ARRAY_MAP)).put(100, 100);
        ((KStringLongMap) chunk.getOrCreate(10, KType.STRING_LONG_MAP)).put("100", 100);

        //clone the chunk
        KStateChunk chunk2 = factory.create(this, null, chunk);

        //test primitives
        Assert.assertTrue(chunk2.getType(0) == KType.BOOL);
        Assert.assertTrue((boolean) chunk.get(0));

        Assert.assertTrue(chunk2.getType(1) == KType.STRING);
        Assert.assertTrue(PrimitiveHelper.equals(chunk2.get(1).toString(), "hello"));

        Assert.assertTrue(chunk2.getType(2) == KType.LONG);
        Assert.assertTrue((long) chunk2.get(2) == 1000l);

        Assert.assertTrue(chunk2.getType(3) == KType.INT);
        Assert.assertTrue((int) chunk2.get(3) == 100);

        Assert.assertTrue(chunk2.getType(4) == KType.DOUBLE);
        Assert.assertTrue((double) chunk2.get(4) == 1.0);

        //test arrays
        Assert.assertTrue(chunk2.getType(5) == KType.DOUBLE_ARRAY);
        Assert.assertTrue(((double[]) chunk2.get(5))[0] == 1.0);

        Assert.assertTrue(chunk2.getType(6) == KType.LONG_ARRAY);
        Assert.assertTrue(((long[]) chunk2.get(6))[0] == 1);

        Assert.assertTrue(chunk2.getType(7) == KType.INT_ARRAY);
        Assert.assertTrue(((int[]) chunk2.get(7))[0] == 1);

        //test maps
        Assert.assertTrue(((KLongLongMap) chunk2.get(8)).get(100) == 100);
        Assert.assertTrue(((KLongLongArrayMap) chunk2.get(9)).get(100)[0] == 100);
        Assert.assertTrue(((KStringLongMap) chunk2.get(10)).getValue("100") == 100);

        //now we test the co-evolution of clone

        //STRINGS
        chunk.set(1, KType.STRING, "helloPast");
        Assert.assertTrue(PrimitiveHelper.equals(chunk.get(1).toString(), "helloPast"));
        Assert.assertTrue(PrimitiveHelper.equals(chunk2.get(1).toString(), "hello"));

        chunk2.set(1, KType.STRING, "helloFuture");
        Assert.assertTrue(PrimitiveHelper.equals(chunk2.get(1).toString(), "helloFuture"));
        Assert.assertTrue(PrimitiveHelper.equals(chunk.get(1).toString(), "helloPast"));

        //ARRAYS
        chunk2.set(5, KType.DOUBLE_ARRAY, new double[]{3.0, 4.0, 5.0});
        Assert.assertTrue(((double[]) chunk2.get(5))[0] == 3.0);
        Assert.assertTrue(((double[]) chunk.get(5))[0] == 1.0);

        chunk2.set(6, KType.LONG_ARRAY, new long[]{100, 200, 300});
        Assert.assertTrue(((long[]) chunk2.get(6))[0] == 100);
        Assert.assertTrue(((long[]) chunk.get(6))[0] == 1);

        chunk2.set(7, KType.INT_ARRAY, new int[]{100, 200, 300});
        Assert.assertTrue(((int[]) chunk2.get(7))[0] == 100);
        Assert.assertTrue(((int[]) chunk.get(7))[0] == 1);

        //MAPS
        ((KLongLongMap) chunk2.get(8)).put(100, 200);
        Assert.assertTrue(((KLongLongMap) chunk2.get(8)).get(100) == 200);
        Assert.assertTrue(((KLongLongMap) chunk.get(8)).get(100) == 100);

        ((KLongLongArrayMap) chunk2.get(9)).put(100, 200);
        Assert.assertTrue(((KLongLongArrayMap) chunk2.get(9)).get(100)[0] == 200);
        Assert.assertTrue(((KLongLongArrayMap) chunk2.get(9)).get(100)[1] == 100);
        Assert.assertTrue(((KLongLongArrayMap) chunk.get(9)).get(100)[0] == 100);

        ((KStringLongMap) chunk2.get(10)).put("100", 200);
        Assert.assertTrue(((KStringLongMap) chunk2.get(10)).getValue("100") == 200);
        Assert.assertTrue(((KStringLongMap) chunk.get(10)).getValue("100") == 100);

        free(chunk);
        free(chunk2);
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
        if (chunk instanceof KHeapChunk) {
            ((KHeapChunk) chunk).setFlags(Constants.DIRTY_BIT, 0);
        } else if (chunk instanceof KOffHeapChunk) {
            long addr = ((KOffHeapChunk) chunk).addr();
            OffHeapLongArray.set(addr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS, Constants.DIRTY_BIT);
        }

    }
}
