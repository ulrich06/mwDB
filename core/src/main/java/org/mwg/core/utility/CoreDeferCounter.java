package org.mwg.core.utility;

import org.mwg.Callback;
import org.mwg.plugin.Job;

import java.util.concurrent.atomic.AtomicInteger;

public class CoreDeferCounter implements org.mwg.DeferCounter {

    private final AtomicInteger _nb_down;

    private final int _counter;

    private Job _end;

    public CoreDeferCounter(int nb) {
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
                _end.run();
            }
        }
    }

    @Override
    public void then(Job p_callback) {
        this._end = p_callback;
        if (this._nb_down.get() == _counter) {
            if (p_callback != null) {
                p_callback.run();
            }
        }
    }

    private Object _result = null;

    @Override
    public Callback wrap() {
        return new Callback() {
            @Override
            public void on(Object result) {
                _result = result;
                count();
            }
        };
    }

    @Override
    public Object waitResult() {
        while (this._nb_down.get() != _counter) {
            //TODO wait here better...
        }
        return _result;
    }
}
