package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.struct.LongLongMap;
import org.mwg.core.Constants;
import org.mwg.Graph;
import org.mwg.struct.LongLongMapCallBack;
import org.mwg.core.chunk.heap.ArrayLongLongMap;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.utility.Unsafe;

public class LongLongMapTest implements KChunkListener {

    private int dirtyCount = 0;

    @Test
    public void arrayHeapTest() {
        test(new ArrayLongLongMap(this, Constants.MAP_INITIAL_CAPACITY, null));
    }

    @Test
    public void arrayOffHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        org.mwg.core.chunk.offheap.ArrayLongLongMap map = new org.mwg.core.chunk.offheap.ArrayLongLongMap(this, Constants.MAP_INITIAL_CAPACITY,-1);
        org.mwg.core.chunk.offheap.ArrayLongLongMap.incrementCopyOnWriteCounter(map.rootAddress());
        test(map);
        org.mwg.core.chunk.offheap.ArrayLongLongMap.free(map.rootAddress());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(LongLongMap map) {
        dirtyCount = 0;
        map.put(0, 0);
        Assert.assertTrue(0 == map.get(0));
        map.put(1, 1);
        Assert.assertTrue(0 == map.get(0));
        Assert.assertTrue(1 == map.get(1));
        //no effect
        map.put(1, 1);

        map.put(0, 1);
        map.put(1, 2);
        Assert.assertTrue(1 == map.get(0));
        Assert.assertTrue(2 == map.get(1));

        map.put(2, Constants.NULL_LONG);
        Assert.assertTrue(2 == map.get(2));

        Assert.assertTrue(dirtyCount == 5);

        long[] keys = new long[3];
        long[] values = new long[3];
        final int[] resIndex = {0};
        map.each(new LongLongMapCallBack() {
            @Override
            public void on(long key, long value) {
                keys[resIndex[0]] = key;
                values[resIndex[0]] = value;
                resIndex[0]++;
            }
        });
        Assert.assertTrue(1 == values[0]);
        Assert.assertTrue(2 == values[1]);
        Assert.assertTrue(2 == values[2]);
        Assert.assertTrue(0 == keys[0]);
        Assert.assertTrue(1 == keys[1]);
        Assert.assertTrue(2 == keys[2]);

        //force the graph to do a rehash capacity
        for (int i = 0; i < Constants.MAP_INITIAL_CAPACITY; i++) {
            map.put(i, i);
        }
        //test that all values are consistent
        for (int i = 0; i < Constants.MAP_INITIAL_CAPACITY; i++) {
            Assert.assertTrue(map.get(i) == i);
        }
    }

    @Override
    public void declareDirty(KChunk chunk) {
        dirtyCount++;
    }

    @Override
    public Graph graph() {
        return null;
    }
}
