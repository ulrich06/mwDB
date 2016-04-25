package org.mwg.core.utility;

import org.mwg.Callback;

import java.util.concurrent.atomic.AtomicInteger;

public class DeferCounter implements org.mwg.DeferCounter {

    private final AtomicInteger _nb_down;

    private final int _counter;

    private Callback _end;

    public DeferCounter(int nb) {
        this._counter = nb;
        this._nb_down = new AtomicInteger(0);
    }

    @Override
    public void count() {
        int previous;
        int next;
        do {
            previous = this._nb_down.get();
            next = previous + 1;
        } while (!this._nb_down.compareAndSet(previous, next));
        if (next == _counter) {
            if (_end != null) {
                _end.on(null);
            }
        }
    }

    @Override
    public void then(Callback p_callback) {
        this._end = p_callback;
        if (this._nb_down.get() == _counter) {
            if (p_callback != null) {
                p_callback.on(null);
            }
        }
    }
}
