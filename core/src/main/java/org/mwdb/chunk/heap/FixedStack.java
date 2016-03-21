package org.mwdb.chunk.heap;

import org.mwdb.chunk.KStack;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixedStack implements KStack {

    /*
    private final AtomicBoolean _lock;
    private final int[] _previous;
    private final int[] _next;
    private volatile int _head; //youngest
    private volatile int _tail; //youngest
*/
    private LinkedBlockingDeque<Long> q;

    public FixedStack(int max) {

        q = new LinkedBlockingDeque<Long>();
        for(long i=0;i<max;i++){
            q.add(i);
        }

        /*
        //init variables
        this._previous = new int[max];
        this._next = new int[max];
        this._lock = new AtomicBoolean(false);
        //fill the stack
        this._head = 0;
        for (int i = 0; i < max; i++) {
            if (i != max - 1) {
                this._next[i] = i + 1;
            } else {
                this._next[i] = -1;
            }
            if (i == 0) {
                this._previous[i] = -1;
            } else {
                this._previous[i] = i - 1;
            }
        }
        this._tail = max - 1;
        */
    }

    @Override
    public boolean enqueue(long index) {
        /*
        int castedIndex = (int) index;
        //lock
        while (!_lock.compareAndSet(false, true)) ;

        if (this._next[castedIndex] != -1) {
            //unlock
            _lock.compareAndSet(true, false);
            //already enqueue, return false
            return false;
        }

        //head is now the index
        int previousOfHead = this._previous[this._head];
        this._previous[this._head] = castedIndex;
        this._next[castedIndex] = this._head;
        this._head = castedIndex;
        this._previous[this._head] = previousOfHead;

        _lock.compareAndSet(true, false);
        return true;*/

        q.add(index);
        return true;
    }

    @Override
    public long dequeueTail() {

        return q.poll();
        /*
        //lock
        while (!_lock.compareAndSet(false, true)) ;

        int currentTail = this._tail;
        if (currentTail == -1) {
            //FIFO is now, unlock and quite
            _lock.compareAndSet(true, false);
            return -1;
        } else {
            int nextTail = this._previous[this._tail];
            //tag index as unused
            this._next[currentTail] = -1;
            this._previous[currentTail] = -1;
            if (nextTail == -1) {
                //FIFO is now empty
                this._tail = 0;
                this._head = 0;
            } else {
                //FIFO contains at least one
                this._next[nextTail] = -2; //tag as still used
                this._tail = nextTail;
            }
            //unlock
            _lock.compareAndSet(true, false);
            return currentTail;
        }
        */
    }

    @Override
    public boolean dequeue(long index) {

        return q.remove(index);

        /*
        int castedIndex = (int) index;

        //lock
        while (!_lock.compareAndSet(false, true)) ;

        if (_next[castedIndex] == -1 || this._tail == -1) {//the element has been detached or tail is empty
            //unlock
            _lock.compareAndSet(true, false);
            return false;
        }

        int currentNext = this._next[castedIndex];
        int currentPrevious = this._previous[castedIndex];
        //tag index as unused
        this._next[castedIndex] = -1;
        this._previous[castedIndex] = -1;

        if (this._tail == index) {
            this._next[currentPrevious] = -2; //tag as used
            this._tail = currentPrevious;
            _lock.compareAndSet(true, false);
        } else {
            //reChain
            if (currentNext != -1) {
                this._previous[currentNext] = currentPrevious;

            }
            if (currentPrevious != -1) {
                this._next[currentPrevious] = currentNext;
            }
        }

        //unlock
        _lock.compareAndSet(true, false);
        return true;
        */
    }

    @Override
    public void free() {

    }


}
