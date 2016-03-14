package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapLongArray {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    public static long allocate(final long capacity) {
        //create the memory segment
        long newMemorySegment = unsafe.allocateMemory(capacity * 8);
        //init the memory
        unsafe.setMemory(newMemorySegment, capacity * 8, (byte) Constants.OFFHEAP_NULL_PTR);
        //return the newly created segment
        return newMemorySegment;
    }

    public static long reallocate(final long addr, final long previousCapacity, final long nextCapacity) {
        //allocate a new bigger segment
        long newBiggerMemorySegment = unsafe.allocateMemory(nextCapacity * 8);
        //reset the segment with -1
        unsafe.setMemory(newBiggerMemorySegment, nextCapacity * 8, (byte) Constants.OFFHEAP_NULL_PTR);
        //copy previous memory segment content
        unsafe.copyMemory(addr, newBiggerMemorySegment, previousCapacity * 8);
        //free the previous
        unsafe.freeMemory(addr);
        //return the newly created segment
        return newBiggerMemorySegment;
    }

    public static void set(final long addr, final long index, final long valueToInsert) {
        unsafe.putLongVolatile(null,addr + index * 8, valueToInsert);
    }

    public static long get(final long addr, final long index) {
        return unsafe.getLongVolatile(null, addr + index * 8);
    }

    public static void free(final long addr) {
        unsafe.freeMemory(addr);
    }

    public static boolean compareAndSwap(final long addr, final long index, final long expectedValue, final long updatedValue) {
        return unsafe.compareAndSwapLong(null, addr + index * 8, expectedValue, updatedValue);
    }


    public static long cloneArray(final long srcAddr, final long length) {
        long newAddr = unsafe.allocateMemory(length * 8);
        unsafe.copyMemory(srcAddr, newAddr, length * 8);
        return newAddr;
    }

}
