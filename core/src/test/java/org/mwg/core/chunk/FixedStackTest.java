package org.mwg.core.chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.core.chunk.heap.FixedStack;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.utility.Unsafe;

import java.util.concurrent.LinkedBlockingDeque;

public class FixedStackTest {
    private static final int CAPACITY = 15;


    @Test
    public void heapFixedStackTest() {
        test(new FixedStack(CAPACITY));
    }

    /**
     * @ignore ts
     */
    @Test
    public void offHeapFixedStackTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        OffHeapFixedStack stack = new OffHeapFixedStack(CAPACITY);
        test(stack);
        stack.free();

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    public void test(Stack stack) {
        // stack is initially full, dequeue until empty
        for (int i = 0; i < CAPACITY; i++) {
            Assert.assertTrue(stack.dequeueTail() == i);
        }
        // insert some values again
        Assert.assertTrue(stack.enqueue(0));
        Assert.assertTrue(stack.enqueue(1));
        Assert.assertTrue(stack.enqueue(2));
        Assert.assertTrue(stack.enqueue(3));
        Assert.assertTrue(stack.enqueue(4));
        // dequeue tail
        Assert.assertTrue(stack.dequeueTail() == 0);
        // enqueue
        Assert.assertTrue(stack.enqueue(5));
        // dequeue index
        Assert.assertTrue(stack.dequeue(2));
        // dequeue tail
        Assert.assertTrue(stack.dequeueTail() == 1);
        Assert.assertTrue(stack.dequeueTail() == 3);
        // dequeue invalid index
        Assert.assertFalse(stack.dequeue(2));
        Assert.assertFalse(stack.dequeue(1));
        Assert.assertFalse(stack.dequeue(0));
        // dequeue valid index
        Assert.assertTrue(stack.dequeue(4));
        // dequeue tail
        Assert.assertTrue(stack.dequeueTail() == 5);

    }
}
