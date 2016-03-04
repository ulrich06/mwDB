package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.KChunk;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStringLongMap;
import org.mwdb.chunk.KStringLongMapCallBack;
import org.mwdb.chunk.heap.ArrayStringLongMap;
import org.mwdb.utility.PrimitiveHelper;

public class StringLongMapTest implements KChunkListener {

    private int dirtyCount = 0;

    @Test
    public void arrayHeapTest() {
        test(new ArrayStringLongMap(this));
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
            Assert.assertTrue(map.getValue("i_"+i) == i);
        }

    }

    @Override
    public void declareDirty(KChunk chunk) {
        dirtyCount++;
    }
}
