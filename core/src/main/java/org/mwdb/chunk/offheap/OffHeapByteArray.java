package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapByteArray {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    public static long allocate(final long capacity) {
        //create the memory segment
        long newMemorySegment = unsafe.allocateMemory(capacity);
        //init the memory
        unsafe.setMemory(newMemorySegment, capacity, (byte) Constants.OFFHEAP_NULL_PTR);
        //return the newly created segment
        return newMemorySegment;
    }

    public static long reallocate(final long addr, final long previousCapacity, final long nextCapacity) {
        //allocate a new bigger segment
        long newBiggerMemorySegment = unsafe.allocateMemory(nextCapacity);
        //reset the segment with -1
        unsafe.setMemory(newBiggerMemorySegment, nextCapacity, (byte) Constants.OFFHEAP_NULL_PTR);
        //copy previous memory segment content
        unsafe.copyMemory(addr, newBiggerMemorySegment, previousCapacity);
        //free the previous
        unsafe.freeMemory(addr);
        //return the newly created segment
        return newBiggerMemorySegment;
    }

    public static void set(final long addr, final long index, final byte valueToInsert) {
        unsafe.putByte(addr + index, valueToInsert);
    }

    public static byte get(final long addr, final long index) {
        return unsafe.getByte(addr + index);
    }

    public static void free(final long addr) {
        unsafe.freeMemory(addr);
    }

}
