package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.*;

public class LongLongArrayMapTest implements KChunkListener {

    private int dirtyCount = 0;

    @Test
    public void arrayHeapTest() {
        test(new org.mwdb.chunk.heap.ArrayLongLongArrayMap(this, Constants.MAP_INITIAL_CAPACITY));
    }

    @Test
    public void arrayOffHeapTest() {
        test(new org.mwdb.chunk.offheap.ArrayLongLongArrayMap(this, Constants.MAP_INITIAL_CAPACITY, -1));
    }


    private void test(KLongLongArrayMap map) {
        dirtyCount = 0;

        map.put(10, 10);
        Assert.assertTrue(map.size() == 1);
        Assert.assertTrue(map.get(10).length == 1);
        Assert.assertTrue(map.get(10)[0] == 10);

        map.put(10, 100);
        Assert.assertTrue(map.size() == 2);
        Assert.assertTrue(map.get(10).length == 2);
        Assert.assertTrue(map.get(10)[0] == 100);
        Assert.assertTrue(map.get(10)[1] == 10);


        map.put(10, 100);
        Assert.assertTrue(map.size() == 2);
        Assert.assertTrue(map.get(10).length == 2);
        

        //force reHash
        for (int i = 0; i < Constants.MAP_INITIAL_CAPACITY; i++) {
            map.put(Constants.BEGINNING_OF_TIME, i);
        }
        Assert.assertTrue(map.size() == Constants.MAP_INITIAL_CAPACITY + 2);

        long[] getRes = map.get(Constants.BEGINNING_OF_TIME);
        Assert.assertTrue(getRes.length == Constants.MAP_INITIAL_CAPACITY);
        for (int i = 0; i < Constants.MAP_INITIAL_CAPACITY; i++) {
            Assert.assertTrue(getRes[i] == (Constants.MAP_INITIAL_CAPACITY - i - 1));
        }

    }

    @Override
    public void declareDirty(KChunk chunk) {
        dirtyCount++;
    }
}
