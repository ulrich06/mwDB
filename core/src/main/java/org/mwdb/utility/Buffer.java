package org.mwdb.utility;

import org.mwdb.Constants;
import org.mwdb.plugin.KStorage;

public class Buffer {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    public static KStorage.KBuffer newOffHeapBuffer() {
        return null;
    }

    public static KStorage.KBuffer newHeapBuffer() {
        return new KStorage.KBuffer() {

            private byte[] buffer;

            private int writeCursor;

            @Override
            public void write(Byte b) {
                if (buffer == null) {
                    buffer = new byte[Constants.MAP_INITIAL_CAPACITY];
                    buffer[0] = b;
                    writeCursor = 1;
                } else if (writeCursor == buffer.length) {
                    byte[] temp = new byte[buffer.length << 1];
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
