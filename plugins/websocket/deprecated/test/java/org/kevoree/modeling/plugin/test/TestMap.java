package org.kevoree.modeling.plugin.test;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.math.Array3LongIntMap;
import org.mwg.math.K3LongIntMap;
import org.mwg.math.K3LongMapCallBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by ludovicmouline on 09/02/16.
 */
public class TestMap {
    private static final int MAP_CAPACITY = 10;
    private static final float MAP_LOAD_FACTOR = 0.75f;

    @Test
    public void simpleTestArray3LongIntMap() {
        K3LongIntMap map = new Array3LongIntMap(MAP_CAPACITY,MAP_LOAD_FACTOR);

        Assert.assertEquals(false,map.contains(0,0,0));

        map.put(5,4,3,2);
        Assert.assertEquals(2,map.get(5,4,3));
        Assert.assertEquals(1,map.size());

        map.put(5,4,3,8);
        Assert.assertEquals(8,map.get(5,4,3));
        Assert.assertEquals(1,map.size());

        map.put(9,8,7,6);
        Assert.assertEquals(2,map.size());
        map.remove(9,8,7);
        Assert.assertEquals(false,map.contains(9,8,7));
        Assert.assertEquals(1,map.size());

        map.clear();
        Assert.assertEquals(0,map.size());
        Assert.assertEquals(false,map.contains(5,4,3));
    }

    @Test
    public void testArray3LongMap() {
        K3LongIntMap map = new Array3LongIntMap(MAP_CAPACITY,MAP_LOAD_FACTOR);
        Random rand = new Random();
        final int NB_VALUES = MAP_CAPACITY * 2;

        List<Integer> values = new ArrayList<>(NB_VALUES);
        List<Long> keys =new ArrayList<>(NB_VALUES * 3);

        for(int i=0;i<NB_VALUES;i++){
            values.add(i,rand.nextInt(10000));
            keys.add(i,rand.nextLong() & Long.MAX_VALUE); //force positive value
            keys.add(i + 1,rand.nextLong() & Long.MAX_VALUE); //force positive value
            keys.add(i + 2,rand.nextLong() & Long.MAX_VALUE); //force positive value
        }

        System.out.println("Test with values: " + Arrays.toString(values.toArray()));
        System.out.println("Test with keys: " + Arrays.toString(keys.toArray()));

        for(int i=0;i<NB_VALUES;i++){
            map.put(keys.get(i * 3),keys.get(3 * i +1),keys.get(3 * i + 2),values.get(i));
        }

        Assert.assertEquals(NB_VALUES,map.size());
        map.each(new K3LongMapCallBack() {
            @Override
            public void on(long universe, long time, long uuid, int value) {
                Assert.assertEquals(true,values.contains(value));
                int index = values.lastIndexOf(value);
                Assert.assertEquals((long)keys.get(index * 3),universe);
                Assert.assertEquals((long)keys.get(index * 3 + 1),time);
                Assert.assertEquals((long)keys.get(index * 3 + 2),uuid);
            }
        });
    }
}
