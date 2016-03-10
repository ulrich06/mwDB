package org.mwdb.chunk.offheap;

import org.mwdb.utility.Unsafe;

public class OffHeapStringArray {

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

    public static void set(final long adr, final int index, final String valueToInsert) {
        long temp_stringPtr = unsafe.getLong(adr + index * 8);
        byte[] valueAsByte = valueToInsert.getBytes();
        long newStringPtr = unsafe.allocateMemory(4 + valueAsByte.length);
        //copy size of the string
        unsafe.putInt(newStringPtr, valueAsByte.length);
        //copy string content
        for (int i = 0; i < valueAsByte.length; i++) {
            unsafe.putByte(4 + newStringPtr + i, valueAsByte[i]);
        }
        //register the new stringPtr
        unsafe.putLong(adr + index * 8, newStringPtr);
        //freeMemory if notNull
        if (temp_stringPtr != -1) {
            unsafe.freeMemory(temp_stringPtr);
        }
    }

    public static String get(final long adr, final int index) {
        long stringPtr = unsafe.getLong(adr + index * 8);
        int length = unsafe.getInt(stringPtr);
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = unsafe.getByte(stringPtr + 4 + i);
        }
        return new String(bytes);
    }

    public static void free(final long adr, final long capacity) {
        for (long i = 0; i < capacity; i++) {
            long stringPtr = unsafe.getLong(adr + i * 8);
            unsafe.freeMemory(stringPtr);
        }
        unsafe.freeMemory(adr);
    }

}
