package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.*;
import org.mwdb.chunk.heap.HeapStateChunk;
import org.mwdb.utility.PrimitiveHelper;

public class StateChunkTest implements KChunkListener {

    private int nbCount = 0;

    @Test
    public void heapStateChunkTest() {
        saveLoadTest(new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, this), new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, this));
        protectionTest(new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, this));
    }

    private void saveLoadTest(KStateChunk chunk, KStateChunk chunk2) {
        //reset nb count
        nbCount = 0;

        //init chunk with primitives
        chunk.set(0, KType.BOOL, true);
        chunk.set(1, KType.STRING, "hello");
        chunk.set(2, KType.DOUBLE, 1.0);
        chunk.set(3, KType.LONG, 1000l);//TODO check for solution for long cast
        chunk.set(4, KType.INT, 100);

        String savedChunk = chunk.save();
        chunk2.load(savedChunk);
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
        chunk2.load(savedChunk);
        savedChunk2 = chunk2.save();

        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk2));

        //init chunk with some maps
        KLongLongMap long2longMap = (KLongLongMap) chunk.init(8, KType.LONG_LONG_MAP);
        long2longMap.put(1, 1);
        long2longMap.put(Constants.END_OF_TIME, Constants.END_OF_TIME);
        long2longMap.put(Constants.BEGINNING_OF_TIME, Constants.BEGINNING_OF_TIME);

        KStringLongMap string2longMap = (KStringLongMap) chunk.init(9, KType.STRING_LONG_MAP);
        string2longMap.put("1", 1);
        string2longMap.put(Constants.END_OF_TIME + "", Constants.END_OF_TIME);
        string2longMap.put(Constants.BEGINNING_OF_TIME + "", Constants.BEGINNING_OF_TIME);

        savedChunk = chunk.save();
        chunk2.load(savedChunk);
        savedChunk2 = chunk2.save();

        //System.out.println(savedChunk);
        //System.out.println(savedChunk2);
        //System.out.println(nbCount);

        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk2));
        Assert.assertTrue(1 == nbCount);
    }

    private void protectionTest(KStateChunk chunk) {
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
        //TODO
        //protectionMethod(chunk, KType.LONG_LONG_ARRAY_MAP, "hello", true);

    }

    private void protectionMethod(KStateChunk chunk, int elemType, Object elem, boolean shouldCrash) {
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
    }
}
