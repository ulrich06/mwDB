package org.mwg.core.utility;

import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.offheap.OffHeapByteArray;

public class Buffer {

    public static void keyToBuffer(org.mwg.struct.Buffer buffer, byte chunkType, long world, long time, long id) {
        buffer.write(chunkType);
        buffer.write(CoreConstants.KEY_SEP);
        Base64.encodeLongToBuffer(world, buffer);
        buffer.write(CoreConstants.KEY_SEP);
        Base64.encodeLongToBuffer(time, buffer);
        buffer.write(CoreConstants.KEY_SEP);
        Base64.encodeLongToBuffer(id, buffer);
    }

    public static org.mwg.struct.Buffer newOffHeapBuffer() {
        return new org.mwg.struct.Buffer() {

            private long bufferPtr = CoreConstants.OFFHEAP_NULL_PTR;

            private long writeCursor = 0;

            private long capacity = 0;

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
        };
    }

    public static org.mwg.struct.Buffer newHeapBuffer() {
        return new org.mwg.struct.Buffer() {

            private byte[] buffer;

            private int writeCursor;

            @Override
            public void write(byte b) {
                if (buffer == null) {
                    buffer = new byte[CoreConstants.MAP_INITIAL_CAPACITY];
                    buffer[0] = b;
                    writeCursor = 1;
                } else if (writeCursor == buffer.length) {
                    byte[] temp = new byte[buffer.length * 2];
                    System.arraycopy(buffer, 0, temp, 0, buffer.length);
                    temp[writeCursor] = b;
                    writeCursor++;
                    buffer = temp;
                } else {
                    buffer[writeCursor] = b;
                    writeCursor++;
                }
            }

            @Override
            public byte read(long position) {
                return buffer[(int) position];
            }

            @Override
            public byte[] data() {
                byte[] copy = new byte[writeCursor];
                System.arraycopy(buffer, 0, copy, 0, writeCursor);
                return copy;
            }

            @Override
            public long size() {
                return writeCursor;
            }

            @Override
            public void free() {
                buffer = null;
            }
        };
    }

}
