
package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStringLongMap;
import org.mwdb.chunk.KStringLongMapCallBack;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicReference;

public class ArrayStringLongMap implements KStringLongMap {

    private final AtomicReference<InternalState> state;

    private final KChunkListener _listener;

    public ArrayStringLongMap(KChunkListener p_listener, int initialCapacity) {
        this._listener = p_listener;
        InternalState newstate = new InternalState(initialCapacity, new String[initialCapacity], new long[initialCapacity], new int[initialCapacity], new int[initialCapacity], 0);
        for (int i = 0; i < initialCapacity; i++) {
            newstate._elementNext[i] = -1;
            newstate._elementHash[i] = -1;
        }
        this.state = new AtomicReference<InternalState>();
        this.state.set(newstate);
    }

    /**
     * Internal Map state, to be replace in a compare and swap manner
     */
    final class InternalState {

        public final int _stateSize;

        public final String[] _elementK;

        public final long[] _elementV;

        public final int[] _elementNext;

        public final int[] _elementHash;

        public final int _threshold;

        protected volatile int _elementCount;

        public InternalState(int p_stateSize, String[] p_elementK, long[] p_elementV, int[] p_elementNext, int[] p_elementHash, int p_elementCount) {
            this._stateSize = p_stateSize;
            this._elementK = p_elementK;
            this._elementV = p_elementV;
            this._elementNext = p_elementNext;
            this._elementHash = p_elementHash;
            this._elementCount = p_elementCount;
            this._threshold = (int) (p_stateSize * Constants.MAP_LOAD_FACTOR);
        }
    }

    @Override
    public final long getValue(String key) {
        InternalState internalState = state.get();
        if (internalState._stateSize == 0) {
            return Constants.NULL_LONG;
        }
        int hashIndex = PrimitiveHelper.intHash(PrimitiveHelper.stringHash(key), internalState._stateSize);
        int m = internalState._elementHash[hashIndex];
        while (m >= 0) {
            if (PrimitiveHelper.equals(key, internalState._elementK[m])) {
                return internalState._elementV[m];
            } else {
                m = internalState._elementNext[m];
            }
        }
        return Constants.NULL_LONG;
    }

    @Override
    public String getKey(long index) {
        InternalState internalState = state.get();
        if (index < internalState._stateSize) {
            return internalState._elementK[(int) index];
        }
        return null;
    }

    @Override
    public final void each(KStringLongMapCallBack callback) {
        InternalState internalState = state.get();
        for (int i = 0; i < internalState._elementCount; i++) {
            if (internalState._elementK[i] != null) { //there is a real value
                callback.on(internalState._elementK[i], internalState._elementV[i]);
            }
        }
    }

    @Override
    public int size() {
        return state.get()._elementCount;
    }

    @Override
    public void remove(String key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    @Override
    public final synchronized void put(String key, long value) {
        int entry = -1;
        int hashIndex = -1;
        InternalState internalState = state.get();
        if (internalState._stateSize > 0) {
            hashIndex = PrimitiveHelper.intHash(PrimitiveHelper.stringHash(key), internalState._stateSize);
            int m = internalState._elementHash[hashIndex];
            while (m >= 0) {
                if (PrimitiveHelper.equals(key, internalState._elementK[m])) {
                    entry = m;
                    break;
                }
                m = internalState._elementNext[m];
            }
        }
        if (entry == -1) {
            //if need to reHash (too small or too much collisions)
            if ((internalState._elementCount + 1) > internalState._threshold) {
                //rehashCapacity(state.elementDataSize);
                int newCapacity = internalState._stateSize << 1;
                String[] newElementK = new String[newCapacity];
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
                    if (internalState._elementK[i] != null) { //there is a real value
                        int newHashIndex = PrimitiveHelper.intHash(PrimitiveHelper.stringHash(internalState._elementK[i]), newCapacity);
                        int currentHashedIndex = newElementHash[newHashIndex];
                        if (currentHashedIndex != -1) {
                            newElementNext[i] = currentHashedIndex;
                        }
                        newElementHash[newHashIndex] = i;
                    }
                }
                //setPrimitiveType value for all
                internalState = new InternalState(newCapacity, newElementK, newElementV, newElementNext, newElementHash, internalState._elementCount);
                state.set(internalState);
                hashIndex = PrimitiveHelper.intHash(PrimitiveHelper.stringHash(key), internalState._stateSize);
            }
            int newIndex = internalState._elementCount;
            internalState._elementK[newIndex] = key;
            if (value == Constants.NULL_LONG) {
                internalState._elementV[newIndex] = internalState._elementCount;
            } else {
                internalState._elementV[newIndex] = value;
            }

            int currentHashedElemIndex = internalState._elementHash[hashIndex];
            if (currentHashedElemIndex != -1) {
                internalState._elementNext[newIndex] = currentHashedElemIndex;
            }
            //now the object is reachable to other thread everything should be ready
            internalState._elementHash[hashIndex] = newIndex;
            internalState._elementCount = internalState._elementCount + 1;
            _listener.declareDirty(null);
        } else {
            if (internalState._elementV[entry] != value && value != Constants.NULL_LONG) {
                //setValue
                internalState._elementV[entry] = value;
                _listener.declareDirty(null);
            }
        }
    }

    //TODO check intersection of remove and put
    /*
    @Override
    public synchronized final void remove(long key) {
        InternalState internalState = state;
        if (state.elementDataSize == 0) {
            return;
        }
        int index = ((int) (key) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = state.elementHash[index];
        int last = -1;
        while (m >= 0) {
            if (key == state.elementKV[m * 2]) {
                break;
            }
            last = m;
            m = state.elementNext[m];
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (state.elementNext[m] > 0) {
                state.elementHash[index] = m;
            } else {
                state.elementHash[index] = -1;
            }
        } else {
            state.elementNext[last] = state.elementNext[m];
        }
        state.elementNext[m] = -1;//flag to dropped value
        this.elementCount--;
        this.droppedCount++;
    }*/

    /*
    public final int size() {
        return this.elementCount;
    }*/

    /* warning: this method is not thread safe */
    /*
    @Override
    public void init(String payload) {
        //_metaClassIndex = metaClassIndex;
        if (payload == null || payload.length() == 0) {
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != '/') {
            cursor++;
        }
        if (cursor >= payload.length()) {
            return;
        }
        int nbElement = Base64.decodeToIntWithBounds(payload, initPos, cursor);
        //reset the map
        int length = (nbElement == 0 ? 1 : nbElement << 1);
        long[] newElementKV = new long[length * 2];
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //setPrimitiveType value for all
        InternalState temp_state = new InternalState(length, newElementKV, newElementNext, newElementHash);
        while (cursor < payload.length()) {
            cursor++;
            int beginChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ':') {
                cursor++;
            }
            int middleChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                cursor++;
            }
            long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
            long loopVal = Base64.decodeToLongWithBounds(payload, middleChunk + 1, cursor);
            int index = (((int) (loopKey)) & 0x7FFFFFFF) % temp_state.elementDataSize;
            //insert K/V
            int newIndex = this.elementCount;
            temp_state.elementKV[newIndex * 2] = loopKey;
            temp_state.elementKV[newIndex * 2 + 1] = loopVal;
            int currentHashedIndex = temp_state.elementHash[index];
            if (currentHashedIndex != -1) {
                temp_state.elementNext[newIndex] = currentHashedIndex;
            } else {
                temp_state.elementNext[newIndex] = -2; //special char to tag used values
            }
            temp_state.elementHash[index] = newIndex;
            this.elementCount++;
        }
        this.elementCount = nbElement;
        this.droppedCount = 0;
        this.state = temp_state;//TODO check with CnS
        this.threshold = (int) (length * loadFactor);
    }*/

    /*
    @Override
    public String save() {
        final StringBuilder buffer = new StringBuilder();//roughly approximate init size
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        InternalState internalState = state;
        for (int i = 0; i < internalState.elementNext.length; i++) {
            if (internalState.elementNext[i] != -1) { //there is a real value
                long loopKey = internalState.elementKV[i * 2];
                long loopValue = internalState.elementKV[i * 2 + 1];
                if (!isFirst) {
                    buffer.append(",");
                }
                isFirst = false;
                Base64.encodeLongToBuffer(loopKey, buffer);
                buffer.append(":");
                Base64.encodeLongToBuffer(loopValue, buffer);
            }
        }
        return buffer.toString();
    }*/

}



