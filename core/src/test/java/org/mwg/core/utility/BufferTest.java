package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ludovicmouline on 28/04/16.
 */
public class BufferTest {
    private byte[] data = new byte[]{1,2,3,4,5};
    private byte[] data2 = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};


    public void testWriteAll(org.mwg.struct.Buffer buffer) {
        //write not initialized buffer
        buffer.writeAll(data);
        byte[] res = buffer.data();
        Assert.assertEquals(data.length,res.length);

        for(int i=0;i<res.length;i++) {
            Assert.assertEquals(data[i],res[i]);
        }


        //write more data than the capacity
        buffer.writeAll(data2);
        res = buffer.data();
        Assert.assertEquals(data.length + data2.length,res.length);
        for(int i=0;i<data.length;i++) {
            Assert.assertEquals(data[i],res[i]);
        }
        for(int i=0;i<data2.length;i++) {
            Assert.assertEquals("error for index " + i,data2[i],res[i + data.length]);
        }

        //write less data than the capacity
        buffer.writeAll(data);
        res = buffer.data();
        Assert.assertEquals(data.length + data2.length + data.length,res.length);
        for(int i=0;i<data.length;i++) {
            Assert.assertEquals(data[i],res[i]);
        }
        for(int i=0;i<data2.length;i++) {
            Assert.assertEquals("error for index " + i,data2[i],res[i + data.length]);
        }
        for(int i=0;i<data.length;i++) {
            Assert.assertEquals("error for index " + i,data[i],res[i + data.length + data2.length]);

        }

    }

    @Test
    public void testWriteAllHeap() {
        org.mwg.struct.Buffer buffer = Buffer.newHeapBuffer();
        testWriteAll(buffer);
    }

    @Test
    public void testWriteAllOffHeap() {
        org.mwg.struct.Buffer buffer = Buffer.newOffHeapBuffer();
        testWriteAll(buffer);
    }
}
