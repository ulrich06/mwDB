package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStateChunk;
import org.mwdb.chunk.KStateChunkCallBack;
import org.mwdb.plugin.KResolver;
import org.mwdb.utility.Base64;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HeapStateChunk implements KStateChunk {

    private static final float loadFactor = ((float) 75 / (float) 100);

    /**
     * volatile zone
     */
    protected volatile int elementCount;

    protected volatile int droppedCount;

    protected volatile InternalState state = null;

    /** */
    protected int threshold;

    private final int initialCapacity = 16;

    private final AtomicLong _flags;

    private final AtomicInteger _counter;

    private final KChunkListener _listener;

    /**
     * Identification Section
     */
    private final long _world;

    private final long _time;

    private final long _id;

    /**
     * Internal state for atomic change
     */
    final class InternalState {

        public final int _elementDataSize;

        public final long[] _elementK;

        public final Object[] _elementV;

        public final int[] _elementNext;

        public final int[] _elementHash;

        public final int[] _elementType;

        public InternalState(int elementDataSize, long[] p_elementK, Object[] p_elementV, int[] p_elementNext, int[] p_elementHash, int[] p_elementType) {
            this._elementDataSize = elementDataSize;
            this._elementK = p_elementK;
            this._elementV = p_elementV;
            this._elementNext = p_elementNext;
            this._elementHash = p_elementHash;
            this._elementType = p_elementType;
        }

        public InternalState cloneState() {
            long[] clonedElementK = new long[_elementK.length];
            System.arraycopy(_elementK, 0, clonedElementK, 0, _elementK.length);
            Object[] clonedElementV = new Object[_elementV.length];
            System.arraycopy(_elementV, 0, clonedElementV, 0, _elementV.length);
            int[] clonedElementNext = new int[_elementNext.length];
            System.arraycopy(_elementNext, 0, clonedElementNext, 0, _elementNext.length);
            int[] clonedElementHash = new int[_elementHash.length];
            System.arraycopy(_elementHash, 0, clonedElementHash, 0, _elementHash.length);
            int[] clonedElementType = new int[_elementType.length];
            System.arraycopy(_elementType, 0, clonedElementType, 0, clonedElementType.length);
            return new InternalState(_elementDataSize, clonedElementK, clonedElementV, clonedElementNext, clonedElementHash, clonedElementType);
        }
    }

    public HeapStateChunk(long p_world, long p_time, long p_id, KChunkListener p_listener) {
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);
        this._listener = p_listener;
        this.elementCount = 0;
        this.droppedCount = 0;
        InternalState newstate = new InternalState(initialCapacity, /* keys */new long[initialCapacity], /* values */ new Object[initialCapacity], /* next */ new int[initialCapacity], /* hash */ new int[initialCapacity], /* elemType */ new int[initialCapacity]);
        for (int i = 0; i < initialCapacity; i++) {
            newstate._elementNext[i] = -1;
            newstate._elementHash[i] = -1;
        }
        this.state = newstate;
        this.threshold = (int) (newstate._elementDataSize * loadFactor);
    }

    /**
     * Identification management
     */
    @Override
    public long world() {
        return this._world;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long id() {
        return this._id;
    }

    @Override
    public short chunkType() {
        return Constants.INDEX_STATE_CHUNK;
    }

    /**
     * Marks management section
     */
    @Override
    public final int marks() {
        return this._counter.get();
    }

    @Override
    public final int mark() {
        return this._counter.incrementAndGet();
    }

    @Override
    public final int unmark() {
        return this._counter.decrementAndGet();
    }

    @Override
    public synchronized void set(long p_elementIndex, int p_elemType, Object p_elem) {
        int entry = -1;
        InternalState internalState = state;
        int hashIndex = -1;
        if (internalState._elementDataSize != 0) {
            hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
            entry = findNonNullKeyEntry(p_elementIndex, hashIndex);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(state._elementDataSize);
                hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, state._elementDataSize);
            }
            int newIndex = (this.elementCount + this.droppedCount - 1);
            state._elementK[newIndex] = p_elementIndex;
            state._elementV[newIndex] = p_elem;
            state._elementType[newIndex] = p_elemType;
            int currentHashedIndex = state._elementHash[hashIndex];
            if (currentHashedIndex != -1) {
                state._elementNext[newIndex] = currentHashedIndex;
            } else {
                state._elementNext[newIndex] = -2; //special char to tag used values
            }
            //now the object is reachable to other thread everything should be ready
            state._elementHash[hashIndex] = newIndex;
        } else {
            state._elementV[entry] = p_elem;/*setValue*/
            state._elementType[entry] = p_elemType;
        }
        internal_set_dirty();
    }

    @Override
    public Object get(long p_elementIndex) {
        InternalState internalState = state;
        if (state._elementDataSize == 0) {
            return null;
        }
        int hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
        int m = internalState._elementHash[hashIndex];
        while (m >= 0) {
            if (p_elementIndex == internalState._elementK[m] /* getKey */) {
                return internalState._elementV[m]; /* getValue */
            } else {
                m = internalState._elementNext[m];
            }
        }
        return null;
    }

    @Override
    public void each(KStateChunkCallBack callBack, KResolver resolver) {
        InternalState currentState = this.state;
        for (int i = 0; i < (this.elementCount + this.droppedCount); i++) {
            if (currentState._elementV[i] != null) {
                callBack.on(resolver.value(currentState._elementK[i]), currentState._elementType[i], currentState._elementV[i]);
            }
        }
    }

    @Override
    public void cloneFrom(KStateChunk origin) {
        //brutal cast, but mixed implementation is not allowed per space
        HeapStateChunk casted = (HeapStateChunk) origin;
        casted.state = this.state.cloneState();
        casted.elementCount = this.elementCount;
        casted.droppedCount = this.droppedCount;
        casted.threshold = this.threshold;
        setFlags(Constants.DIRTY_BIT, 0);
    }

    protected final void rehashCapacity(int capacity) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        long[] newElementK = new long[length * 2];
        Object[] newElementV = new Object[length * 2];
        int[] newElementType = new int[length * 2];
        System.arraycopy(state._elementK, 0, newElementK, 0, state._elementK.length);
        System.arraycopy(state._elementV, 0, newElementV, 0, state._elementV.length);
        System.arraycopy(state._elementType, 0, newElementType, 0, state._elementType.length);
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < state._elementV.length; i++) {
            if (state._elementV[i] != null) { //there is a real value
                int keyHash = (int) PrimitiveHelper.longHash(state._elementK[i], length);
                //int index = (PrimitiveHelper.stringHash(state._elementK[i]) & 0x7FFFFFFF) % length;
                int currentHashedIndex = newElementHash[keyHash];
                if (currentHashedIndex != -1) {
                    newElementNext[i] = currentHashedIndex;
                }
                newElementHash[keyHash] = i;
            }
        }
        //setPrimitiveType value for all
        state = new InternalState(length, newElementK, newElementV, newElementNext, newElementHash, newElementType);
        this.threshold = (int) (length * loadFactor);
    }

    final int findNonNullKeyEntry(long key, int keyHash) {
        int m = state._elementHash[keyHash];
        while (m >= 0) {
            if (key == state._elementK[m] /* getKey */) {
                return m;
            }
            m = state._elementNext[m];
        }
        return -1;
    }

    //TODO check intersection of remove and put
    /*
    @Override
    public synchronized final void remove(String key) {
        InternalState internalState = state;
        if (state.elementDataSize == 0) {
            return;
        }
        int index = (PrimitiveHelper.stringHash(key) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = state.elementHash[index];
        int last = -1;
        while (m >= 0) {
            if (PrimitiveHelper.equals(key, state.elementK[m])) {
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
    }

    public final int size() {
        return this.elementCount;
    }*/

    /* warning: this method is not thread safe */
    @Override
    public void init(String payload) {
        if (payload == null || payload.length() == 0) {
            return;
        }
        int initPos = 1;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != '/') {
            cursor++;
        }
        int nbElement = Base64.decodeToIntWithBounds(payload, initPos, cursor);
        //reset the map
        int length = (nbElement == 0 ? 1 : nbElement << 1);
        long[] newElementK = new long[length];
        Object[] newElementV = new Object[length];
        int[] newElementType = new int[length];
        //hash and chaining
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //setPrimitiveType value for all
        InternalState temp_state = new InternalState(length, newElementK, newElementV, newElementNext, newElementHash, newElementType);
        while (cursor < payload.length()) {
            cursor++;
            int beginChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ':') {
                cursor++;
            }
            int middleChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ':') {
                cursor++;
            }
            int payloadChunk = cursor;
            while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                cursor++;
            }
            long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, middleChunk);
            int loopType = Base64.decodeToIntWithBounds(payload, middleChunk + 1, payloadChunk);
            String loopVal = Base64.decodeToStringWithBounds(payload, payloadChunk + 1, cursor);

            //TODO
            int index = -1;
            // int index = (PrimitiveHelper.stringHash(loopKey) & 0x7FFFFFFF) % temp_state._elementDataSize;
            //insert K/V
            int newIndex = this.elementCount;
            temp_state._elementK[newIndex] = loopKey;
            temp_state._elementType[newIndex] = loopType;
            temp_state._elementV[newIndex] = loopVal;

            int currentHashedIndex = temp_state._elementHash[index];
            if (currentHashedIndex != -1) {
                temp_state._elementNext[newIndex] = currentHashedIndex;
            } else {
                temp_state._elementNext[newIndex] = -2; //special char to tag used values
            }
            temp_state._elementHash[index] = newIndex;
            this.elementCount++;
        }
        this.elementCount = nbElement;
        this.droppedCount = 0;
        this.state = temp_state;//TODO check with CnS
        this.threshold = (int) (length * loadFactor);
    }

    @Override
    public String serialize() {
        final StringBuilder buffer = new StringBuilder();
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.append('/');
        boolean isFirst = true;
        InternalState internalState = state;
        for (int i = 0; i < internalState._elementNext.length; i++) {
            if (internalState._elementNext[i] != -1) { //there is a real value
                long loopKey = internalState._elementK[i];
                Object loopValue = internalState._elementV[i];
                if (loopValue != null) {
                    if (!isFirst) {
                        buffer.append(",");
                    }
                    isFirst = false;
                    Base64.encodeLongToBuffer(loopKey, buffer);
                    buffer.append(":");
                    /** Encode to type of elem, for unSerialization */
                    Base64.encodeIntToBuffer(internalState._elementType[i], buffer);
                    buffer.append(":");
                    switch (internalState._elementType[i]) {
                        /** Primitive Types */
                        case KType.STRING:
                            Base64.encodeStringToBuffer((String) loopValue, buffer);
                            break;
                        case KType.BOOL:
                            if ((boolean) internalState._elementV[i]) {
                                buffer.append("1");
                            } else {
                                buffer.append("0");
                            }
                            break;
                        case KType.LONG:
                            Base64.encodeLongToBuffer((long) loopValue, buffer);
                            break;
                        case KType.DOUBLE:
                            Base64.encodeDoubleToBuffer((double) loopValue, buffer);
                            break;
                        case KType.INT:
                            Base64.encodeIntToBuffer((int) loopValue, buffer);
                            break;
                        /** Arrays */
                        case KType.DOUBLE_ARRAY:
                            double[] castedDoubleArr = (double[]) loopValue;
                            Base64.encodeIntToBuffer(castedDoubleArr.length, buffer);
                            for (int j = 0; j < castedDoubleArr.length; j++) {
                                buffer.append(Constants.CHUNK_VAL_SEP);
                                Base64.encodeDoubleToBuffer(castedDoubleArr[j], buffer);
                            }
                            break;
                        case KType.LONG_ARRAY:
                            long[] castedLongArr = (long[]) loopValue;
                            Base64.encodeIntToBuffer(castedLongArr.length, buffer);
                            for (int j = 0; j < castedLongArr.length; j++) {
                                buffer.append(Constants.CHUNK_VAL_SEP);
                                Base64.encodeDoubleToBuffer(castedLongArr[j], buffer);
                            }
                            break;
                        case KType.INT_ARRAY:
                            long[] castedIntArr = (long[]) loopValue;
                            Base64.encodeIntToBuffer(castedIntArr.length, buffer);
                            for (int j = 0; j < castedIntArr.length; j++) {
                                buffer.append(Constants.CHUNK_VAL_SEP);
                                Base64.encodeDoubleToBuffer(castedIntArr[j], buffer);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return buffer.toString();
    }

    @Override
    public void free() {
        // clear();
    }

    private void internal_set_dirty() {
        if (this._listener != null) {
            if ((_flags.get() & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                //the synchronization risk is minim here, at worse the object will be saved twice for the next iteration
                setFlags(Constants.DIRTY_BIT, 0);
                this._listener.declareDirty(this);
            }
        } else {
            setFlags(Constants.DIRTY_BIT, 0);
        }
    }

    /**
     * Flags management section
     */
    @Override
    public long flags() {
        return _flags.get();
    }

    @Override
    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!_flags.compareAndSet(val, nval));
    }
}
