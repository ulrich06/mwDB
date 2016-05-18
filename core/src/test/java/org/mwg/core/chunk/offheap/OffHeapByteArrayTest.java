package org.mwg.core.chunk.offheap;


import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @ignore ts
 */
public class OffHeapByteArrayTest {

    private int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    private int[] byteArraytoIntArray(byte[] bytes) {
        int[] toReturn = new int[bytes.length / 4];
        for(int i=0;i<toReturn.length;i++) {
            byte[] encodedInt = new byte[]{
                    bytes[4 * i + 3],
                    bytes[4 * i + 2],
                    bytes[4 * i + 1],
                    bytes[4 * i]
            };
            toReturn[i] = fromByteArray(encodedInt);
        }
        return toReturn;
    }


    private float fromByteArraytoFLoat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    private float[] byteArraytoFloatArray(byte[] bytes) {
        float[] toReturn = new float[bytes.length / 4];
        for(int i=0;i<toReturn.length;i++) {
            byte[] encodedInt = new byte[]{
                    bytes[4 * i + 3],
                    bytes[4 * i + 2],
                    bytes[4 * i + 1],
                    bytes[4 * i]
            };
            toReturn[i] = fromByteArraytoFLoat(encodedInt);
        }
        return toReturn;
    }




    @Test
    public void copyIntArrayTest() {
        int[] heapTable = new int[]{Integer.MAX_VALUE,Integer.MIN_VALUE,3,4,5};

        long offHeapTableAddr = OffHeapByteArray.allocate(heapTable.length * 4); //int on 4 bytes
        OffHeapByteArray.copyArray(heapTable,offHeapTableAddr,heapTable.length);

        byte[] data = new byte[heapTable.length * 4];
        for(int i=0;i<data.length;i++) {
            data[i] = OffHeapByteArray.get(offHeapTableAddr,i);
        }

        int[] res = byteArraytoIntArray(data);
        Assert.assertEquals(heapTable.length,res.length);
        for(int i=0;i<res.length;i++) {
            Assert.assertEquals(heapTable[i],res[i]);
        }


    }

    @Test
    public void copyFloatArrayTest() {
        float[] heapTable = new float[]{Float.MAX_VALUE,Float.MIN_VALUE,3.7f,4.2f,5.4f};

        long offHeapTableAddr = OffHeapByteArray.allocate(heapTable.length * 4); //float on 4 bytes
        OffHeapByteArray.copyArray(heapTable,offHeapTableAddr,heapTable.length);

        byte[] data = new byte[heapTable.length * 4];
        for(int i=0;i<data.length;i++) {
            data[i] = OffHeapByteArray.get(offHeapTableAddr,i);
        }

        float[] res = byteArraytoFloatArray(data);
        Assert.assertEquals(heapTable.length,res.length);
        for(int i=0;i<res.length;i++) {
            Assert.assertEquals(heapTable[i],res[i],0);
        }


    }

    @Test
    public void copyByteArrayTest() {
        byte[] byteTable = new byte[]{127,127,8,9};

        long offHeapTableAddr = OffHeapByteArray.allocate(byteTable.length); //byte on 1 bytes
        OffHeapByteArray.copyArray(byteTable,offHeapTableAddr,byteTable.length);

        byte[] data = new byte[byteTable.length];
        for(int i=0;i<data.length;i++) {
            data[i] = OffHeapByteArray.get(offHeapTableAddr,i);
        }

        Assert.assertEquals(byteTable.length,data.length);
        for(int i=0;i<data.length;i++) {
            Assert.assertEquals(byteTable[i],data[i],0.1);
        }
    }
}
