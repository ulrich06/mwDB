package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.*;
import org.mwdb.chunk.heap.ArrayStringLongMap;
import org.mwdb.chunk.offheap.KOffHeapStateChunkElem;
import org.mwdb.utility.PrimitiveHelper;

public class StringLongMapTest implements KChunkListener {

    private int dirtyCount = 0;


    @Test
    public void arrayHeapTest() {
        test(new ArrayStringLongMap(this, Constants.MAP_INITIAL_CAPACITY));
    }

    @Test
    public void arrayOffHeapTest() {
      //  test(new org.mwdb.chunk.offheap.ArrayStringLongMap(this, Constants.MAP_INITIAL_CAPACITY, -1));
    }


   // @Test
    public void bigTest() {
        //   KStringLongMap map = new org.mwdb.chunk.heap.ArrayStringLongMap(this, 10_000_000);
           KStringLongMap map = new org.mwdb.chunk.offheap.ArrayStringLongMap(this, 100_000_000, -1);
       // KLongLongMap map = new org.mwdb.chunk.offheap.ArrayLongLongMap(this, 100_000_000);
        //    KLongLongMap map = new org.mwdb.chunk.heap.ArrayLongLongMap(this, 10_000_000);

        long previous = System.currentTimeMillis();
        for (long i = 0; i < 100_000_000; i++) {
            if (i % 1_000_000 == 0) {
                System.out.println(i);
            }
              String toInsert = "hello_" + i;
             map.put(toInsert, i);
            //map.put(i, i);
        }
        long after = System.currentTimeMillis();
        System.out.println((after - previous));

    }

    private void test(KStringLongMap map) {
        dirtyCount = 0;
        map.put("Hello", 0);
        Assert.assertTrue(0 == map.getValue("Hello"));
        map.put("Hello1", 1);
        Assert.assertTrue(0 == map.getValue("Hello"));
        Assert.assertTrue(1 == map.getValue("Hello1"));
        //no effect
        map.put("Hello1", 1);

        map.put("Hello", 1);
        map.put("Hello1", 2);
        Assert.assertTrue(1 == map.getValue("Hello"));
        Assert.assertTrue(2 == map.getValue("Hello1"));

        map.put("DictionaryUsage", Constants.NULL_LONG);
        Assert.assertTrue(2 == map.getValue("DictionaryUsage"));

        Assert.assertTrue(PrimitiveHelper.equals("Hello", map.getKey(0)));

        Assert.assertTrue(dirtyCount == 5);

        String[] keys = new String[3];
        long[] values = new long[3];
        final int[] resIndex = {0};
        map.each(new KStringLongMapCallBack() {
            @Override
            public void on(String key, long value) {
                keys[resIndex[0]] = key;
                values[resIndex[0]] = value;
                resIndex[0]++;
            }
        });
        Assert.assertTrue(1 == values[0]);
        Assert.assertTrue(2 == values[1]);
        Assert.assertTrue(2 == values[2]);
        Assert.assertTrue(PrimitiveHelper.equals("Hello", keys[0]));
        Assert.assertTrue(PrimitiveHelper.equals("Hello1", keys[1]));
        Assert.assertTrue(PrimitiveHelper.equals("DictionaryUsage", keys[2]));

        //force the graph to do a rehash capacity
        for (int i = 0; i < Constants.MAP_INITIAL_CAPACITY; i++) {
            map.put("i_" + i, i);
        }

        //test that all values are consistent
        for (int i = 0; i < Constants.MAP_INITIAL_CAPACITY; i++) {
            Assert.assertTrue(map.getValue("i_" + i) == i);
        }

        free(map);
    }

    /**
     * @native ts
     */
    private void free(KStringLongMap map) {
        if (map instanceof KOffHeapStateChunkElem) {
            ((KOffHeapStateChunkElem) map).free();
        }
    }

    @Override
    public void declareDirty(KChunk chunk) {
        dirtyCount++;
    }
}
