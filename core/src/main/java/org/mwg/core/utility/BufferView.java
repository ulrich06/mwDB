package org.mwg.core.utility;

import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

public class BufferView implements Buffer {

    private AbstractBuffer _origin;

    private long _initPos;

    private long _endPos;

    BufferView(AbstractBuffer p_origin, long p_initPos, long p_endPos) {
        this._origin = p_origin;
        this._initPos = p_initPos;
        this._endPos = p_endPos;
        if (_endPos < _initPos) {
            _endPos = _initPos;
        }
    }

    @Override
    public final void write(byte b) {
        throw new RuntimeException("Write operation forbidden during iteration");
    }

    @Override
    public final void writeAll(byte[] bytes) {
        throw new RuntimeException("Write operation forbidden during iteration");
    }

    @Override
    public final byte read(long position) {
        return _origin.read(_initPos + position);
    }

    @Override
    public final byte[] data() {
        return _origin.slice(_initPos, _endPos);
    }

    @Override
    public final long size() {
        /*if (_initPos == _endPos) {
            return 0;
        } else {
            return _endPos - _initPos + 1;
        }*/
        return _endPos - _initPos + 1;

    }

    @Override
    public final void free() {
        throw new RuntimeException("Free operation forbidden during iteration");
    }

    @Override
    public final BufferIterator iterator() {
        throw new RuntimeException("iterator creation forbidden forbidden during iteration");
    }

    @Override
    public void removeLast() {
        throw new RuntimeException("Write operation forbidden during iteration");
    }
}
