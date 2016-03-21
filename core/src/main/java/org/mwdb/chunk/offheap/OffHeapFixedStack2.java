package org.mwdb.chunk.offheap;

import org.mwdb.chunk.KStack;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @ignore ts
 */
public class OffHeapFixedStack2 implements KStack {

    private long _first;
    private long _last;
    private long _next;
    private long _prev;
    private long _count;
    final ReentrantLock lock = new ReentrantLock();
    final long _capacity;

    public OffHeapFixedStack2(long capacity) {
        this._capacity = capacity;
        this._next = OffHeapLongArray.allocate(capacity);
        this._prev = OffHeapLongArray.allocate(capacity);
        this._first = -1;
        this._last = -1;
        for (long i = 0; i < capacity; i++) {
            long l = _last;
            OffHeapLongArray.set(_prev, i, l);
            _last = i;
            if (_first == -1) {
                _first = i;
            } else {
                OffHeapLongArray.set(_next, l, i);
            }
        }
        _count = capacity;
    }

    @Override
    public boolean enqueue(long index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (_count >= _capacity) {
                return false;
            }
            if (_first == index || _last == index) {
                return false;
            }
            if (OffHeapLongArray.get(_prev, index) != -1 || OffHeapLongArray.get(_next, index) != -1) { //test if was already in FIFO
                return false;
            }
            long l = _last;
            OffHeapLongArray.set(_prev, index, l);
            _last = index;
            if (_first == -1) {
                _first = index;
            } else {
                OffHeapLongArray.set(_next, l, index);
            }
            ++_count;
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long dequeueTail() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            long f = _first;
            if (f == -1) {
                return -1;
            }
            long n = OffHeapLongArray.get(_next, f);
            //tag as unused
            OffHeapLongArray.set(_next, f, -1);
            OffHeapLongArray.set(_prev, f, -1);
            _first = n;
            if (n == -1) {
                _last = -1;
            } else {
                OffHeapLongArray.set(_prev, n, -1);
            }
            --_count;
            return f;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean dequeue(long index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            long p = OffHeapLongArray.get(_prev, index);
            long n = OffHeapLongArray.get(_next, index);
            if (p == -1 && n == -1) {
                return false;
            }
            if (p == -1) {
                long f = _first;
                if (f == -1) {
                    return false;
                }
                long n2 = OffHeapLongArray.get(_next, f);
                OffHeapLongArray.set(_next, f, -1);
                OffHeapLongArray.set(_prev, f, -1);
                _first = n2;
                if (n2 == -1) {
                    _last = -1;
                } else {
                    OffHeapLongArray.set(_prev, n2, -1);
                }
                --_count;
            } else if (n == -1) {
                long l = _last;
                if (l == -1) {
                    return false;
                }
                long p2 = OffHeapLongArray.get(_prev, l);
                OffHeapLongArray.set(_prev, l, -1);
                OffHeapLongArray.set(_next, l, -1);
                _last = p2;
                if (p2 == -1) {
                    _first = -1;
                } else {
                    OffHeapLongArray.set(_next, p2, -1);
                }
                --_count;
            } else {
                OffHeapLongArray.set(_next, p, n);
                OffHeapLongArray.set(_prev, n, p);
                OffHeapLongArray.set(_prev, index, -1);
                OffHeapLongArray.set(_next, index, -1);
                --_count;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void free() {
        OffHeapLongArray.free(_next);
        OffHeapLongArray.free(_prev);
    }

}
