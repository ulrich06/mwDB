package org.mwg.core.chunk.offheap;

import org.mwg.core.CoreConstants;
import org.mwg.core.utility.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapByteArray {
    public static long alloc_counter = 0;

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    public static long allocate(final long capacity) {
        if (Unsafe.DEBUG_MODE) {
            alloc_counter++;
        }

        //create the memory segment
        long newMemorySegment = unsafe.allocateMemory(capacity);
        //init the memory
        unsafe.setMemory(newMemorySegment, capacity, (byte) CoreConstants.OFFHEAP_NULL_PTR);
        //return the newly created segment
        return newMemorySegment;
    }

    public static long reallocate(final long addr, final long previousCapacity, final long nextCapacity) {
        //allocate a new bigger segment
        long newBiggerMemorySegment = unsafe.allocateMemory(nextCapacity);
        //reset the segment selectWith -1
        unsafe.setMemory(newBiggerMemorySegment, nextCapacity, (byte) CoreConstants.OFFHEAP_NULL_PTR);
        //copy previous memory segment content
        unsafe.copyMemory(addr, newBiggerMemorySegment, previousCapacity);
        //free the previous
        unsafe.freeMemory(addr);
        //return the newly created segment
        return newBiggerMemorySegment;
    }

    public static void set(final long addr, final long index, final byte valueToInsert) {
        unsafe.putByteVolatile(null, addr + index, valueToInsert);
    }

    public static byte get(final long addr, final long index) {
        return unsafe.getByteVolatile(null, addr + index);
    }

    public static void free(final long addr) {
        if (Unsafe.DEBUG_MODE) {
            alloc_counter--;
        }

        unsafe.freeMemory(addr);
    }

    /*
    public static long cloneArray(final long srcAddr, final long length) {
        if (Unsafe.DEBUG_MODE) {
            alloc_counter++;
        }

        long newAddr = unsafe.allocateMemory(length);
        unsafe.copyMemory(srcAddr, newAddr, length);
        return newAddr;
    }*/

}
