package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.core.CoreConstants;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

import static org.mwg.Constants.BUFFER_SEP;

public class BufferBuilderTest {
    private byte[] data = new byte[]{1, 2, 3, 4, 5};
    private byte[] data2 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};


    private void testWriteAll(org.mwg.struct.Buffer buffer) {
        //write not initialized buffer
        buffer.writeAll(data);
        byte[] res = buffer.data();
        Assert.assertEquals(data.length, res.length);

        for (int i = 0; i < res.length; i++) {
            Assert.assertEquals(data[i], res[i]);
        }


        //write more data than the capacity
        buffer.writeAll(data2);
        res = buffer.data();
        Assert.assertEquals(data.length + data2.length, res.length);
        for (int i = 0; i < data.length; i++) {
            Assert.assertEquals(data[i], res[i]);
        }
        for (int i = 0; i < data2.length; i++) {
            Assert.assertEquals("error for index " + i, data2[i], res[i + data.length]);
        }

        //write less data than the capacity
        buffer.writeAll(data);
        res = buffer.data();
        Assert.assertEquals(data.length + data2.length + data.length, res.length);
        for (int i = 0; i < data.length; i++) {
            Assert.assertEquals(data[i], res[i]);
        }
        for (int i = 0; i < data2.length; i++) {
            Assert.assertEquals("error for index " + i, data2[i], res[i + data.length]);
        }
        for (int i = 0; i < data.length; i++) {
            Assert.assertEquals("error for index " + i, data[i], res[i + data.length + data2.length]);

        }

    }

    private void testIterator(org.mwg.struct.Buffer buffer) {
        buffer.writeAll("123".getBytes());
        buffer.write(BUFFER_SEP);
        buffer.writeAll("567".getBytes());
        buffer.write(BUFFER_SEP);
        buffer.writeAll("89".getBytes());

        BufferIterator it = buffer.iterator();
        Assert.assertEquals(it.hasNext(), true);

        Buffer view1 = it.next();
        Assert.assertNotNull(view1);
        Assert.assertEquals(view1.size(), 3);
        byte[] view1flat = view1.data();
        view1flat[0] = "123".getBytes()[0];
        view1flat[1] = "123".getBytes()[1];
        view1flat[2] = "123".getBytes()[2];
        Assert.assertEquals(it.hasNext(), true);

        Buffer view2 = it.next();
        Assert.assertNotNull(view2);
        Assert.assertEquals(view2.size(), 3);
        byte[] view2flat = view2.data();
        view2flat[0] = "567".getBytes()[0];
        view2flat[1] = "567".getBytes()[1];
        view2flat[2] = "567".getBytes()[2];
        Assert.assertEquals(it.hasNext(), true);

        Buffer view3 = it.next();
        Assert.assertNotNull(view3);
        Assert.assertEquals(view3.size(), 2);
        byte[] view3flat = view3.data();
        view3flat[0] = "89".getBytes()[0];
        view3flat[1] = "89".getBytes()[1];
        Assert.assertEquals(it.hasNext(), false);
    }

    private void testIteratorNull(org.mwg.struct.Buffer buffer) {
        buffer.write(BUFFER_SEP);
        buffer.write(BUFFER_SEP);

        BufferIterator it = buffer.iterator();
        Assert.assertTrue(it.hasNext());

        Buffer view = it.next();
        Assert.assertNotNull(view);
        Assert.assertEquals(view.size(), 0);

        Buffer view2 = it.next();
        Assert.assertNotNull(view2);
        Assert.assertEquals(view2.size(), 0);

        Buffer view3 = it.next();
        Assert.assertNotNull(view3);
        Assert.assertEquals(view3.size(), 0);

        Buffer view4 = it.next();
        Assert.assertNull(view4);

    }


    @Test
    public void testWriteAllHeap() {
        org.mwg.struct.Buffer buffer = BufferBuilder.newHeapBuffer();
        testWriteAll(buffer);
    }

    @Test
    public void testWriteAllOffHeap() {
        org.mwg.struct.Buffer buffer = BufferBuilder.newOffHeapBuffer();
        testWriteAll(buffer);
    }

    @Test
    public void testIteratorHeap() {
        org.mwg.struct.Buffer buffer = BufferBuilder.newHeapBuffer();
        testIterator(buffer);
        buffer.free();
        buffer = BufferBuilder.newHeapBuffer();
        testIteratorNull(buffer);
        buffer.free();
    }

    @Test
    public void testIteratorOffHeap() {
        org.mwg.struct.Buffer buffer = BufferBuilder.newOffHeapBuffer();
        testIterator(buffer);
        buffer.free();
        buffer = BufferBuilder.newOffHeapBuffer();
        testIteratorNull(buffer);
        buffer.free();
    }

    //@Test
    public void testIteratorHeap2(){
        byte[] bytes = new byte[]{12,11, BUFFER_SEP,87, BUFFER_SEP,87,45};
        Buffer buffer = BufferBuilder.newHeapBuffer();
        buffer.writeAll(bytes);
        BufferIterator it = buffer.iterator();

        Assert.assertArrayEquals(new byte[]{12,11},it.next().data());
        Assert.assertArrayEquals(new byte[]{87},it.next().data());
        Assert.assertArrayEquals(new byte[]{12,45},it.next().data());

        Assert.assertEquals(false,it.hasNext());
    }

    //@Test
    public void testOneElementBuffer() {
        byte[] bytesOneElementBuffer = new byte[] {15,CoreConstants.BUFFER_SEP,16,CoreConstants.BUFFER_SEP,17};


        Buffer oneElementBuffer = BufferBuilder.newHeapBuffer();
        oneElementBuffer.writeAll(bytesOneElementBuffer);

        BufferIterator itOneElementBuffer = oneElementBuffer.iterator();

        Assert.assertArrayEquals(new byte[]{15}, itOneElementBuffer.next().data());
        Assert.assertArrayEquals(new byte[]{16}, itOneElementBuffer.next().data());
        Assert.assertArrayEquals(new byte[]{17}, itOneElementBuffer.next().data());

        Assert.assertEquals(false,itOneElementBuffer.hasNext());

    }

    //@Test
    public void testEmptyBuffer() {
        byte[] bytes = new byte[] {BUFFER_SEP, BUFFER_SEP, BUFFER_SEP,15,BUFFER_SEP,BUFFER_SEP};
        Buffer buffer = BufferBuilder.newHeapBuffer();
        buffer.writeAll(bytes);

        BufferIterator it = buffer.iterator();

        Assert.assertArrayEquals(null,it.next().data());
        Assert.assertArrayEquals(null,it.next().data());
        Assert.assertArrayEquals(null,it.next().data());
        Assert.assertArrayEquals(new byte[]{15},it.next().data());
        Assert.assertArrayEquals(null,it.next().data());
        Assert.assertArrayEquals(null,it.next().data());

        Assert.assertEquals(false,it.hasNext());



    }

}
