package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Graph;
import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.Chunk;
import org.mwg.struct.LongLongArrayMap;


public class LongLongArrayMapTest implements ChunkListener {

    private int dirtyCount = 0;

    @Test
    public void arrayHeapTest() {
        test(new org.mwg.core.chunk.heap.ArrayLongLongArrayMap(this, CoreConstants.MAP_INITIAL_CAPACITY, null));
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

        org.mwg.core.chunk.offheap.ArrayLongLongArrayMap map = new org.mwg.core.chunk.offheap.ArrayLongLongArrayMap(this, CoreConstants.MAP_INITIAL_CAPACITY, -1);
        org.mwg.core.chunk.offheap.ArrayLongLongArrayMap.incrementCopyOnWriteCounter(map.rootAddress());
        test(map);
        org.mwg.core.chunk.offheap.ArrayLongLongArrayMap.free(map.rootAddress());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(LongLongArrayMap map) {

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
        for (int i = 0; i < CoreConstants.MAP_INITIAL_CAPACITY; i++) {
            map.put(CoreConstants.BEGINNING_OF_TIME, i);
        }
        Assert.assertTrue(map.size() == CoreConstants.MAP_INITIAL_CAPACITY + 2);

        long[] getRes = map.get(CoreConstants.BEGINNING_OF_TIME);
        Assert.assertTrue(getRes.length == CoreConstants.MAP_INITIAL_CAPACITY);
        for (int i = 0; i < CoreConstants.MAP_INITIAL_CAPACITY; i++) {
            Assert.assertTrue(getRes[i] == (CoreConstants.MAP_INITIAL_CAPACITY - i - 1));
        }

        //test previous to reHash
        Assert.assertTrue(map.get(10).length == 2);
        Assert.assertTrue(map.get(10)[0] == 100);
        Assert.assertTrue(map.get(10)[1] == 10);

        //make a remove call
        map.remove(10, 10);
        Assert.assertTrue(map.size() == CoreConstants.MAP_INITIAL_CAPACITY + 2 - 1);
        Assert.assertTrue(map.get(10).length == 1);

        map.remove(CoreConstants.BEGINNING_OF_TIME, 0);
        Assert.assertTrue(map.size() == CoreConstants.MAP_INITIAL_CAPACITY + 2 - 2);
        getRes = map.get(CoreConstants.BEGINNING_OF_TIME);
        Assert.assertTrue(getRes.length == CoreConstants.MAP_INITIAL_CAPACITY - 1);
        for (int i = 1; i < CoreConstants.MAP_INITIAL_CAPACITY; i++) {
            Assert.assertTrue(getRes[i - 1] == (CoreConstants.MAP_INITIAL_CAPACITY - i));
        }

        Assert.assertTrue(dirtyCount == 18);
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
