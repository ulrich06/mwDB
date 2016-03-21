package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.chunk.KStack;
import org.mwdb.chunk.heap.FixedStack;
import org.mwdb.chunk.offheap.*;

import java.util.concurrent.LinkedBlockingDeque;

public class FixedStackTest {
    private static final int CAPACITY = 15;

    class RefFixedStack implements KStack {

        private LinkedBlockingDeque<Long> q;

        public RefFixedStack(int max) {
            q = new LinkedBlockingDeque<Long>();
            for (long i = 0; i < max; i++) {
                q.add(i);
            }
        }

        @Override
        public boolean enqueue(long index) {
            q.add(index);
            return true;
        }

        @Override
        public long dequeueTail() {
            return q.poll();
        }

        @Override
        public boolean dequeue(long index) {
            return q.remove(index);
        }

        @Override
        public void free() {
        }

    }


    @Test
    public void heapFixedStackTest() {
        test(new FixedStack(CAPACITY));
    }

    @Test
    public void offHeapFixedStackTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        OffHeapFixedStack stack = new OffHeapFixedStack(CAPACITY);
        test(stack);
        stack.free();

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    public void test(KStack stack) {
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
