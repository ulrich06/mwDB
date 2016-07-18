package org.mwg.core.utility;

import org.mwg.task.TaskResultIterator;

class CoreTaskResultIterator<A> implements TaskResultIterator<A> {

    private final Object[] _backend;

    private final int _size;

    private int _current = 0;

    CoreTaskResultIterator(Object[] p_backend) {
        this._backend = p_backend;
        this._size = p_backend.length;
    }

    @Override
    public A next() {
        if (_current < _size) {
            Object result = _backend[_current];
            _current++;
            return (A) result;
        } else {
            return null;
        }
    }
}
