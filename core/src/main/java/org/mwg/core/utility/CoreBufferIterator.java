package org.mwg.core.utility;

import org.mwg.core.CoreConstants;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

class CoreBufferIterator implements BufferIterator {

    private final AbstractBuffer _origin;
    private final long _originSize;
    private long _cursor = -1;

    CoreBufferIterator(AbstractBuffer p_origin) {
        _origin = p_origin;
        _originSize = p_origin.size();
    }

    @Override
    public final boolean hasNext() {
        return _cursor < _originSize;
    }

    @Override
    public final synchronized Buffer next() {

        long previousCursor = _cursor;
        while (_cursor < _originSize) {
            _cursor++;
            byte current = _origin.read(_cursor);
            if (current == CoreConstants.BUFFER_SEP) {
                return new BufferView(_origin, previousCursor + 1, _cursor - 1);
            }
        }
        if (previousCursor < _originSize) {
            return new BufferView(_origin, previousCursor + 1, _cursor -1);
        }
        return null;
    }
}
