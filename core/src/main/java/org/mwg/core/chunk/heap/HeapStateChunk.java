package org.mwg.core.chunk.heap;

import org.mwg.Type;
import org.mwg.core.CoreConstants;
import org.mwg.Graph;
import org.mwg.plugin.*;
import org.mwg.struct.*;
import org.mwg.core.chunk.*;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

public class HeapStateChunk implements HeapChunk, StateChunk, ChunkListener {

    /**
     * @ignore ts
     */
    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    private final long _world;
    private final long _time;
    private final long _id;

    private volatile InternalState state;
    private volatile long _flags;
    private volatile long _marks;

    /**
     * @ignore ts
     */
    private static final long _flagsOffset;
    /**
     * @ignore ts
     */
    private static final long _marksOffset;

    /** @ignore ts */
    static {
        try {
            _flagsOffset = unsafe.objectFieldOffset(HeapStateChunk.class.getDeclaredField("_flags"));
            _marksOffset = unsafe.objectFieldOffset(HeapStateChunk.class.getDeclaredField("_marks"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private final ChunkListener _space;
    private boolean inLoadMode;

    @Override
    public final void declareDirty(Chunk chunk) {
        if (!this.inLoadMode) {
            internal_set_dirty();
        }
    }

    @Override
    public final Graph graph() {
        return _space.graph();
    }

    private final class InternalState {

        final int _elementDataSize;

        final long[] _elementK;

        final Object[] _elementV;

        final int[] _elementNext;

        final int[] _elementHash;

        final byte[] _elementType;

        final int threshold;

        volatile int _elementCount;

        boolean hashReadOnly;

        InternalState(int elementDataSize, long[] p_elementK, Object[] p_elementV, int[] p_elementNext, int[] p_elementHash, byte[] p_elementType, int p_elementCount, boolean p_hashReadOnly) {
            this.hashReadOnly = p_hashReadOnly;
            this._elementDataSize = elementDataSize;
            this._elementK = p_elementK;
            this._elementV = p_elementV;
            this._elementNext = p_elementNext;
            this._elementHash = p_elementHash;
            this._elementType = p_elementType;
            this._elementCount = p_elementCount;
            this.threshold = (int) (_elementDataSize * CoreConstants.MAP_LOAD_FACTOR);
        }

        InternalState deepClone() {
            long[] clonedElementK = new long[this._elementDataSize];
            System.arraycopy(_elementK, 0, clonedElementK, 0, this._elementDataSize);
            int[] clonedElementNext = new int[this._elementDataSize];
            System.arraycopy(_elementNext, 0, clonedElementNext, 0, this._elementDataSize);
            int[] clonedElementHash = new int[this._elementDataSize];
            System.arraycopy(_elementHash, 0, clonedElementHash, 0, this._elementDataSize);
            byte[] clonedElementType = new byte[this._elementDataSize];
            System.arraycopy(_elementType, 0, clonedElementType, 0, this._elementDataSize);
            return new InternalState(this._elementDataSize, clonedElementK, _elementV /* considered as safe because came fromVar a softClone */, clonedElementNext, clonedElementHash, clonedElementType, _elementCount, false);
        }

        InternalState softClone() {
            Object[] clonedElementV = new Object[this._elementDataSize];
            System.arraycopy(_elementV, 0, clonedElementV, 0, this._elementDataSize);
            return new InternalState(this._elementDataSize, _elementK, clonedElementV, _elementNext, _elementHash, _elementType, _elementCount, true);
        }
    }

    public HeapStateChunk(final long p_world, final long p_time, final long p_id, final ChunkListener p_space, Buffer initialPayload, Chunk origin) {
        this.inLoadMode = false;
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._flags = 0;
        this._marks = 0;
        this._space = p_space;
        if (initialPayload != null && initialPayload.length() > 0) {
            load(initialPayload, false);
        } else if (origin != null) {
            HeapStateChunk castedOrigin = (HeapStateChunk) origin;
            InternalState clonedState = castedOrigin.state.softClone();
            state = clonedState;
            //deep clone for map
            for (int i = 0; i < clonedState._elementCount; i++) {
                switch (clonedState._elementType[i]) {
                    case Type.LONG_TO_LONG_MAP:
                        if (clonedState._elementV[i] != null) {
                            clonedState._elementV[i] = new ArrayLongLongMap(this, -1, (ArrayLongLongMap) clonedState._elementV[i]);
                        }
                        break;
                    case Type.LONG_TO_LONG_ARRAY_MAP:
                        if (clonedState._elementV[i] != null) {
                            clonedState._elementV[i] = new ArrayLongLongArrayMap(this, -1, (ArrayLongLongArrayMap) clonedState._elementV[i]);
                        }
                        break;
                    case Type.STRING_TO_LONG_MAP:
                        if (clonedState._elementV[i] != null) {
                            clonedState._elementV[i] = new ArrayStringLongMap(this, -1, (ArrayStringLongMap) clonedState._elementV[i]);
                        }
                        break;
                }
            }

        } else {
            //init a new state
            int initialCapacity = CoreConstants.MAP_INITIAL_CAPACITY;
            InternalState newstate = new InternalState(initialCapacity, /* keys */new long[initialCapacity], /* values */ new Object[initialCapacity], /* next */ new int[initialCapacity], /* hash */ new int[initialCapacity], /* elemType */ new byte[initialCapacity], 0, false);
            for (int i = 0; i < initialCapacity; i++) {
                newstate._elementNext[i] = -1;
                newstate._elementHash[i] = -1;
            }
            state = newstate;
        }
    }

    @Override
    public final long world() {
        return this._world;
    }

    @Override
    public final long time() {
        return this._time;
    }

    @Override
    public final long id() {
        return this._id;
    }

    @Override
    public final byte chunkType() {
        return ChunkType.STATE_CHUNK;
    }

    @Override
    public final long marks() {
        return this._marks;
    }

    /**
     * @native ts
     * this._marks = this._marks + 1;
     * return this._marks
     */
    @Override
    public final long mark() {
        long before;
        long after;
        do {
            before = _marks;
            after = before + 1;
        } while (!unsafe.compareAndSwapLong(this, _marksOffset, before, after));
        return after;
    }

    /**
     * @native ts
     * this._marks = this._marks - 1;
     * return this._marks
     */
    @Override
    public final long unmark() {
        long before;
        long after;
        do {
            before = _marks;
            after = before - 1;
        } while (!unsafe.compareAndSwapLong(this, _marksOffset, before, after));
        return after;
    }

    /**
     * @native ts
     * if(p_unsafe_elem != null){
     * if(p_elemType == org.mwg.Type.STRING){ if(!(typeof p_unsafe_elem === 'string')){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.BOOL){ if(!(typeof p_unsafe_elem === 'boolean')){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.DOUBLE || p_elemType == org.mwg.Type.LONG || p_elemType == org.mwg.Type.INT){ if(!(typeof p_unsafe_elem === 'number')){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.DOUBLE_ARRAY){ if(!(p_unsafe_elem instanceof Float64Array)){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.LONG_ARRAY){ if(!(p_unsafe_elem instanceof Float64Array)){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.INT_ARRAY){ if(!(p_unsafe_elem instanceof Int32Array)){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.STRING_TO_LONG_MAP){ if(!(typeof p_unsafe_elem === 'object')){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.LONG_TO_LONG_MAP){ if(!(typeof p_unsafe_elem === 'boolean')){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * if(p_elemType == org.mwg.Type.LONG_TO_LONG_ARRAY_MAP){ if(!(typeof p_unsafe_elem === 'boolean')){ throw new Error("mwDB usage error, set method called with type " + org.mwg.Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem); } }
     * }
     * this.internal_set(p_elementIndex, p_elemType, p_unsafe_elem, true);
     */
    @Override
    public final void set(final long p_elementIndex, final byte p_elemType, final Object p_unsafe_elem) {
        internal_set(p_elementIndex, p_elemType, p_unsafe_elem, true);
    }

    @Override
    public void setFromKey(String key, byte p_elemType, Object p_unsafe_elem) {
        internal_set(_space.graph().resolver().stringToHash(key, true), p_elemType, p_unsafe_elem, true);
    }

    private synchronized void internal_set(final long p_elementIndex, final byte p_elemType, final Object p_unsafe_elem, boolean replaceIfPresent) {
        Object param_elem = null;
        //check the param type

        if (p_unsafe_elem != null) {
            try {
                switch (p_elemType) {
                    /** Primitives */
                    case Type.BOOL:
                        param_elem = (boolean) p_unsafe_elem;
                        break;
                    case Type.DOUBLE:
                        param_elem = (double) p_unsafe_elem;
                        break;
                    case Type.LONG:
                        if (p_unsafe_elem instanceof Integer) {
                            int preCasting = (int) p_unsafe_elem;
                            param_elem = (long) preCasting;
                        } else {
                            param_elem = (long) p_unsafe_elem;
                        }
                        break;
                    case Type.INT:
                        param_elem = (int) p_unsafe_elem;
                        break;
                    case Type.STRING:
                        param_elem = (String) p_unsafe_elem;
                        break;
                    /** Arrays */
                    case Type.DOUBLE_ARRAY:
                        if (p_unsafe_elem != null) {
                            double[] castedParamDouble = (double[]) p_unsafe_elem;
                            double[] clonedDoubleArray = new double[castedParamDouble.length];
                            System.arraycopy(castedParamDouble, 0, clonedDoubleArray, 0, castedParamDouble.length);
                            param_elem = clonedDoubleArray;
                        }
                        break;
                    case Type.RELATION:
                    case Type.LONG_ARRAY:
                        if (p_unsafe_elem != null) {
                            long[] castedParamLong = (long[]) p_unsafe_elem;
                            long[] clonedLongArray = new long[castedParamLong.length];
                            System.arraycopy(castedParamLong, 0, clonedLongArray, 0, castedParamLong.length);
                            param_elem = clonedLongArray;
                        }
                        break;
                    case Type.INT_ARRAY:
                        if (p_unsafe_elem != null) {
                            int[] castedParamInt = (int[]) p_unsafe_elem;
                            int[] clonedIntArray = new int[castedParamInt.length];
                            System.arraycopy(castedParamInt, 0, clonedIntArray, 0, castedParamInt.length);
                            param_elem = clonedIntArray;
                        }
                        break;
                    /** Maps */
                    case Type.STRING_TO_LONG_MAP:
                        param_elem = (StringLongMap) p_unsafe_elem;
                        break;
                    case Type.LONG_TO_LONG_MAP:
                        param_elem = (LongLongMap) p_unsafe_elem;
                        break;
                    case Type.LONG_TO_LONG_ARRAY_MAP:
                        param_elem = (LongLongArrayMap) p_unsafe_elem;
                        break;
                    default:
                        throw new RuntimeException("Internal Exception, unknown type");
                }
            } catch (Exception e) {
                throw new RuntimeException("mwDB usage error, set method called with type " + Type.typeName(p_elemType) + " while param object is " + p_unsafe_elem);
            }
        }

        int entry = -1;
        InternalState internalState = state;
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
                int newLength = (internalState._elementDataSize == 0 ? 1 : internalState._elementDataSize * 2);
                long[] newElementK = new long[newLength];
                Object[] newElementV = new Object[newLength];
                byte[] newElementType = new byte[newLength];
                if (internalState._elementDataSize > 0) {
                    System.arraycopy(internalState._elementK, 0, newElementK, 0, internalState._elementDataSize);
                    System.arraycopy(internalState._elementV, 0, newElementV, 0, internalState._elementDataSize);
                    System.arraycopy(internalState._elementType, 0, newElementType, 0, internalState._elementDataSize);
                }
                int[] newElementNext = new int[newLength];
                int[] newElementHash = new int[newLength];
                for (int i = 0; i < newLength; i++) {
                    newElementNext[i] = -1;
                    newElementHash[i] = -1;
                }
                //rehashEveryThing
                for (int i = 0; i < newElementV.length; i++) {
                    if (newElementV[i] != null) { //there is a real value
                        int keyHash = (int) PrimitiveHelper.longHash(newElementK[i], newLength);
                        int currentHashedIndex = newElementHash[keyHash];
                        if (currentHashedIndex != -1) {
                            newElementNext[i] = currentHashedIndex;
                        }
                        newElementHash[keyHash] = i;
                    }
                }
                //setPrimitiveType value for all
                internalState = new InternalState(newLength, newElementK, newElementV, newElementNext, newElementHash, newElementType, internalState._elementCount, false);
                this.state = internalState;
                hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
            } else if (internalState.hashReadOnly) {
                //deepClone state
                internalState = internalState.deepClone();
                state = internalState;
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
            if (replaceIfPresent || (p_elemType != internalState._elementType[entry])) {
                internalState._elementV[entry] = param_elem;/*setValue*/
                if (internalState._elementType[entry] != p_elemType) {
                    //typeSwitch, we have to deep clone as well
                    internalState = internalState.deepClone();
                    state = internalState;
                    internalState._elementType[entry] = p_elemType;
                }

            }
        }
        internal_set_dirty();
    }

    @Override
    public final Object get(long p_elementIndex) {
        final InternalState internalState = state;
        if (internalState._elementDataSize == 0) {
            return null;
        }
        int hashIndex = (int) PrimitiveHelper.longHash(p_elementIndex, internalState._elementDataSize);
        int m = internalState._elementHash[hashIndex];

        Object result = null;
        while (m >= 0) {
            if (p_elementIndex == internalState._elementK[m] /* getKey */) {
                result = internalState._elementV[m]; /* getValue */
                break;
            } else {
                m = internalState._elementNext[m];
            }
        }

        if (result == null) {
            return null;
        }
        switch (internalState._elementType[m]) {
            case Type.DOUBLE_ARRAY:
                double[] castedResultD = (double[]) result;
                double[] copyD = new double[castedResultD.length];
                System.arraycopy(castedResultD, 0, copyD, 0, castedResultD.length);
                return copyD;
            case Type.RELATION:
            case Type.LONG_ARRAY:
                long[] castedResultL = (long[]) result;
                long[] copyL = new long[castedResultL.length];
                System.arraycopy(castedResultL, 0, copyL, 0, castedResultL.length);
                return copyL;
            case Type.INT_ARRAY:
                int[] castedResultI = (int[]) result;
                int[] copyI = new int[castedResultI.length];
                System.arraycopy(castedResultI, 0, copyI, 0, castedResultI.length);
                return copyI;
            default:
                return result;
        }
    }

    @Override
    public Object getFromKey(String key) {
        return get(_space.graph().resolver().stringToHash(key, false));
    }

    @Override
    public <A> A getFromKeyWithDefault(String key, A defaultValue) {
        Object result = getFromKey(key);
        if (result == null) {
            return defaultValue;
        } else {
            return (A) result;
        }
    }

    @Override
    public final byte getType(long p_elementIndex) {
        final InternalState internalState = state;
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
    public byte getTypeFromKey(String key) {
        return getType(_space.graph().resolver().stringToHash(key, false));
    }

    @Override
    public final Object getOrCreate(long p_elementIndex, byte elemType) {
        Object previousObject = get(p_elementIndex);
        byte previousType = getType(p_elementIndex);
        if (previousObject != null && previousType == elemType) {
            return previousObject;
        }
        switch (elemType) {
            case Type.STRING_TO_LONG_MAP:
                internal_set(p_elementIndex, elemType, new ArrayStringLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, null), false);
                break;
            case Type.LONG_TO_LONG_MAP:
                internal_set(p_elementIndex, elemType, new ArrayLongLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, null), false);
                break;
            case Type.LONG_TO_LONG_ARRAY_MAP:
                internal_set(p_elementIndex, elemType, new ArrayLongLongArrayMap(this, CoreConstants.MAP_INITIAL_CAPACITY, null), false);
                break;
        }
        return get(p_elementIndex);
    }

    @Override
    public Object getOrCreateFromKey(String key, byte elemType) {
        return getOrCreate(_space.graph().resolver().stringToHash(key, true), elemType);
    }

    @Override
    public final void each(NodeStateCallback callBack) {
        final InternalState currentState = this.state;
        for (int i = 0; i < (currentState._elementCount); i++) {
            if (currentState._elementV[i] != null) {
                callBack.on(currentState._elementK[i], currentState._elementType[i], currentState._elementV[i]);
            }
        }
    }

    @Override
    public void merge(Buffer buffer) {
        load(buffer, true);
    }

    private void load(Buffer payload, boolean isMerge) {
        if (payload == null || payload.length() == 0) {
            return;
        }
        inLoadMode = true;
        //future map elements
        long[] newElementK = null;
        Object[] newElementV = null;
        byte[] newElementType = null;
        int[] newElementNext = null;
        int[] newElementHash = null;
        int newNumberElement = 0;
        int newStateCapacity = 0;
        //reset size
        int currentElemIndex = 0;

        int cursor = 0;
        long payloadSize = payload.length();

        int previousStart = -1;
        long currentChunkElemKey = CoreConstants.NULL_LONG;
        byte currentChunkElemType = -1;

        //init detections
        boolean isFirstElem = true;

        //array sub creation variable
        double[] currentDoubleArr = null;
        long[] currentLongArr = null;
        int[] currentIntArr = null;

        //map sub creation variables
        StringLongMap currentStringLongMap = null;
        LongLongMap currentLongLongMap = null;
        LongLongArrayMap currentLongLongArrayMap = null;

        //array variables
        long currentSubSize = -1;
        int currentSubIndex = 0;

        //map key variables
        long currentMapLongKey = CoreConstants.NULL_LONG;
        String currentMapStringKey = null;

        while (cursor < payloadSize) {
            byte current = payload.read(cursor);
            if (current == CoreConstants.CHUNK_SEP) {
                if (isFirstElem) {
                    //initial the map
                    isFirstElem = false;
                    int stateChunkSize = Base64.decodeToIntWithBounds(payload, 0, cursor);

                    if (!isMerge) { //in case of merge, state chunk already initialized
                        newNumberElement = stateChunkSize;
                        int newStateChunkSize = (stateChunkSize == 0 ? 1 : stateChunkSize * 2);
                        //init map element
                        newElementK = new long[newStateChunkSize];
                        newElementV = new Object[newStateChunkSize];
                        newElementType = new byte[newStateChunkSize];
                        newStateCapacity = newStateChunkSize;
                        //init hash and chaining
                        newElementNext = new int[newStateChunkSize];
                        newElementHash = new int[newStateChunkSize];
                        for (int i = 0; i < newStateChunkSize; i++) {
                            newElementNext[i] = -1;
                            newElementHash[i] = -1;
                        }
                    }

                    previousStart = cursor + 1;
                } else {
                    //beginning of the Chunk elem
                    //check if something is still in buffer
                    if (currentChunkElemType != -1) {
                        Object toInsert = null;
                        switch (currentChunkElemType) {
                            /** Primitive Object */
                            case Type.BOOL:
                                if (payload.read(previousStart) == CoreConstants.BOOL_FALSE) {
                                    toInsert = false;
                                } else if (payload.read(previousStart) == CoreConstants.BOOL_TRUE) {
                                    toInsert = true;
                                }
                                break;
                            case Type.STRING:
                                toInsert = Base64.decodeToStringWithBounds(payload, previousStart, cursor);
                                break;

                            case Type.DOUBLE:
                                toInsert = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                                break;

                            case Type.LONG:
                                toInsert = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                                break;

                            case Type.INT:
                                toInsert = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                                break;
                            /** Arrays */
                            case Type.DOUBLE_ARRAY:
                                if (currentDoubleArr == null) {
                                    currentDoubleArr = new double[Base64.decodeToIntWithBounds(payload, previousStart, cursor)];
                                } else {
                                    currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                                }
                                toInsert = currentDoubleArr;
                                break;
                            case Type.RELATION:
                            case Type.LONG_ARRAY:
                                if (currentLongArr == null) {
                                    currentLongArr = new long[Base64.decodeToIntWithBounds(payload, previousStart, cursor)];
                                } else {
                                    currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                                }
                                toInsert = currentLongArr;
                                break;

                            case Type.INT_ARRAY:
                                if (currentIntArr == null) {
                                    currentIntArr = new int[Base64.decodeToIntWithBounds(payload, previousStart, cursor)];
                                } else {
                                    currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                                }
                                toInsert = currentIntArr;
                                break;
                            /** Maps */
                            case Type.STRING_TO_LONG_MAP:
                                if (currentMapStringKey != null) {
                                    currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                }
                                toInsert = currentStringLongMap;
                                break;
                            case Type.LONG_TO_LONG_MAP:
                                if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                    currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                }
                                toInsert = currentLongLongMap;
                                break;
                            case Type.LONG_TO_LONG_ARRAY_MAP:
                                if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                    currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                }
                                toInsert = currentLongLongArrayMap;
                                break;
                        }
                        if (toInsert != null) {
                            //insert K/V

                            if (isMerge) {
                                internal_set(currentChunkElemKey, currentChunkElemType, toInsert, true); //enhance this with boolean array
                            } else {
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
                    }
                    //next round, reset all variables...
                    previousStart = cursor + 1;
                    currentChunkElemKey = CoreConstants.NULL_LONG;
                    currentChunkElemType = -1;
                    currentSubSize = -1;
                    currentSubIndex = 0;
                    currentMapLongKey = CoreConstants.NULL_LONG;
                    currentMapStringKey = null;
                }
            } else if (current == CoreConstants.CHUNK_SUB_SEP) { //SEPARATION BETWEEN KEY,TYPE,VALUE
                if (currentChunkElemKey == CoreConstants.NULL_LONG) {
                    currentChunkElemKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    previousStart = cursor + 1;
                } else if (currentChunkElemType == -1) {
                    currentChunkElemType = (byte) Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                    previousStart = cursor + 1;
                }
            } else if (current == CoreConstants.CHUNK_SUB_SUB_SEP) { //SEPARATION BETWEEN ARRAY VALUES AND MAP KEY/VALUE TUPLES
                if (currentSubSize == -1) {
                    currentSubSize = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    //init array or maps
                    switch (currentChunkElemType) {
                        /** Arrays */
                        case Type.DOUBLE_ARRAY:
                            currentDoubleArr = new double[(int) currentSubSize];
                            break;
                        case Type.RELATION:
                        case Type.LONG_ARRAY:
                            currentLongArr = new long[(int) currentSubSize];
                            break;
                        case Type.INT_ARRAY:
                            currentIntArr = new int[(int) currentSubSize];
                            break;
                        /** Maps */
                        case Type.STRING_TO_LONG_MAP:
                            currentStringLongMap = new ArrayStringLongMap(this, (int) currentSubSize, null);
                            break;
                        case Type.LONG_TO_LONG_MAP:
                            currentLongLongMap = new ArrayLongLongMap(this, (int) currentSubSize, null);
                            break;
                        case Type.LONG_TO_LONG_ARRAY_MAP:
                            currentLongLongArrayMap = new ArrayLongLongArrayMap(this, (int) currentSubSize, null);
                            break;
                    }
                } else {
                    switch (currentChunkElemType) {
                        /** Arrays */
                        case Type.DOUBLE_ARRAY:
                            currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        case Type.RELATION:
                        case Type.LONG_ARRAY:
                            currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        case Type.INT_ARRAY:
                            currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        /** Maps */
                        case Type.STRING_TO_LONG_MAP:
                            if (currentMapStringKey != null) {
                                currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                currentMapStringKey = null;
                            }
                            break;
                        case Type.LONG_TO_LONG_MAP:
                            if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                currentMapLongKey = CoreConstants.NULL_LONG;
                            }
                            break;
                        case Type.LONG_TO_LONG_ARRAY_MAP:
                            if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                                currentMapLongKey = CoreConstants.NULL_LONG;
                            }
                            break;
                    }
                }
                previousStart = cursor + 1;
            } else if (current == CoreConstants.CHUNK_SUB_SUB_SUB_SEP) {
                switch (currentChunkElemType) {
                    case Type.STRING_TO_LONG_MAP:
                        if (currentMapStringKey == null) {
                            currentMapStringKey = Base64.decodeToStringWithBounds(payload, previousStart, cursor);
                        } else {
                            currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                            //reset key for next loop
                            currentMapStringKey = null;
                        }
                        break;
                    case Type.LONG_TO_LONG_MAP:
                        if (currentMapLongKey == CoreConstants.NULL_LONG) {
                            currentMapLongKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                        } else {
                            currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                            //reset key for next loop
                            currentMapLongKey = CoreConstants.NULL_LONG;
                        }
                        break;
                    case Type.LONG_TO_LONG_ARRAY_MAP:
                        if (currentMapLongKey == CoreConstants.NULL_LONG) {
                            currentMapLongKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                        } else {
                            currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                            //reset key for next loop
                            currentMapLongKey = CoreConstants.NULL_LONG;
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
                case Type.BOOL:
                    if (payload.read(previousStart) == CoreConstants.BOOL_FALSE) {
                        toInsert = false;
                    } else if (payload.read(previousStart) == CoreConstants.BOOL_TRUE) {
                        toInsert = true;
                    }
                    break;
                case Type.STRING:
                    toInsert = Base64.decodeToStringWithBounds(payload, previousStart, cursor);
                    break;
                case Type.DOUBLE:
                    toInsert = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                    break;
                case Type.LONG:
                    toInsert = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    break;
                case Type.INT:
                    toInsert = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                    break;
                /** Arrays */
                case Type.DOUBLE_ARRAY:
                    if (currentDoubleArr == null) {
                        currentDoubleArr = new double[Base64.decodeToIntWithBounds(payload, previousStart, cursor)];
                    } else {
                        currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(payload, previousStart, cursor);
                    }
                    toInsert = currentDoubleArr;
                    break;
                case Type.RELATION:
                case Type.LONG_ARRAY:
                    if (currentLongArr == null) {
                        currentLongArr = new long[Base64.decodeToIntWithBounds(payload, previousStart, cursor)];
                    } else {
                        currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    }
                    toInsert = currentLongArr;
                    break;
                case Type.INT_ARRAY:
                    if (currentIntArr == null) {
                        currentIntArr = new int[Base64.decodeToIntWithBounds(payload, previousStart, cursor)];
                    } else {
                        currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(payload, previousStart, cursor);
                    }
                    toInsert = currentIntArr;
                    break;
                /** Maps */
                case Type.STRING_TO_LONG_MAP:
                    if (currentMapStringKey != null) {
                        currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                    }
                    toInsert = currentStringLongMap;
                    break;
                case Type.LONG_TO_LONG_MAP:
                    if (currentMapLongKey != CoreConstants.NULL_LONG) {
                        currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                    }
                    toInsert = currentLongLongMap;
                    break;
                case Type.LONG_TO_LONG_ARRAY_MAP:
                    if (currentMapLongKey != CoreConstants.NULL_LONG) {
                        currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(payload, previousStart, cursor));
                    }
                    toInsert = currentLongLongArrayMap;
                    break;

            }
            if (toInsert != null) {
                //insert K/V

                if (isMerge) {
                    internal_set(currentChunkElemKey, currentChunkElemType, toInsert, true); //enhance this with boolean array
                } else {
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
        }
        //set the state
        if (!isMerge) {
            this.state = new InternalState(newStateCapacity, newElementK, newElementV, newElementNext, newElementHash, newElementType, newNumberElement, false);
        }

        this.inLoadMode = false;
    }

    @Override
    public final void save(Buffer buffer) {
        final InternalState internalState = state;
        Base64.encodeIntToBuffer(internalState._elementCount, buffer);
        for (int i = 0; i < internalState._elementCount; i++) {
            if (internalState._elementV[i] != null) { //there is a real value
                long loopKey = internalState._elementK[i];
                Object loopValue = internalState._elementV[i];
                if (loopValue != null) {
                    buffer.write(CoreConstants.CHUNK_SEP);
                    Base64.encodeLongToBuffer(loopKey, buffer);
                    buffer.write(CoreConstants.CHUNK_SUB_SEP);
                    /** Encode to type of elem, for unSerialization */
                    Base64.encodeIntToBuffer(internalState._elementType[i], buffer);
                    buffer.write(CoreConstants.CHUNK_SUB_SEP);
                    switch (internalState._elementType[i]) {
                        /** Primitive Types */
                        case Type.STRING:
                            Base64.encodeStringToBuffer((String) loopValue, buffer);
                            break;
                        case Type.BOOL:
                            if ((boolean) internalState._elementV[i]) {
                                buffer.write(CoreConstants.BOOL_TRUE);
                            } else {
                                buffer.write(CoreConstants.BOOL_FALSE);
                            }
                            break;
                        case Type.LONG:
                            Base64.encodeLongToBuffer((long) loopValue, buffer);
                            break;
                        case Type.DOUBLE:
                            Base64.encodeDoubleToBuffer((double) loopValue, buffer);
                            break;
                        case Type.INT:
                            Base64.encodeIntToBuffer((int) loopValue, buffer);
                            break;
                        /** Arrays */
                        case Type.DOUBLE_ARRAY:
                            double[] castedDoubleArr = (double[]) loopValue;
                            Base64.encodeIntToBuffer(castedDoubleArr.length, buffer);
                            for (int j = 0; j < castedDoubleArr.length; j++) {
                                buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                                Base64.encodeDoubleToBuffer(castedDoubleArr[j], buffer);
                            }
                            break;
                        case Type.RELATION:
                        case Type.LONG_ARRAY:
                            long[] castedLongArr = (long[]) loopValue;
                            Base64.encodeIntToBuffer(castedLongArr.length, buffer);
                            for (int j = 0; j < castedLongArr.length; j++) {
                                buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                                Base64.encodeLongToBuffer(castedLongArr[j], buffer);
                            }
                            break;
                        case Type.INT_ARRAY:
                            int[] castedIntArr = (int[]) loopValue;
                            Base64.encodeIntToBuffer(castedIntArr.length, buffer);
                            for (int j = 0; j < castedIntArr.length; j++) {
                                buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                                Base64.encodeIntToBuffer(castedIntArr[j], buffer);
                            }
                            break;
                        /** Maps */
                        case Type.STRING_TO_LONG_MAP:
                            StringLongMap castedStringLongMap = (StringLongMap) loopValue;
                            Base64.encodeLongToBuffer(castedStringLongMap.size(), buffer);
                            castedStringLongMap.each(new StringLongMapCallBack() {
                                @Override
                                public void on(final String key, final long value) {
                                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                                    Base64.encodeStringToBuffer(key, buffer);
                                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(value, buffer);
                                }
                            });
                            break;
                        case Type.LONG_TO_LONG_MAP:
                            LongLongMap castedLongLongMap = (LongLongMap) loopValue;
                            Base64.encodeLongToBuffer(castedLongLongMap.size(), buffer);
                            castedLongLongMap.each(new LongLongMapCallBack() {
                                @Override
                                public void on(final long key, final long value) {
                                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(key, buffer);
                                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(value, buffer);
                                }
                            });
                            break;
                        case Type.LONG_TO_LONG_ARRAY_MAP:
                            LongLongArrayMap castedLongLongArrayMap = (LongLongArrayMap) loopValue;
                            Base64.encodeLongToBuffer(castedLongLongArrayMap.size(), buffer);
                            castedLongLongArrayMap.each(new LongLongArrayMapCallBack() {
                                @Override
                                public void on(final long key, final long value) {
                                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                                    Base64.encodeLongToBuffer(key, buffer);
                                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SUB_SEP);
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
    }

    private void internal_set_dirty() {
        if (_space != null) {
            if ((_flags & CoreConstants.DIRTY_BIT) != CoreConstants.DIRTY_BIT) {
                _space.declareDirty(this);
            }
        }
    }

    @Override
    public final long flags() {
        return _flags;
    }

    /**
     * @native ts
     * var val = this._flags
     * var nval = val & ~bitsToDisable | bitsToEnable;
     * this._flags = nval;
     * return val != nval;
     */
    @Override
    public final boolean setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags;
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!unsafe.compareAndSwapLong(this, _flagsOffset, val, nval));
        return val != nval;
    }
}
