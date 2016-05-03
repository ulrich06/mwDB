package org.mwg.core.utility;

import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.offheap.OffHeapByteArray;

/**
 * @ignore ts
 */
class OffHeapBuffer extends AbstractBuffer {

    private long bufferPtr = CoreConstants.OFFHEAP_NULL_PTR;

    private long writeCursor = 0;

    private long capacity = 0;

    @Override
    byte[] slice(long initPos, long endPos) {
        int newSize = (int) (endPos - initPos + 1);
        byte[] result = new byte[newSize];
        for (int i = 0; i < newSize; i++) {
            result[i] = OffHeapByteArray.get(bufferPtr, i + initPos);
        }
        return result;
    }

    @Override
    public void write(byte b) {
        if (bufferPtr == CoreConstants.OFFHEAP_NULL_PTR) {
            capacity = CoreConstants.MAP_INITIAL_CAPACITY;
            bufferPtr = OffHeapByteArray.allocate(capacity);
            OffHeapByteArray.set(bufferPtr, writeCursor, b);
            writeCursor++;
        } else if (writeCursor == capacity) {
            long newCapacity = capacity * 2;
            bufferPtr = OffHeapByteArray.reallocate(bufferPtr, capacity, newCapacity);
            capacity = newCapacity;
            OffHeapByteArray.set(bufferPtr, writeCursor, b);
            writeCursor++;
        } else {
            OffHeapByteArray.set(bufferPtr, writeCursor, b);
            writeCursor++;
        }
    }

    @Override
    public void writeAll(byte[] bytes) {
        if (bufferPtr == CoreConstants.OFFHEAP_NULL_PTR) {
            capacity = BufferBuilder.getNewSize(CoreConstants.MAP_INITIAL_CAPACITY, bytes.length);
            bufferPtr = OffHeapByteArray.allocate(capacity);
            OffHeapByteArray.copyArray(bytes, bufferPtr, bytes.length);
            writeCursor = bytes.length;
        } else if (writeCursor + bytes.length > capacity) {
            long newCapacity = BufferBuilder.getNewSize(capacity, capacity + bytes.length);
            bufferPtr = OffHeapByteArray.reallocate(bufferPtr, capacity, newCapacity);
            OffHeapByteArray.copyArray(bytes, bufferPtr + writeCursor, bytes.length);
            capacity = newCapacity;
            writeCursor = writeCursor + bytes.length;
        } else {
            OffHeapByteArray.copyArray(bytes, bufferPtr + writeCursor, bytes.length);
            writeCursor = writeCursor + bytes.length;
        }
    }

    @Override
    public byte read(long position) {
        if (bufferPtr != CoreConstants.OFFHEAP_NULL_PTR && position < capacity) {
            return OffHeapByteArray.get(bufferPtr, position);
        }
        return -1;
    }

    @Override
    public byte[] data() {
        byte[] result = new byte[(int) writeCursor];
        for (long i = 0; i < writeCursor; i++) {
            result[(int) i] = OffHeapByteArray.get(bufferPtr, i);
        }
        return result;
    }

    @Override
    public long size() {
        return writeCursor;
    }

    @Override
    public void free() {
        if (bufferPtr != CoreConstants.OFFHEAP_NULL_PTR) {
            OffHeapByteArray.free(bufferPtr);
            bufferPtr = CoreConstants.OFFHEAP_NULL_PTR;
        }
    }

    @Override
    public void removeLast() {
        writeCursor--;
    }

}
