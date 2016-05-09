package org.mwg.core.utility;

import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

abstract class AbstractBuffer implements Buffer {

    abstract byte[] slice(long initPos, long endPos);

    @Override
    public final BufferIterator iterator() {
        return new CoreBufferIterator(this);
    }

    /* Need to be present in Abstract class for TS*/
    public abstract byte read(long position);
    public abstract long size();

}

