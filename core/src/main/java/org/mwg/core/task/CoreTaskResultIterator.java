package org.mwg.core.task;

import org.mwg.task.TaskResultIterator;

class CoreTaskResultIterator<A> implements TaskResultIterator<A> {

    private final Object[] _backend;

    private final int _size;

    private int _current = 0;

    CoreTaskResultIterator(Object[] p_backend) {
        if(p_backend != null) {
            this._backend = p_backend;
        } else {
            _backend = new Object[0];
        }
        _size = _backend.length;
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
