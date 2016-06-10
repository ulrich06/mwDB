package org.mwg.core.utility;

import org.mwg.core.CoreConstants;

class HeapBuffer extends AbstractBuffer {

    private byte[] buffer;

    private int writeCursor;

    @Override
    byte[] slice(long initPos, long endPos) {
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
            int initSize = (int) BufferBuilder.getNewSize(CoreConstants.MAP_INITIAL_CAPACITY, bytes.length);
            buffer = new byte[initSize];
            System.arraycopy(bytes, 0, buffer, 0, bytes.length);
            writeCursor = bytes.length;
        } else if (writeCursor + bytes.length > buffer.length) {
            int newSize = (int) BufferBuilder.getNewSize(buffer.length, buffer.length + bytes.length);
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
    public long length() {
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
}
