package org.mwg.utils;


import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class BytesIntConversionTest {

    @Test
    public void testIntToByte() {
        int[] dataSet = new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 24478795, -98421554};

        for(int i=0;i<dataSet.length;i++) {
            byte[] array = BytesIntConversion.toBytes(dataSet[i]);
            byte[] correct = ByteBuffer.allocate(4).putInt(dataSet[i]).array();
            Assert.assertArrayEquals(correct,array);
        }
    }

    @Test
    public void testByteToInt() {
        byte[][] dataSet = {{127, -1, -1, -1},
                {-128, 0, 0, 0},
                {1, 117, -124, 75},
                {-6, 34, 52, -50}};

        for(int i=0;i<dataSet.length;i++) {
            int integer = BytesIntConversion.toInt(dataSet[i]);
            int correct = ByteBuffer.wrap(dataSet[i]).getInt();

            Assert.assertEquals(correct,integer);
        }
    }
}
