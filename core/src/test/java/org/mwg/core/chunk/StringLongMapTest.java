package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.Constants;
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.DataHasher;
import org.mwg.plugin.Chunk;
import org.mwg.struct.StringLongMap;
import org.mwg.struct.StringLongMapCallBack;
import org.mwg.core.chunk.heap.ArrayStringLongMap;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

public class StringLongMapTest implements ChunkListener {

    private int dirtyCount = 0;

    @Test
    public void arrayHeapTest() {
        test(new ArrayStringLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, null));
    }

    /**
     * @ignore ts
     */
    @Test
    public void arrayOffHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        org.mwg.core.chunk.offheap.ArrayStringLongMap map = new org.mwg.core.chunk.offheap.ArrayStringLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, -1);
        org.mwg.core.chunk.offheap.ArrayStringLongMap.incrementCopyOnWriteCounter(map.rootAddress());
        test(map);
        org.mwg.core.chunk.offheap.ArrayStringLongMap.free(map.rootAddress());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    /*
    @Test
    public void bigTest() {
        //StringLongMap map = new org.mwg.core.chunk.heap.ArrayStringLongMap(this, 1000000);
        StringLongMap map = new org.mwg.core.chunk.offheap.ArrayStringLongMap(this, 100000000, -1);
        // LongLongMap map = new org.mwg.core.chunk.offheap.ArrayLongLongMap(this, 100000000);
        //    LongLongMap map = new org.mwg.core.chunk.heap.ArrayLongLongMap(this, 10000000);

        long previous = System.currentTimeMillis();
        for (long i = 0; i < 100000000; i++) {
            if (i % 1000000 == 0) {
                System.out.println(i);
            }
            String toInsert = "hello_" + i;
            map.put(toInsert, i);
            Assert.assertTrue(PrimitiveHelper.equals(toInsert, map.getKey(i)));
            Assert.assertTrue(i == map.getValue(toInsert));
        }
        long after = System.currentTimeMillis();
        System.out.println((after - previous));
    }*/

    private void test(StringLongMap map) {
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
        Assert.assertTrue(Constants.NULL_LONG == map.getValue("DictionaryUsage"));

        Assert.assertTrue(PrimitiveHelper.equals("Hello", map.getByHash(DataHasher.hash("Hello"))));

        Assert.assertTrue(dirtyCount == 5);

        final String[] keys = new String[3];
        final long[] values = new long[3];
        final int[] resIndex = {0};
        map.each(new StringLongMapCallBack() {
            @Override
            public void on(final String key, final long value) {
                keys[resIndex[0]] = key;
                values[resIndex[0]] = value;
                resIndex[0]++;
            }
        });
        Assert.assertTrue(1 == values[0]);
        Assert.assertTrue(2 == values[1]);
        Assert.assertTrue(Constants.NULL_LONG == values[2]);
        Assert.assertTrue(PrimitiveHelper.equals("Hello", keys[0]));
        Assert.assertTrue(PrimitiveHelper.equals("Hello1", keys[1]));
        Assert.assertTrue(PrimitiveHelper.equals("DictionaryUsage", keys[2]));

        //force the graph to do a rehash capacity
        for (int i = 0; i < CoreConstants.MAP_INITIAL_CAPACITY; i++) {
            map.put("i_" + i, i);
        }
        //test that all values are consistent
        for (int i = 0; i < CoreConstants.MAP_INITIAL_CAPACITY; i++) {
            Assert.assertTrue(map.getValue("i_" + i) == i);
        }
    }

    @Override
    public void declareDirty(Chunk chunk) {
        dirtyCount++;
    }

    @Override
    public Graph graph() {
        return null;
    }
}
