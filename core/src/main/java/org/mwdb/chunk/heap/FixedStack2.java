package org.mwdb.chunk.heap;

import org.mwdb.chunk.KStack;

import java.util.concurrent.locks.ReentrantLock;

public class FixedStack2 implements KStack {

    private int _first;
    private int _last;
    private int[] _next;
    private int[] _prev;
    private int _count;
    final ReentrantLock lock = new ReentrantLock();
    final int _capacity;

    public FixedStack2(int capacity) {
        this._capacity = capacity;
        this._next = new int[capacity];
        this._prev = new int[capacity];
        this._first = -1;
        this._last = -1;
        for (int i = 0; i < capacity; i++) {
            int l = _last;
            _prev[i] = l;
            _last = i;
            if (_first == -1) {
                _first = i;
            } else {
                _next[l] = i;
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
            int castedIndex = (int) index;
            if (_first == castedIndex || _last == castedIndex) {
                return false;
            }
            if (_prev[castedIndex] != -1 || _next[castedIndex] != -1) { //test if was already in FIFO
                return false;
            }
            int l = _last;
            _prev[castedIndex] = l;
            _last = castedIndex;
            if (_first == -1) {
                _first = castedIndex;
            } else {
                _next[l] = castedIndex;
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
            int f = _first;
            if (f == -1) {
                return -1;
            }
            int n = _next[f];
            //tag as unused
            _next[f] = -1;
            _prev[f] = -1;
            _first = n;
            if (n == -1) {
                _last = -1;
            } else {
                _prev[n] = -1;
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
            int castedIndex = (int) index;
            int p = _prev[castedIndex];
            int n = _next[castedIndex];
            if (p == -1 && n == -1) {
                return false;
            }
            if (p == -1) {
                int f = _first;
                if (f == -1) {
                    return false;
                }
                int n2 = _next[f];
                _next[f] = -1;
                _prev[f] = -1;
                _first = n2;
                if (n2 == -1) {
                    _last = -1;
                } else {
                    _prev[n2] = -1;
                }
                --_count;
            } else if (n == -1) {
                int l = _last;
                if (l == -1) {
                    return false;
                }
                int p2 = _prev[l];
                _prev[l] = -1;
                _next[l] = -1;
                _last = p2;
                if (p2 == -1) {
                    _first = -1;
                } else {
                    _next[p2] = -1;
                }
                --_count;
            } else {
                _next[p] = n;
                _prev[n] = p;
                _prev[castedIndex] = -1;
                _next[castedIndex] = -1;
                --_count;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }


}
