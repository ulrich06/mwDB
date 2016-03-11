package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.*;
import org.mwdb.plugin.KResolver;
import org.mwdb.utility.Base64;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class HeapStateChunk implements KHeapChunk, KStateChunk, KChunkListener {

    /**
     * Identification Section
     */
    private final long _world;

    private final long _time;

    private final long _id;

    /**
     * Dirty management
     */
    private final AtomicLong _flags;

    private final AtomicInteger _counter;

    private final KChunkListener _listener;

    /**
     * Internal management
     */
    private final AtomicReference<InternalState> state;

    private boolean inLoadMode;

    @Override
    public void declareDirty(KChunk chunk) {
        if (!this.inLoadMode) {
            internal_set_dirty();
        }
    }

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

        public final int threshold;

        protected volatile int _elementCount;

        public InternalState(int elementDataSize, long[] p_elementK, Object[] p_elementV, int[] p_elementNext, int[] p_elementHash, int[] p_elementType, int p_elementCount) {
            this._elementDataSize = elementDataSize;
            this._elementK = p_elementK;
            this._elementV = p_elementV;
            this._elementNext = p_elementNext;
            this._elementHash = p_elementHash;
            this._elementType = p_elementType;
            this._elementCount = p_elementCount;
            this.threshold = (int) (_elementDataSize * Constants.MAP_LOAD_FACTOR);
        }

        public InternalState cloneState() {
            long[] clonedElementK = new long[this._elementDataSize];
            System.arraycopy(_elementK, 0, clonedElementK, 0, this._elementDataSize);
            Object[] clonedElementV = new Object[this._elementDataSize];

            //TODO warning do a deep clone, or copy on write here !!!
            System.arraycopy(_elementV, 0, clonedElementV, 0, this._elementDataSize);
            int[] clonedElementNext = new int[this._elementDataSize];
            System.arraycopy(_elementNext, 0, clonedElementNext, 0, this._elementDataSize);
            int[] clonedElementHash = new int[this._elementDataSize];
            System.arraycopy(_elementHash, 0, clonedElementHash, 0, this._elementDataSize);
            int[] clonedElementType = new int[this._elementDataSize];
            System.arraycopy(_elementType, 0, clonedElementType, 0, this._elementDataSize);
            return new InternalState(this._elementDataSize, clonedElementK, clonedElementV, clonedElementNext, clonedElementHash, clonedElementType, _elementCount);
        }
    }

    public HeapStateChunk(final long p_world, final long p_time, final long p_id, final KChunkListener p_listener, String initialPayload, KChunk origin) {
        this.inLoadMode = false;
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);
        this._listener = p_listener;
        state = new AtomicReference<InternalState>();

        if (initialPayload != null) {
            load(initialPayload);
        } else if (origin != null) {
            HeapStateChunk castedOrigin = (HeapStateChunk) origin;
            state.set(castedOrigin.state.get().cloneState());
        } else {
            //init a new state
            int initialCapacity = Constants.MAP_INITIAL_CAPACITY;
            InternalState newstate = new InternalState(initialCapacity, /* keys */new long[initialCapacity], /* values */ new Object[initialCapacity], /* next */ new int[initialCapacity], /* hash */ new int[initialCapacity], /* elemType */ new int[initialCapacity], 0);
            for (int i = 0; i < initialCapacity; i++) {
                newstate._elementNext[i] = -1;
                newstate._elementHash[i] = -1;
            }
            state.set(newstate);
        }
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
    public byte chunkType() {
        return Constants.STATE_CHUNK;
    }

    /**
     * Marks management section
     */
    @Override
    public final long marks() {
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
    public void set(final long p_elementIndex, final byte p_elemType, final Object p_unsafe_elem) {
        internal_set(p_elementIndex, p_elemType, p_unsafe_elem, true);
    }

    private synchronized void internal_set(final long p_elementIndex, final byte p_elemType, final Object p_unsafe_elem, boolean replaceIfPresent) {
        Object param_elem = null;
        //check the param type
        try {
            switch (p_elemType) {
                /** Primitives */
                case KType.BOOL:
                    param_elem = (boolean) p_unsafe_elem;
                    break;
                case KType.DOUBLE:
                    param_elem = (double) p_unsafe_elem;
                    break;
                case KType.LONG:
                    param_elem = (long) p_unsafe_elem;
                    break;
                case KType.INT:
                    param_elem = (int) p_unsafe_elem;
                    break;
                case KType.STRING:
                    param_elem = (String) p_unsafe_elem;
                    break;
                /** Arrays */
                case KType.DOUBLE_ARRAY:
                    param_elem = (double[]) p_unsafe_elem;
                    break;
                case KType.LONG_ARRAY:
                    param_elem = (long[]) p_unsafe_elem;
                    break;
                case KType.INT_ARRAY:
                    param_elem = (int[]) p_unsafe_elem;
                    break;
                /** Maps */

                case KType.STRING_LONG_MAP:
                    param_elem = (KStringLongMap) p_unsafe_elem;
                    break;
                case KType.LONG_LONG_MAP:
                    param_elem = (KLongLongMap) p_unsafe_elem;
                    break;
                case KType.LONG_LONG_ARRAY_MAP:
                    param_elem = (KLongLongArrayMap) p_unsafe_elem;
                    break;

                default:
                    throw new RuntimeException("mwDB usage error, set method called with an unknown type " + p_elemType);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException("mwDB usage error, set method called with type " + p_elemType + " while param object is " + param_elem);
        }
        int entry = -1;
        InternalState internalState = state.get();
        int hashIndex = -1;
        if (internalState._elementDataSize > 0) {
            hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
            int m = internalState._elementHash[hashIndex];
            while (m != -1) {
                if (p_elementIndex == internalState._elementK[m]) {
                    entry = m;
                    break;
                }
                m = internalState._elementNext[m];
            }
        }
        if (entry == -1) {
            if (internalState._elementCount + 1 > internalState.threshold) {
                int newLength = (internalState._elementDataSize == 0 ? 1 : internalState._elementDataSize << 1);
                long[] newElementK = new long[newLength];
                Object[] newElementV = new Object[newLength];
                int[] newElementType = new int[newLength];
                System.arraycopy(internalState._elementK, 0, newElementK, 0, internalState._elementDataSize);
                System.arraycopy(internalState._elementV, 0, newElementV, 0, internalState._elementDataSize);
                System.arraycopy(internalState._elementType, 0, newElementType, 0, internalState._elementDataSize);
                int[] newElementNext = new int[newLength];
                int[] newElementHash = new int[newLength];
                for (int i = 0; i < newLength; i++) {
                    newElementNext[i] = -1;
                    newElementHash[i] = -1;
                }
                //rehashEveryThing
                for (int i = 0; i < internalState._elementV.length; i++) {
                    if (internalState._elementV[i] != null) { //there is a real value
                        int keyHash = (int) PrimitiveHelper.longHash(internalState._elementK[i], newLength);
                        int currentHashedIndex = newElementHash[keyHash];
                        if (currentHashedIndex != -1) {
                            newElementNext[i] = currentHashedIndex;
                        }
                        newElementHash[keyHash] = i;
                    }
                }
                //setPrimitiveType value for all
                internalState = new InternalState(newLength, newElementK, newElementV, newElementNext, newElementHash, newElementType, internalState._elementCount);
                this.state.set(internalState);
                hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
            }
            int newIndex = internalState._elementCount;
            internalState._elementCount = internalState._elementCount + 1;
            internalState._elementK[newIndex] = p_elementIndex;
            internalState._elementV[newIndex] = param_elem;
            internalState._elementType[newIndex] = p_elemType;
            int currentHashedIndex = internalState._elementHash[hashIndex];
            if (currentHashedIndex != -1) {
                internalState._elementNext[newIndex] = currentHashedIndex;
            }
            //now the object is reachable to other thread everything should be ready
            internalState._elementHash[hashIndex] = newIndex;
        } else {
            if (replaceIfPresent) {
                internalState._elementV[entry] = param_elem;/*setValue*/
                internalState._elementType[entry] = p_elemType;
            }
        }
        internal_set_dirty();
    }

    @Override
    public Object get(long p_elementIndex) {
        InternalState internalState = state.get();
        if (internalState._elementDataSize == 0) {
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
    public int getType(long p_elementIndex) {
        InternalState internalState = state.get();
        if (internalState._elementDataSize == 0) {
            return -1;
        }
        int hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
        int m = internalState._elementHash[hashIndex];
        while (m >= 0) {
            if (p_elementIndex == internalState._elementK[m] /* getKey */) {
                return internalState._elementType[m]; /* getValue */
            } else {
                m = internalState._elementNext[m];
            }
        }
        return -1;
    }


    @Override
    public Object getOrCreate(long p_elementIndex, byte elemType) {
        Object previousObject = get(p_elementIndex);
        if (previousObject != null) {
            return previousObject;
        }
        switch (elemType) {
            case KType.STRING_LONG_MAP:
                internal_set(p_elementIndex, elemType, new ArrayStringLongMap(this, Constants.MAP_INITIAL_CAPACITY), false);
                break;
            case KType.LONG_LONG_MAP:
                internal_set(p_elementIndex, elemType, new ArrayLongLongMap(this, Constants.MAP_INITIAL_CAPACITY), false);
                break;
            case KType.LONG_LONG_ARRAY_MAP:
                internal_set(p_elementIndex, elemType, new ArrayLongLongArrayMap(this, Constants.MAP_INITIAL_CAPACITY), false);
                break;
        }
        return get(p_elementIndex);
    }

    @Override
    public void each(KStateChunkCallBack callBack, KResolver resolver) {
        InternalState currentState = this.state.get();
        for (int i = 0; i < (currentState._elementCount); i++) {
            if (currentState._elementV[i] != null) {
                callBack.on(resolver.longKeyToString(currentState._elementK[i]), currentState._elementType[i], currentState._elementV[i]);
            }
        }
    }

    private void load(String payload) {
        if (payload == null || payload.length() == 0) {
            return;
        }
        inLoadMode = true;
        //future map elements
        long[] newElementK = null;
        Object[] newElementV = null;
        int[] newElementType = null;
        int[] newElementNext = null;
        int[] newElementHash = null;
        int newNumberElement = 0;
        int newStateCapacity = 0;
        //reset size
        int currentElemIndex = 0;

        int cursor = 0;
        int payloadSize = payload.length();

        int previousStart = -1;
        long currentChunkElemKey = Constants.NULL_LONG;
        int currentChunkElemType = -1;

        //init detections
        boolean isFirstElem = true;

        //array sub creation variable
        double[] currentDoubleArr = null;
        long[] currentLongArr = null;
        int[] currentIntArr = null;

        //map sub creation variables
        KStringLongMap currentStringLongMap = null;
        KLongLongMap currentLongLongMap = null;
        KLongLongArrayMap currentLongLongArrayMap = null;

        //array variables
        long currentSubSize = -1;
        int currentSubIndex = 0;

        //map key variables
        long currentMapLongKey = Constants.NULL_LONG;
        String currentMapStringKey = null;

        while (cursor < payloadSize) {
            if (payload.charAt(cursor) == Constants.CHUNK_SEP) {
                if (isFirstElem) {
                    //initial the map
                    isFirstElem = false;
                    int stateChunkSize = Base64.decodeToIntWithBounds(payload, 0, cursor);
                    newNumberElement = stateChunkSize;
                    int newStateChunkSize = (stateChunkSize == 0 ? 1 : stateChunkSize << 1);
                    //init map element
                    newElementK = new long[newStateChunkSize];
                    newElementV = new Object[newStateChunkSize];
                    newElementType = new int[newStateChunkSize];
                    newStateCapacity = newStateChunkSize;
                    //init hash and chaining
                    newElementNext = new int[newStateChunkSize];
                    newElementHash = new int[newStateChunkSize];
                    for (int i = 0; i < newStateChunkSize; i++) {
                        newElementNext[i] = -1;
                        newElementHash[i] = -1;
                    }
                    previousStart = cursor + 1;
                } else {
                    //beginning of the Chunk elem
                    //check if something is still in buffer
                    if (currentChunkElemType != -1) {
                        Object toInsert = null;
                        switch (currentChunkElemType) {
                            /** Primitive Object */
                            case KType.BOOL:
                                if (payload.charAt(previousStart) == '0') {
                                    toInsert = false;
                                } else if (payload.charAt(previousStart) == '1') {
                                    toInsert = true;
                                }
                                break;
                            case KType.STRING:
                                toInsert = Base64.decodeToStringWithBounds(payload, previousStart, cursor);
                                break;

                            case KType.DOUBLE:
                                toInsert = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                                break;

                            case KType.LONG:
                                toInsert = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                                break;

                            case KType.INT:
                                toInsert = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                                break;
                            /** Arrays */
                            case KType.DOUBLE_ARRAY:
                                currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                                toInsert = currentDoubleArr;
                                break;

                            case KType.LONG_ARRAY:
                                currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                                toInsert = currentLongArr;
                                break;

                            case KType.INT_ARRAY:
                                currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                                toInsert = currentIntArr;
                                break;
                            /** Maps */
                            case KType.STRING_LONG_MAP:
                                if (currentMapStringKey != null) {
                                    currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                }
                                toInsert = currentStringLongMap;
                                break;
                            case KType.LONG_LONG_MAP:
                                if (currentMapLongKey != Constants.NULL_LONG) {
                                    currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                }
                                toInsert = currentLongLongMap;
                                break;
                            case KType.LONG_LONG_ARRAY_MAP:
                                if (currentMapLongKey != Constants.NULL_LONG) {
                                    currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                }
                                toInsert = currentLongLongArrayMap;
                                break;
                        }
                        if (toInsert != null) {
                            //insert K/V
                            int newIndex = currentElemIndex;
                            newElementK[newIndex] = currentChunkElemKey;
                            newElementV[newIndex] = toInsert;
                            newElementType[newIndex] = currentChunkElemType;

                            int hashIndex = (int) PrimitiveHelper.longHash(currentChunkElemKey, newStateCapacity);
                            int currentHashedIndex = newElementHash[hashIndex];
                            if (currentHashedIndex != -1) {
                                newElementNext[newIndex] = currentHashedIndex;
                            }
                            newElementHash[hashIndex] = newIndex;
                            currentElemIndex++;
                        }
                    }
                    //next round, reset all variables...
                    previousStart = cursor + 1;
                    currentChunkElemKey = Constants.NULL_LONG;
                    currentChunkElemType = -1;
                    currentSubSize = -1;
                    currentSubIndex = 0;
                    currentMapLongKey = Constants.NULL_LONG;
                    currentMapStringKey = null;
                }
            } else if (payload.charAt(cursor) == Constants.CHUNK_SUB_SEP) { //SEPARATION BETWEEN KEY,TYPE,VALUE
                if (currentChunkElemKey == Constants.NULL_LONG) {
                    currentChunkElemKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    previousStart = cursor + 1;
                } else if (currentChunkElemType == -1) {
                    currentChunkElemType = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                    previousStart = cursor + 1;
                }
            } else if (payload.charAt(cursor) == Constants.CHUNK_SUB_SUB_SEP) { //SEPARATION BETWEEN ARRAY VALUES AND MAP KEY/VALUE TUPLES
                if (currentSubSize == -1) {
                    currentSubSize = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    //init array or maps
                    switch (currentChunkElemType) {
                        /** Arrays */
                        case KType.DOUBLE_ARRAY:
                            currentDoubleArr = new double[(int) currentSubSize];
                            break;
                        case KType.LONG_ARRAY:
                            currentLongArr = new long[(int) currentSubSize];
                            break;
                        case KType.INT_ARRAY:
                            currentIntArr = new int[(int) currentSubSize];
                            break;
                        /** Maps */
                        case KType.STRING_LONG_MAP:
                            currentStringLongMap = new ArrayStringLongMap(this, (int) currentSubSize);
                            break;
                        case KType.LONG_LONG_MAP:
                            currentLongLongMap = new ArrayLongLongMap(this, (int) currentSubSize);
                            break;
                        case KType.LONG_LONG_ARRAY_MAP:
                            currentLongLongArrayMap = new ArrayLongLongArrayMap(this, (int) currentSubSize);
                            break;
                    }
                } else {
                    switch (currentChunkElemType) {
                        /** Arrays */
                        case KType.DOUBLE_ARRAY:
                            currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        case KType.LONG_ARRAY:
                            currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        case KType.INT_ARRAY:
                            currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        /** Maps */
                        case KType.STRING_LONG_MAP:
                            if (currentMapStringKey != null) {
                                currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                currentMapStringKey = null;
                            }
                            break;
                        case KType.LONG_LONG_MAP:
                            if (currentMapLongKey != Constants.NULL_LONG) {
                                currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                currentMapLongKey = Constants.NULL_LONG;
                            }
                            break;
                        case KType.LONG_LONG_ARRAY_MAP:
                            if (currentMapLongKey != Constants.NULL_LONG) {
                                currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                currentMapLongKey = Constants.NULL_LONG;
                            }
                            break;
                    }
                }
                previousStart = cursor + 1;
            } else if (payload.charAt(cursor) == Constants.CHUNK_SUB_SUB_SUB_SEP) {
                switch (currentChunkElemType) {
                    case KType.STRING_LONG_MAP:
                        if (currentMapStringKey == null) {
                            currentMapStringKey = Base64.decodeToStringWithBounds(payload, previousStart, cursor);
                        } else {
                            currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                            //reset key for next loop
                            currentMapStringKey = null;
                        }
                        break;
                    case KType.LONG_LONG_MAP:
                        if (currentMapLongKey == Constants.NULL_LONG) {
                            currentMapLongKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                        } else {
                            currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                            //reset key for next loop
                            currentMapLongKey = Constants.NULL_LONG;
                        }
                        break;
                    case KType.LONG_LONG_ARRAY_MAP:
                        if (currentMapLongKey == Constants.NULL_LONG) {
                            currentMapLongKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                        } else {
                            currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                            //reset key for next loop
                            currentMapLongKey = Constants.NULL_LONG;
                        }
                        break;
                }
                previousStart = cursor + 1;
            }
            cursor++;
        }

        //take the last element
        if (currentChunkElemType != -1) {
            Object toInsert = null;
            switch (currentChunkElemType) {
                /** Primitive Object */
                case KType.BOOL:
                    if (payload.charAt(previousStart) == '0') {
                        toInsert = false;
                    } else if (payload.charAt(previousStart) == '1') {
                        toInsert = true;
                    }
                    break;
                case KType.STRING:
                    toInsert = Base64.decodeToStringWithBounds(payload, previousStart, cursor);
                    break;
                case KType.DOUBLE:
                    toInsert = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                    break;
                case KType.LONG:
                    toInsert = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    break;
                case KType.INT:
                    toInsert = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                    break;
                /** Arrays */
                case KType.DOUBLE_ARRAY:
                    currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                    toInsert = currentDoubleArr;
                    break;
                case KType.LONG_ARRAY:
                    currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    toInsert = currentLongArr;
                    break;
                case KType.INT_ARRAY:
                    currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                    toInsert = currentIntArr;
                    break;
                /** Maps */
                case KType.STRING_LONG_MAP:
                    if (currentMapStringKey != null) {
                        currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                    }
                    toInsert = currentStringLongMap;
                    break;
                case KType.LONG_LONG_MAP:
                    if (currentMapLongKey != Constants.NULL_LONG) {
                        currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                    }
                    toInsert = currentLongLongMap;
                    break;
                case KType.LONG_LONG_ARRAY_MAP:
                    if (currentMapLongKey != Constants.NULL_LONG) {
                        currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                    }
                    toInsert = currentLongLongArrayMap;
                    break;

            }
            if (toInsert != null) {
                //insert K/V
                newElementK[currentElemIndex] = currentChunkElemKey;
                newElementV[currentElemIndex] = toInsert;
                newElementType[currentElemIndex] = currentChunkElemType;

                int hashIndex = (int) PrimitiveHelper.longHash(currentChunkElemKey, newStateCapacity);
                int currentHashedIndex = newElementHash[hashIndex];
                if (currentHashedIndex != -1) {
                    newElementNext[currentElemIndex] = currentHashedIndex;
                }
                newElementHash[hashIndex] = currentElemIndex;
            }
        }
        //set the state
        InternalState newState = new InternalState(newStateCapacity, newElementK, newElementV, newElementNext, newElementHash, newElementType, newNumberElement);
        this.state.set(newState);
        this.inLoadMode = false;
    }

    @Override
    public String save() {
        final StringBuilder buffer = new StringBuilder();
        final InternalState internalState = state.get();
        Base64.encodeIntToBuffer(internalState._elementCount, buffer);
        for (int i = 0; i < internalState._elementCount; i++) {
            if (internalState._elementV[i] != null) { //there is a real value
                long loopKey = internalState._elementK[i];
                Object loopValue = internalState._elementV[i];
                if (loopValue != null) {
                    buffer.append(Constants.CHUNK_SEP);
                    Base64.encodeLongToBuffer(loopKey, buffer);
                    buffer.append(Constants.CHUNK_SUB_SEP);
                    /** Encode to type of elem, for unSerialization */
                    Base64.encodeIntToBuffer(internalState._elementType[i], buffer);
                    buffer.append(Constants.CHUNK_SUB_SEP);
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
                                buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                                Base64.encodeDoubleToBuffer(castedDoubleArr[j], buffer);
                            }
                            break;
                        case KType.LONG_ARRAY:
                            long[] castedLongArr = (long[]) loopValue;
                            Base64.encodeIntToBuffer(castedLongArr.length, buffer);
                            for (int j = 0; j < castedLongArr.length; j++) {
                                buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                                Base64.encodeLongToBuffer(castedLongArr[j], buffer);
                            }
                            break;
                        case KType.INT_ARRAY:
                            int[] castedIntArr = (int[]) loopValue;
                            Base64.encodeIntToBuffer(castedIntArr.length, buffer);
                            for (int j = 0; j < castedIntArr.length; j++) {
                                buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                                Base64.encodeIntToBuffer(castedIntArr[j], buffer);
                            }
                            break;
                        /** Maps */
                        case KType.STRING_LONG_MAP:
                            KStringLongMap castedStringLongMap = (KStringLongMap) loopValue;
                            Base64.encodeLongToBuffer(castedStringLongMap.size(), buffer);
                            castedStringLongMap.each(new KStringLongMapCallBack() {
                                @Override
                                public void on(final String key, final long value) {
                                    buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                                    Base64.encodeStringToBuffer(key, buffer);
                                    buffer.append(Constants.CHUNK_SUB_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(value, buffer);
                                }
                            });
                            break;
                        case KType.LONG_LONG_MAP:
                            KLongLongMap castedLongLongMap = (KLongLongMap) loopValue;
                            Base64.encodeLongToBuffer(castedLongLongMap.size(), buffer);
                            castedLongLongMap.each(new KLongLongMapCallBack() {
                                @Override
                                public void on(final long key, final long value) {
                                    buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(key, buffer);
                                    buffer.append(Constants.CHUNK_SUB_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(value, buffer);
                                }
                            });
                            break;
                        case KType.LONG_LONG_ARRAY_MAP:
                            KLongLongArrayMap castedLongLongArrayMap = (KLongLongArrayMap) loopValue;
                            Base64.encodeLongToBuffer(castedLongLongArrayMap.size(), buffer);
                            castedLongLongArrayMap.each(new KLongLongArrayMapCallBack() {
                                @Override
                                public void on(final long key, final long value) {
                                    buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(key, buffer);
                                    buffer.append(Constants.CHUNK_SUB_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(value, buffer);
                                }
                            });
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return buffer.toString();
    }

    private void internal_set_dirty() {
        if (this._listener != null) {
            if ((_flags.get() & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                this._listener.declareDirty(this);
            }
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
