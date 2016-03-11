package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KStack;

/**
 * @ignore ts
 */
public class OffHeapFixedStack implements KStack {

    //long arrays
    private static final int INDEX_PREVIOUS = 0;
    private static final int INDEX_NEXT = 1;
    //long values
    private static final int INDEX_HEAD = 2;
    private static final int INDEX_TAIL = 3;
    private static final int INDEX_LOCK = 4;
    private static final int INDEX_CAPACITY = 5;
    // pointer values
    private final long root_array_ptr;
    private final long previous_ptr;
    private final long next_ptr;

    public OffHeapFixedStack(long max, long previousAddr) {
        if (previousAddr == Constants.OFFHEAP_NULL_PTR) {
            root_array_ptr = OffHeapLongArray.allocate(6);
            /** init long values */
            OffHeapLongArray.set(root_array_ptr, INDEX_LOCK, 0);
            OffHeapLongArray.set(root_array_ptr, INDEX_HEAD, 0);
            OffHeapLongArray.set(root_array_ptr, INDEX_TAIL, max - 1);
            OffHeapLongArray.set(root_array_ptr, INDEX_CAPACITY, max);
            /** init long[] variables */
            previous_ptr = OffHeapLongArray.allocate(max);
            OffHeapLongArray.set(root_array_ptr, INDEX_PREVIOUS, previous_ptr);
            next_ptr = OffHeapLongArray.allocate(max);
            OffHeapLongArray.set(root_array_ptr, INDEX_NEXT, next_ptr);

            for (int i = 0; i < max; i++) {
                if (i != max - 1) {
                    OffHeapLongArray.set(next_ptr, i, i + 1);
                } else {
                    OffHeapLongArray.set(next_ptr, i, -1);
                }
                if (i == 0) {
                    OffHeapLongArray.set(previous_ptr, i, -1);
                } else {
                    OffHeapLongArray.set(previous_ptr, i, i - 1);
                }
            }
        } else {
            root_array_ptr = previousAddr;
            previous_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_PREVIOUS);
            next_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_NEXT);
        }
    }

    public void free() {
        OffHeapLongArray.free(next_ptr);
        OffHeapLongArray.free(previous_ptr);
        OffHeapLongArray.free(root_array_ptr);
    }

    @Override
    public boolean enqueue(long index) {
        //lock

        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1)) ;

        if (OffHeapLongArray.get(next_ptr, index) != -1) {
            //unlock
            OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
            //already enqueue, return false
            return false;
        }

        //head is now the index
        OffHeapLongArray.set(previous_ptr, OffHeapLongArray.get(root_array_ptr, INDEX_HEAD), index);
        OffHeapLongArray.set(next_ptr, index, OffHeapLongArray.get(root_array_ptr, INDEX_HEAD));
        OffHeapLongArray.set(root_array_ptr, INDEX_HEAD, index);

        OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
        return true;
    }

    @Override
    public long dequeueTail() {
        //lock
        OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1);

        long currentTail = OffHeapLongArray.get(root_array_ptr, INDEX_TAIL);
        if (currentTail == -1) {
            //FIFO is now, unlock and quite
            OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
            return -1;
        } else {
            long nextTail = OffHeapLongArray.get(previous_ptr, OffHeapLongArray.get(root_array_ptr, INDEX_TAIL));
            //tag index as unused
            OffHeapLongArray.set(next_ptr, currentTail, -1);
            OffHeapLongArray.set(previous_ptr, currentTail, -1);
            if (nextTail == -1) {
                //FIFO is now empty
                OffHeapLongArray.set(root_array_ptr, INDEX_TAIL, -1);
                OffHeapLongArray.set(root_array_ptr, INDEX_HEAD, -1);
            } else {
                //FIFO contains at least one
                OffHeapLongArray.set(next_ptr, nextTail, -2); //tag as still used
                OffHeapLongArray.set(root_array_ptr, INDEX_TAIL, nextTail);
            }
            //unlock
            OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
            return currentTail;
        }
    }

    @Override
    public boolean dequeue(long index) {
        //lock
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1)) ;

        if (OffHeapLongArray.get(next_ptr, index) != -1 || OffHeapLongArray.get(root_array_ptr, INDEX_TAIL) == -1) {//the element has been detached or tail is empty
            //unlock
            OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
            return false;
        }

        long currentNext = OffHeapLongArray.get(next_ptr, index);
        long currentPrevious = OffHeapLongArray.get(previous_ptr, index);
        if (OffHeapLongArray.get(root_array_ptr, INDEX_TAIL) == index) {
            OffHeapLongArray.set(next_ptr, currentPrevious, -2); //tag as used
            OffHeapLongArray.set(root_array_ptr, INDEX_TAIL, currentPrevious);
            //tag index as unused
            OffHeapLongArray.set(next_ptr, index, -1);
            OffHeapLongArray.set(previous_ptr, index, -1);
            OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
        } else {
            //reChain
            if (currentNext != -1) {
                OffHeapLongArray.set(previous_ptr, currentNext, currentPrevious);
            }
            if (currentPrevious != -1) {
                OffHeapLongArray.set(next_ptr, currentPrevious, currentNext);
            }
        }

        //unlock
        OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0);
        return true;
    }

    @Override
    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("_head=" + OffHeapLongArray.get(root_array_ptr, INDEX_HEAD) + "\n");
        buff.append("\n");

        long max = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        final StringBuffer nextBuff = new StringBuffer();
        final StringBuffer previousBuff = new StringBuffer();
        nextBuff.append("_next= ");
        previousBuff.append("_prev= ");
        for (long i = 0; i < max; i++) {
            nextBuff.append(OffHeapLongArray.get(next_ptr, i));
            previousBuff.append(OffHeapLongArray.get(previous_ptr, i));
            if (i < max - 1) {
                buff.append(", ");
            }
        }
        buff.append(nextBuff.toString());
        buff.append("\n");
        buff.append(previousBuff.toString());
        return buff.toString();
    }

}
