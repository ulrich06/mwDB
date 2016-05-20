
package org.mwg.core.chunk.heap;

import org.mwg.core.CoreConstants;
import org.mwg.struct.LongLongArrayMap;
import org.mwg.core.chunk.ChunkListener;
import org.mwg.struct.LongLongArrayMapCallBack;
import org.mwg.core.utility.PrimitiveHelper;

public class ArrayLongLongArrayMap implements LongLongArrayMap {

    private volatile InternalState state;
    private volatile boolean aligned;
    private final ChunkListener _listener;

    public ArrayLongLongArrayMap(ChunkListener p_listener, int initialCapacity, ArrayLongLongArrayMap p_origin) {
        this._listener = p_listener;
        if (p_origin == null) {
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity], new long[initialCapacity], new int[initialCapacity], new int[initialCapacity], 0, 0);
            for (int i = 0; i < initialCapacity; i++) {
                if (i == initialCapacity - 1) {
                    newstate._elementNext[i] = -1;
                } else {
                    newstate._elementNext[i] = i + 1;
                }
                newstate._elementHash[i] = -1;
                newstate._elementV[i] = CoreConstants.NULL_LONG; //we init all values to NULL_LONG
            }
            this.state = newstate;
            aligned = true;
        } else {
            this.state = p_origin.state;
            aligned = false;
        }
    }

    /**
     * Internal Map state, to be replace in a compare and swap manner
     */
    private final class InternalState {

        final int _stateSize;

        final long[] _elementK;

        final long[] _elementV;

        final int[] _elementNext;

        final int[] _elementHash;

        final int _threshold;

        volatile int _elementCount;

        volatile int _nextAvailableSlot;

        InternalState(int p_stateSize, long[] p_elementK, long[] p_elementV, int[] p_elementNext, int[] p_elementHash, int p_elementCount, int p_nextAvailableSlot) {
            this._stateSize = p_stateSize;
            this._elementK = p_elementK;
            this._elementV = p_elementV;
            this._elementNext = p_elementNext;
            this._elementHash = p_elementHash;
            this._elementCount = p_elementCount;
            this._nextAvailableSlot = p_nextAvailableSlot;
            this._threshold = (int) (p_stateSize * CoreConstants.MAP_LOAD_FACTOR);
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
            return new InternalState(_stateSize, cloned_elementK, cloned_elementV, cloned_elementNext, cloned_elementHash, _elementCount, _nextAvailableSlot);
        }
    }

    @Override
    public final long[] get(long key) {
        final InternalState internalState = state;
        if (internalState._stateSize == 0) {
            return new long[0];
        }
        final int hashIndex = (int) PrimitiveHelper.longHash(key, internalState._stateSize);
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
                        newCapacity = capacity * 2;
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
    public final void each(LongLongArrayMapCallBack callback) {
        final InternalState internalState = state;
        for (int i = 0; i < internalState._stateSize; i++) {
            if (internalState._elementV[i] != CoreConstants.NULL_LONG) { //there is a real value
                callback.on(internalState._elementK[i], internalState._elementV[i]);
            }
        }
    }

    @Override
    public long size() {
        return state._elementCount;
    }

    @Override
    public final void put(long key, long value) {
        internal_modify_map(key, value, true);
    }

    private synchronized void internal_modify_map(long key, long value, boolean toInsert) {
        //first test if reHash is necessary
        InternalState internalState = state;
        if (toInsert) {
            //no reHash in case of remove
            if ((internalState._elementCount + 1) > internalState._threshold) {
                int newCapacity = internalState._stateSize * 2;
                long[] newElementK = new long[newCapacity];
                long[] newElementV = new long[newCapacity];
                System.arraycopy(internalState._elementK, 0, newElementK, 0, internalState._stateSize);
                System.arraycopy(internalState._elementV, 0, newElementV, 0, internalState._stateSize);
                //reset new values
                for (int i = internalState._stateSize; i < newCapacity; i++) {
                    newElementV[i] = CoreConstants.NULL_LONG;
                }
                int[] newElementNext = new int[newCapacity];
                int[] newElementHash = new int[newCapacity];
                for (int i = 0; i < newCapacity; i++) {
                    newElementNext[i] = -1;
                    newElementHash[i] = -1;
                }
                //rehashEveryThing
                int previousEmptySlot = -1;
                int emptySlotHEad = -1;

                for (int i = 0; i < newElementV.length; i++) {
                    if (newElementV[i] != CoreConstants.NULL_LONG) { //there is a real value
                        int newHashIndex = (int) PrimitiveHelper.longHash(newElementK[i], newCapacity);
                        int currentHashedIndex = newElementHash[newHashIndex];
                        if (currentHashedIndex != -1) {
                            newElementNext[i] = currentHashedIndex;
                        } else {
                            newElementNext[i] = -2;
                        }
                        newElementHash[newHashIndex] = i;
                    } else {
                        //else we compute the availability stack
                        if (previousEmptySlot == -1) {
                            emptySlotHEad = i;
                        } else {
                            newElementNext[previousEmptySlot] = i;
                        }
                        previousEmptySlot = i;
                    }
                }
                internalState = new InternalState(newCapacity, newElementK, newElementV, newElementNext, newElementHash, internalState._elementCount, emptySlotHEad);
                state = internalState;
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
                state = state.clone();
                internalState = state;
                aligned = true;
            }
            //we deQueue next available index
            int newIndex = internalState._nextAvailableSlot;
            if (newIndex == -1) {
                throw new RuntimeException("Full Map should not happen, implementation error");
            }
            internalState._nextAvailableSlot = internalState._elementNext[newIndex];
            internalState._elementNext[newIndex] = -1; //reset next index

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
                    internalState._elementCount--;
                    internalState._elementK[m] = CoreConstants.NULL_LONG;
                    internalState._elementV[m] = CoreConstants.NULL_LONG;
                    if (previousM == -1) {
                        //we are in the top of hashFunction
                        internalState._elementHash[hashIndex] = internalState._elementNext[m];
                    } else {
                        internalState._elementNext[previousM] = internalState._elementNext[m];
                    }
                    //we enqueue m hasField in the available queue
                    internalState._elementNext[m] = internalState._nextAvailableSlot;
                    internalState._nextAvailableSlot = m;
                    return;
                }
                previousM = m;
                m = internalState._elementNext[m];
            }
        }
    }

    @Override
    public final void remove(long key, long value) {
        internal_modify_map(key, value, false);
    }

}



