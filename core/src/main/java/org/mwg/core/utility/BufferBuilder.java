package org.mwg.core.utility;

import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.offheap.OffHeapByteArray;

public class BufferBuilder {

    private BufferBuilder() {
    }

    public static void keyToBuffer(org.mwg.struct.Buffer buffer, byte chunkType, long world, long time, long id) {
        buffer.write(chunkType);
        buffer.write(CoreConstants.KEY_SEP);
        Base64.encodeLongToBuffer(world, buffer);
        buffer.write(CoreConstants.KEY_SEP);
        Base64.encodeLongToBuffer(time, buffer);
        buffer.write(CoreConstants.KEY_SEP);
        Base64.encodeLongToBuffer(id, buffer);
    }

    private static long getNewSize(long old, long target) {
        while (old < target) {
            old = old * 2;
        }
        return old;
    }

    public static org.mwg.struct.Buffer newOffHeapBuffer() {
        
        return new AbstractBuffer() {

            private long bufferPtr = CoreConstants.OFFHEAP_NULL_PTR;

            private long writeCursor = 0;

            private long capacity = 0;

            @Override
            byte[] slice(long initPos, long endPos) {
                if(initPos == endPos) {
                    return new byte[0];
                }
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
                    capacity = getNewSize(CoreConstants.MAP_INITIAL_CAPACITY, bytes.length);
                    bufferPtr = OffHeapByteArray.allocate(capacity);
                    OffHeapByteArray.copyArray(bytes, bufferPtr, bytes.length);
                    writeCursor = bytes.length;
                } else if (writeCursor + bytes.length > capacity) {
                    long newCapacity = getNewSize(capacity, capacity + bytes.length);
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
        };
    }

    public static org.mwg.struct.Buffer newHeapBuffer() {
        return new AbstractBuffer() {

            private byte[] buffer;

            private int writeCursor;

            @Override
            byte[] slice(long initPos, long endPos) {
                if(initPos == endPos) {
                    return new byte[0];
                }
                int newSize = (int) (endPos - initPos + 1);
                byte[] newResult = new byte[newSize];
                System.arraycopy(buffer, (int) initPos, newResult, 0, newSize);
                return newResult;
            }

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
            public void writeAll(byte[] bytes) {
                if (buffer == null) {
                    int initSize = (int) getNewSize(CoreConstants.MAP_INITIAL_CAPACITY, bytes.length);
                    buffer = new byte[initSize];
                    System.arraycopy(bytes, 0, buffer, 0, bytes.length);
                    writeCursor = bytes.length;
                } else if (writeCursor + bytes.length > buffer.length) {
                    int newSize = (int) getNewSize(buffer.length, buffer.length + bytes.length);
                    byte[] tmp = new byte[newSize];
                    System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                    System.arraycopy(bytes, 0, tmp, writeCursor, bytes.length);
                    buffer = tmp;
                    writeCursor = writeCursor + bytes.length;
                } else {
                    System.arraycopy(bytes, 0, buffer, writeCursor, bytes.length);
                    writeCursor = writeCursor + bytes.length;
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

            @Override
            public void removeLast() {
                writeCursor--;
            }
        };
    }

}
