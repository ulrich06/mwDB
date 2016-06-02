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
            Assert.assertEquals(data2[i], res[i + data.length]);
        }

        //write less data than the capacity
        buffer.writeAll(data);
        res = buffer.data();
        Assert.assertEquals(data.length + data2.length + data.length, res.length);
        for (int i = 0; i < data.length; i++) {
            Assert.assertEquals(data[i], res[i]);
        }
        for (int i = 0; i < data2.length; i++) {
            Assert.assertEquals(data2[i], res[i + data.length]);
        }
        for (int i = 0; i < data.length; i++) {
            Assert.assertEquals(data[i], res[i + data.length + data2.length]);

        }

    }

    private void testIterator(org.mwg.struct.Buffer buffer) {
        byte[] one = new byte[3];
        one[0] = (byte) "1".codePointAt(0);
        one[1] = (byte) "2".codePointAt(0);
        one[2] = (byte) "3".codePointAt(0);

        byte[] two = new byte[3];
        two[0] = (byte) "5".codePointAt(0);
        two[1] = (byte) "6".codePointAt(0);
        two[2] = (byte) "7".codePointAt(0);

        byte[] three = new byte[2];
        three[0] = (byte) "8".codePointAt(0);
        three[1] = (byte) "9".codePointAt(0);


        buffer.writeAll(one);
        buffer.write(BUFFER_SEP);
        buffer.writeAll(two);
        buffer.write(BUFFER_SEP);
        buffer.writeAll(three);

        BufferIterator it = buffer.iterator();
        Assert.assertEquals(it.hasNext(), true);

        Buffer view1 = it.next();
        Assert.assertNotNull(view1);
        Assert.assertEquals(view1.size(), 3);
        byte[] view1flat = view1.data();
        view1flat[0] = one[0];
        view1flat[1] = one[1];
        view1flat[2] = one[2];
        Assert.assertEquals(it.hasNext(), true);

        Buffer view2 = it.next();
        Assert.assertNotNull(view2);
        Assert.assertEquals(view2.size(), 3);
        byte[] view2flat = view2.data();
        view2flat[0] = two[0];
        view2flat[1] = two[1];
        view2flat[2] = two[2];
        Assert.assertEquals(it.hasNext(), true);

        Buffer view3 = it.next();
        Assert.assertNotNull(view3);
        Assert.assertEquals(view3.size(), 2);
        byte[] view3flat = view3.data();
        view3flat[0] = three[0];
        view3flat[1] = three[1];
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

        Assert.assertEquals(false, it.hasNext());

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


    @Test
    public void testIteratorHeap2() {
        testIterator2(BufferBuilder.newHeapBuffer());
    }

    @Test
    public void testIteratorOffHeap2() {
        testIterator2(BufferBuilder.newOffHeapBuffer());
    }

    public void testIterator2(Buffer buffer) {
        byte[] bytes = new byte[]{12, 11, BUFFER_SEP, 87, BUFFER_SEP, 87, 45};
        buffer.writeAll(bytes);
        BufferIterator it = buffer.iterator();

        Assert.assertArrayEquals(new byte[]{12, 11}, it.next().data());
        Assert.assertArrayEquals(new byte[]{87}, it.next().data());
        Assert.assertArrayEquals(new byte[]{87, 45}, it.next().data());

        Assert.assertEquals(false, it.hasNext());
    }

    @Test
    public void testOneElementBufferHeap() {
        testOneElementBuffer(BufferBuilder.newHeapBuffer());
    }

    @Test
    public void testOneElementBufferOffHeap() {
        testOneElementBuffer(BufferBuilder.newOffHeapBuffer());
    }

    public void testOneElementBuffer(Buffer oneElementBuffer) {
        byte[] bytesOneElementBuffer = new byte[]{15, CoreConstants.BUFFER_SEP, 16, CoreConstants.BUFFER_SEP, 17};

        oneElementBuffer.writeAll(bytesOneElementBuffer);

        BufferIterator itOneElementBuffer = oneElementBuffer.iterator();

        Assert.assertArrayEquals(new byte[]{15}, itOneElementBuffer.next().data());
        Assert.assertArrayEquals(new byte[]{16}, itOneElementBuffer.next().data());
        Assert.assertArrayEquals(new byte[]{17}, itOneElementBuffer.next().data());

        Assert.assertEquals(false, itOneElementBuffer.hasNext());
    }

    @Test
    public void testEmptyBufferHeap() {
        testEmptyBuffer(BufferBuilder.newHeapBuffer());
    }

    @Test
    public void testEmptyBufferOffHeap() {
        testEmptyBuffer(BufferBuilder.newOffHeapBuffer());
    }

    public void testEmptyBuffer(Buffer buffer) {
        byte[] bytes = new byte[]{BUFFER_SEP, BUFFER_SEP, BUFFER_SEP, 15, BUFFER_SEP, BUFFER_SEP};
        buffer.writeAll(bytes);

        BufferIterator it = buffer.iterator();

        BufferView next = (BufferView) it.next();
        Assert.assertArrayEquals(new byte[0], next.data());
        Assert.assertEquals(0, next.size());

        next = (BufferView) it.next();
        Assert.assertArrayEquals(new byte[0], next.data());
        Assert.assertEquals(0, next.size());

        next = (BufferView) it.next();
        Assert.assertArrayEquals(new byte[0], next.data());
        Assert.assertEquals(0, next.size());

        Assert.assertArrayEquals(new byte[]{15}, it.next().data());

        next = (BufferView) it.next();
        Assert.assertArrayEquals(new byte[0], next.data());
        Assert.assertEquals(0, next.size());

        next = (BufferView) it.next();
        Assert.assertArrayEquals(new byte[0], next.data());
        Assert.assertEquals(0, next.size());

        Assert.assertEquals(false, it.hasNext());

    }

    @Test
    public void testReadHeap() {
        testRead(BufferBuilder.newHeapBuffer());
    }

    @Test
    public void testReadOffHeap() {
        testRead(BufferBuilder.newOffHeapBuffer());
    }

    public void testRead(Buffer buffer) {
        byte[] bytes = new byte[]{BUFFER_SEP, BUFFER_SEP};
        buffer.writeAll(bytes);

        BufferIterator it = buffer.iterator();

        BufferView view = (BufferView) it.next();
        boolean catched = false;
        try {
            view.read(10);
        } catch (ArrayIndexOutOfBoundsException e) {
            catched = true;
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            Assert.assertEquals(true, catched);
        }

    }

    @Test
    public void testIteratorOnLimitSize() {
        byte[] bytes = new byte[]{47, 47, 47, 47, 43, 59, 73, 65, 65, 65, 65, 65, 69, 35, 0, 103};
        Buffer buffer = BufferBuilder.newHeapBuffer();

        buffer.writeAll(bytes);
        BufferIterator it = buffer.iterator();

        Assert.assertArrayEquals(new byte[]{47, 47, 47, 47, 43, 59, 73, 65, 65, 65, 65, 65, 69}, it.next().data());
        Assert.assertArrayEquals(new byte[]{0, 103}, it.next().data());

        Assert.assertEquals(false, it.hasNext());
    }

}
