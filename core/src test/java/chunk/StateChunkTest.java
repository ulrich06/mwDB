package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.KChunk;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStateChunk;
import org.mwdb.chunk.heap.HeapStateChunk;
import org.mwdb.utility.PrimitiveHelper;

public class StateChunkTest implements KChunkListener {

    @Test
    public void heapStateChunkTest() {
        test(new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, this), new HeapStateChunk(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, this));
    }

    private void test(KStateChunk chunk, KStateChunk chunk2) {
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

        //init chunk with primitives
        chunk.set(5, KType.LONG_ARRAY, new long[]{0, 1, 2, 3, 4});
        chunk.set(6, KType.DOUBLE_ARRAY, new double[]{0.1, 1.1, 2.1, 3.1, 4.1});
        chunk.set(7, KType.INT_ARRAY, new int[]{0, 1, 2, 3, 4});

        //TO REMOVE AFTER
        //chunk.set(6, KType.INT, 100);

        savedChunk = chunk.save();
        chunk2.load(savedChunk);
        savedChunk2 = chunk2.save();


        System.out.println(savedChunk);
        System.out.println(savedChunk2);

        Assert.assertTrue(PrimitiveHelper.equals(savedChunk, savedChunk2));

    }

    @Override
    public void declareDirty(KChunk chunk) {
        //TODO
    }
}
