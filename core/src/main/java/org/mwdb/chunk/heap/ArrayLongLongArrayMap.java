
package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KLongLongArrayMap;
import org.mwdb.chunk.KLongLongArrayMapCallBack;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicReference;

public class ArrayLongLongArrayMap implements KLongLongArrayMap {

    // TODO get at many places but set only in constructor and modify method -> normal ref is enough
    private final AtomicReference<InternalState> state;

    private final KChunkListener _listener;

    // TODO only used in constructor and synchronized method -> volatile not needed
    private volatile boolean aligned;

    public ArrayLongLongArrayMap(KChunkListener p_listener, int initialCapacity, ArrayLongLongArrayMap p_origin) {
        this._listener = p_listener;
        this.state = new AtomicReference<InternalState>();
        if (p_origin == null) {
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity], new long[initialCapacity], new int[initialCapacity], new int[initialCapacity], 0, 0);
            for (int i = 0; i < initialCapacity; i++) {
                newstate._elementNext[i] = -1;
                newstate._elementHash[i] = -1;
            }
            this.state.set(newstate);
            aligned = true;
        } else {
            this.state.set(p_origin.state.get());
            aligned = false;
        }
    }

    /**
     * Internal Map state, to be replace in a compare and swap manner
     */
    final class InternalState {

        final int _stateSize;

        final long[] _elementK;

        final long[] _elementV;

        final int[] _elementNext;

        final int[] _elementHash;

        final int _threshold;

        volatile int _elementCount;

        volatile int _elementDeleted;

        public InternalState(int p_stateSize, long[] p_elementK, long[] p_elementV, int[] p_elementNext, int[] p_elementHash, int p_elementCount, int p_elementDeleted) {
            this._stateSize = p_stateSize;
            this._elementK = p_elementK;
            this._elementV = p_elementV;
            this._elementNext = p_elementNext;
            this._elementHash = p_elementHash;
            this._elementCount = p_elementCount;
            this._elementDeleted = p_elementDeleted;
            this._threshold = (int) (p_stateSize * Constants.MAP_LOAD_FACTOR);
        }

        public InternalState clone() {
            long[] cloned_elementK = new long[_stateSize];
            System.arraycopy(_elementK, 0, cloned_elementK, 0, _stateSize);
            long[] cloned_elementV = new long[_stateSize];
            System.arraycopy(_elementV, 0, cloned_elementV, 0, _stateSize);
            int[] cloned_elementNext = new int[_stateSize];
            System.arraycopy(_elementNext, 0, cloned_elementNext, 0, _stateSize);
            int[] cloned_elementHash = new int[_stateSize];
            System.arraycopy(_elementHash, 0, cloned_elementHash, 0, _stateSize);
            return new InternalState(_stateSize, cloned_elementK, cloned_elementV, cloned_elementNext, cloned_elementHash, _elementCount, _elementDeleted);
        }
    }

    @Override
    public final long[] get(long key) {
        InternalState internalState = state.get();
        if (internalState._stateSize == 0) {
            return new long[0];
        }
        int hashIndex = (int) PrimitiveHelper.longHash(key, internalState._stateSize);
        long[] result = new long[0];
        int capacity = 0;
        int resultIndex = 0;

        int m = internalState._elementHash[hashIndex];
        while (m >= 0) {
            if (key == internalState._elementK[m]) {
                if (resultIndex == capacity) {
                    int newCapacity;
                    if (capacity == 0) {
                        newCapacity = 1;
                    } else {
                        newCapacity = capacity << 1;
                    }
                    long[] tempResult = new long[newCapacity];
                    System.arraycopy(result, 0, tempResult, 0, result.length);
                    result = tempResult;
                    capacity = newCapacity;
                }
                result[resultIndex] = internalState._elementV[m];
                resultIndex++;
            }
            m = internalState._elementNext[m];
        }
        if (resultIndex == capacity) {
            return result;
        } else {
            //shrink result
            long[] shrinkedResult = new long[resultIndex];
            System.arraycopy(result, 0, shrinkedResult, 0, resultIndex);
            return shrinkedResult;
        }
    }

    @Override
    public final void each(KLongLongArrayMapCallBack callback) {
        InternalState internalState = state.get();
        for (int i = 0; i < internalState._elementCount; i++) {
            if (internalState._elementNext[i] != -1) { //there is a real value
                callback.on(internalState._elementK[i], internalState._elementV[i]);
            }
        }
    }

    @Override
    public long size() {
        InternalState internalState = state.get();
        return internalState._elementCount - internalState._elementDeleted;
    }

    // TODO no need to synchronize this method, internal_modify_map is already synchronized
    @Override
    public final synchronized void put(long key, long value) {
        internal_modify_map(key, value, true);
    }

    private synchronized void internal_modify_map(long key, long value, boolean toInsert) {
        //first test if reHash is necessary
        InternalState internalState = state.get();
        if (toInsert) {
            //no reHash in case of remove
            if ((internalState._elementCount + 1) > internalState._threshold) {
                int newCapacity = internalState._stateSize << 1;
                long[] newElementK = new long[newCapacity];
                long[] newElementV = new long[newCapacity];
                System.arraycopy(internalState._elementK, 0, newElementK, 0, internalState._stateSize);
                System.arraycopy(internalState._elementV, 0, newElementV, 0, internalState._stateSize);
                int[] newElementNext = new int[newCapacity];
                int[] newElementHash = new int[newCapacity];
                for (int i = 0; i < newCapacity; i++) {
                    newElementNext[i] = -1;
                    newElementHash[i] = -1;
                }
                //rehashEveryThing
                for (int i = 0; i < internalState._elementCount; i++) {
                    if (internalState._elementNext[i] != -1) { //there is a real value
                        int newHashIndex = (int) PrimitiveHelper.longHash(internalState._elementK[i], newCapacity);
                        int currentHashedIndex = newElementHash[newHashIndex];
                        if (currentHashedIndex != -1) {
                            newElementNext[i] = currentHashedIndex;
                        } else {
                            newElementNext[i] = -2;
                        }
                        newElementHash[newHashIndex] = i;
                    }
                }
                internalState = new InternalState(newCapacity, newElementK, newElementV, newElementNext, newElementHash, internalState._elementCount, internalState._elementDeleted);
                state.set(internalState);
            }
            int hashIndex = (int) PrimitiveHelper.longHash(key, internalState._stateSize);
            int m = internalState._elementHash[hashIndex];
            while (m >= 0) {
                if (key == internalState._elementK[m] && value == internalState._elementV[m]) {
                    return;
                }
                m = internalState._elementNext[m];
            }

            //now we are sure that the current state have to be altered, so we realigne it if necesserary
            if (!aligned) {
                //clone the state
                state.set(state.get().clone());
                internalState = state.get();
                aligned = true;
            }

            int newIndex = internalState._elementCount;
            internalState._elementK[newIndex] = key;
            internalState._elementV[newIndex] = value;
            int currentHashedElemIndex = internalState._elementHash[hashIndex];
            if (currentHashedElemIndex != -1) {
                internalState._elementNext[newIndex] = currentHashedElemIndex;
            } else {
                internalState._elementNext[newIndex] = -2;
            }
            internalState._elementHash[hashIndex] = newIndex;
            internalState._elementCount = internalState._elementCount + 1;
            _listener.declareDirty(null);
        } else {
            int hashIndex = (int) PrimitiveHelper.longHash(key, internalState._stateSize);
            int m = internalState._elementHash[hashIndex];
            int previousM = -1;
            while (m >= 0) {
                if (key == internalState._elementK[m] && value == internalState._elementV[m]) {
                    internalState._elementDeleted++;
                    internalState._elementK[m] = Constants.NULL_LONG;
                    internalState._elementV[m] = Constants.NULL_LONG;
                    if (previousM == -1) {
                        //we are in the top of hashFunction
                        internalState._elementHash[hashIndex] = internalState._elementNext[m];
                    } else {
                        internalState._elementNext[previousM] = internalState._elementNext[m];
                    }
                    internalState._elementNext[m] = -1;
                    return;
                }
                previousM = m;
                m = internalState._elementNext[m];
            }
        }

    }

    @Override
    public void remove(long key, long value) {
        internal_modify_map(key, value, false);
    }

}



