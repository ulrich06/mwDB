package org.mwg.core.utility;

import org.mwg.core.CoreConstants;

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

    static long getNewSize(long old, long target) {
        while (old < target) {
            old = old * 2;
        }
        return old;
    }

    /**
     * @native ts
     * return new org.mwg.core.utility.HeapBuffer();
     */
    public static org.mwg.struct.Buffer newOffHeapBuffer() {
        return new OffHeapBuffer();
    }

    public static org.mwg.struct.Buffer newHeapBuffer() {
        return new HeapBuffer();
    }

}
