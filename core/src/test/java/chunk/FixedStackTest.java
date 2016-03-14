package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.KStack;
import org.mwdb.chunk.heap.FixedStack;
import org.mwdb.chunk.offheap.OffHeapFixedStack;

public class FixedStackTest {
    private static final int CAPACITY = 15;


    @Test
    public void heapFixedStackTest() {
        test(new FixedStack(CAPACITY));
    }

    @Test
    public void offHeapFixedStackTest() {
        test(new OffHeapFixedStack(CAPACITY, Constants.OFFHEAP_NULL_PTR));
    }

    public void test(KStack stack) {
        for (int i = 0; i < 3; i++) {
            Assert.assertTrue(stack.dequeueTail() == CAPACITY - (i + 1));
        }

        System.out.println((stack.dequeue(12)));
        System.out.println((stack.enqueue(14)));
        System.out.println((stack));

    }
}
