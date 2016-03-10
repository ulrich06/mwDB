package org.mwdb.chunk.offheap;

import org.mwdb.utility.Unsafe;

public class OffHeapLongArray {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    public static long init(final long capacity) {
        //create the memory segment
        long newMemorySegment = unsafe.allocateMemory(capacity * 8);
        //init the memory
        unsafe.setMemory(newMemorySegment, capacity * 8, (byte) -1);
        //return the newly created segment
        return newMemorySegment;
    }

    public static long reallocate(final long adr, final long previousCapacity, final long nextCapacity) {
        //allocate a new bigger segment
        long newBiggerMemorySegment = unsafe.allocateMemory(nextCapacity * 8);
        //reset the segment with -1
        unsafe.setMemory(newBiggerMemorySegment, nextCapacity * 8, (byte) -1);
        //copy previous memory segment content
        unsafe.copyMemory(newBiggerMemorySegment, adr, previousCapacity * 8);
        //free the previous
        unsafe.freeMemory(adr);
        //return the newly created segment
        return newBiggerMemorySegment;
    }

    public static void set(final long adr, final int index, final long valueToInsert) {
        unsafe.putLong(adr + index * 8, valueToInsert);
    }

    public static long get(final long adr, final int index) {
        return unsafe.getLong(adr + index * 8);
    }

    public static void free(final long adr){
        unsafe.freeMemory(adr);
    }

}
