package org.mwg.core.chunk.offheap;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.core.CoreConstants;
import org.mwg.plugin.Chunk;
import org.mwg.struct.*;
import org.mwg.core.chunk.*;
import org.mwg.plugin.Resolver;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

/**
 * @ignore ts
 * Memory layout: all structures are memory blocks of either primitive values (as longs)
 * or pointers to memory blocks
 */
public class OffHeapStateChunk implements StateChunk, ChunkListener, OffHeapChunk {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    // keys
    private static final int INDEX_WORLD = CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD;
    private static final int INDEX_TIME = CoreConstants.OFFHEAP_CHUNK_INDEX_TIME;
    private static final int INDEX_ID = CoreConstants.OFFHEAP_CHUNK_INDEX_ID;
    private static final int INDEX_TYPE = CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE;
    private static final int INDEX_FLAGS = CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS;
    private static final int INDEX_MARKS = CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS;

    // long arrays
    private static final int INDEX_ELEMENT_K = 6;
    private static final int INDEX_ELEMENT_V = 7;
    private static final int INDEX_ELEMENT_NEXT = 8;
    private static final int INDEX_ELEMENT_HASH = 9;
    private static final int INDEX_ELEMENT_TYPE = 10;

    // long values
    private static final int INDEX_LOCK = 11;
    private static final int INDEX_COUNTER = 12;
    private static final int INDEX_ELEMENT_DATA_SIZE = 13;
    private static final int INDEX_ELEMENT_THRESHOLD = 14;
    private static final int INDEX_ELEMENT_COUNT = 15;
    private static final int INDEX_HASH_READ_ONLY = 16;

    //pointer values
    private final ChunkListener _space;
    private long elementK_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;
    private long elementType_ptr;
    private final long root_array_ptr;

    // simple values
    private boolean inLoadMode = false;

    private void consistencyCheck() {
        if (OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_NEXT) != elementNext_ptr) {
            elementK_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V);
            elementNext_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT);
            elementHash_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH);
            elementType_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE);
        }
    }

    public OffHeapStateChunk(ChunkListener space, long previousAddr, Buffer initialPayload, Chunk origin) {
        _space = space;
        if (previousAddr == CoreConstants.OFFHEAP_NULL_PTR) {
            root_array_ptr = OffHeapLongArray.allocate(17);
            OffHeapLongArray.set(root_array_ptr, INDEX_FLAGS, 0);
            OffHeapLongArray.set(root_array_ptr, INDEX_COUNTER, 0);
            long initialCapacity = CoreConstants.MAP_INITIAL_CAPACITY;
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, 0);
            long threshold = (long) (initialCapacity * CoreConstants.MAP_LOAD_FACTOR);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_THRESHOLD, threshold);
            OffHeapLongArray.set(root_array_ptr, INDEX_LOCK, 0); // not locked

            inLoadMode = false;

            if (initialPayload != null) {
                load(initialPayload);
            } else if (origin != null) {
                OffHeapStateChunk castedOrigin = (OffHeapStateChunk) origin;
                softClone(castedOrigin);
                incrementCopyOnWriteCounter(castedOrigin.root_array_ptr);
            } else {
                /** init long[] variables */
                elementK_ptr = OffHeapLongArray.allocate(initialCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
                elementV_ptr = OffHeapLongArray.allocate(initialCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, elementV_ptr); //used for soft clone, therefore cow counter cannot be here
                elementNext_ptr = OffHeapLongArray.allocate(initialCapacity + 1); //cow counter + capacity
                OffHeapLongArray.set(elementNext_ptr, 0, 1); //init cow counter
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);
                elementHash_ptr = OffHeapLongArray.allocate(initialCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
                elementType_ptr = OffHeapLongArray.allocate(initialCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, elementType_ptr);

                OffHeapLongArray.set(root_array_ptr, INDEX_HASH_READ_ONLY, 0);
            }

        } else {
            root_array_ptr = previousAddr;
            elementK_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V);
            elementNext_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT);
            elementHash_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH);
            elementType_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE);
        }

    }

    @Override
    public final long addr() {
        return this.root_array_ptr;
    }

    private void softClone(OffHeapStateChunk origin) {
        long elementDataSize = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        long elementCount = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_COUNT);

        // root array is already initialized
        // copy elementV array
        long elementV_ptr = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_V);
        long clonedElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, elementDataSize);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, clonedElementV_ptr);

        // link
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, origin.elementK_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, origin.elementNext_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, origin.elementHash_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, origin.elementType_ptr);

        // set elementDataSize
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, elementDataSize);
        // set elementCount
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, elementCount);
        // set hashReadOnly
        OffHeapLongArray.set(root_array_ptr, INDEX_HASH_READ_ONLY, 1);

        // increase the copy on write counters, manage the map indirections
        for (long i = 0; i < elementCount; i++) {
            byte elementType = (byte) OffHeapLongArray.get(origin.elementType_ptr, i);
            if (elementType != CoreConstants.OFFHEAP_NULL_PTR) { // is there a real value?
                long elemPtr = OffHeapLongArray.get(clonedElementV_ptr, i);
                switch (elementType) {
                    /** String */
                    case Type.STRING:
                        unsafe.getAndAddLong(null, elemPtr, 1);
                        break;
                    /** Arrays */
                    case Type.DOUBLE_ARRAY:
                        unsafe.getAndAddLong(null, elemPtr, 1);
                        break;
                    case Type.LONG_ARRAY:
                        unsafe.getAndAddLong(null, elemPtr, 1);
                        break;
                    case Type.INT_ARRAY:
                        unsafe.getAndAddLong(null, elemPtr, 1);
                        break;
                    // Maps
                    case Type.LONG_LONG_MAP:
                        if (OffHeapLongArray.get(clonedElementV_ptr, i) != CoreConstants.OFFHEAP_NULL_PTR) {
                            long tmpLongLongMap_ptr = OffHeapLongArray.get(clonedElementV_ptr, i);
                            long longLongMap_ptr = ArrayLongLongMap.softClone(tmpLongLongMap_ptr);
                            ArrayLongLongMap.incrementCopyOnWriteCounter(tmpLongLongMap_ptr);
                            OffHeapLongArray.set(clonedElementV_ptr, i, longLongMap_ptr);
                        }
                        break;
                    case Type.LONG_LONG_ARRAY_MAP:
                        if (OffHeapLongArray.get(clonedElementV_ptr, i) != CoreConstants.OFFHEAP_NULL_PTR) {
                            long tmpLongLongArrayMap_ptr = OffHeapLongArray.get(clonedElementV_ptr, i);
                            long longLongArrayMap_ptr = ArrayLongLongArrayMap.softClone(tmpLongLongArrayMap_ptr);
                            ArrayLongLongArrayMap.incrementCopyOnWriteCounter(tmpLongLongArrayMap_ptr);
                            OffHeapLongArray.set(clonedElementV_ptr, i, longLongArrayMap_ptr);
                        }
                        break;
                    case Type.STRING_LONG_MAP:
                        if (OffHeapLongArray.get(clonedElementV_ptr, i) != CoreConstants.OFFHEAP_NULL_PTR) {
                            long tmpStringLongMap_ptr = OffHeapLongArray.get(clonedElementV_ptr, i);
                            long stringLongMap_ptr = ArrayStringLongMap.softClone(tmpStringLongMap_ptr);
                            ArrayStringLongMap.incrementCopyOnWriteCounter(tmpStringLongMap_ptr);
                            OffHeapLongArray.set(clonedElementV_ptr, i, stringLongMap_ptr);
                        }
                        break;
                }
            }
        }

        this.elementV_ptr = clonedElementV_ptr;
        this.elementK_ptr = origin.elementK_ptr;
        this.elementHash_ptr = origin.elementHash_ptr;
        this.elementNext_ptr = origin.elementNext_ptr;
        this.elementType_ptr = origin.elementType_ptr;
    }

    private void shallowClone(OffHeapStateChunk origin) {
        long elementDataSize = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        long elementCount = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_COUNT);

        // root array is already initialized
        // copy elementK array
        long elementK_ptr = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_K);
        long clonedElementK_ptr = OffHeapLongArray.cloneArray(elementK_ptr, elementDataSize);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, clonedElementK_ptr);
//        // copy elementV array
//        long elementV_ptr = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_V);
//        long clonedElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, elementDataSize);
//        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, clonedElementV_ptr);
        // copy elementNext array
        long elementNext_ptr = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_NEXT);
        long clonedElementNext_ptr = OffHeapLongArray.cloneArray(elementNext_ptr, elementDataSize + 1); //cow counter + size
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, clonedElementNext_ptr);
        // copy elementHash array
        long elementHash_ptr = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_HASH);
        long clonedElementHash_ptr = OffHeapLongArray.cloneArray(elementHash_ptr, elementDataSize);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, clonedElementHash_ptr);
        // copy elementType array
        long elementType_ptr = OffHeapLongArray.get(origin.root_array_ptr, INDEX_ELEMENT_TYPE);
        long clonedElementType_ptr = OffHeapLongArray.cloneArray(elementType_ptr, elementDataSize);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, clonedElementType_ptr);
        // set elementDataSize
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, elementDataSize);
        // set elementCount
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, elementCount);
        // set hashReadOnly
        OffHeapLongArray.set(root_array_ptr, INDEX_HASH_READ_ONLY, 0);

        this.elementK_ptr = clonedElementK_ptr;
        this.elementNext_ptr = clonedElementNext_ptr;
        this.elementHash_ptr = clonedElementHash_ptr;
        this.elementType_ptr = clonedElementType_ptr;
    }

    @Override
    public final void each(StateChunkCallBack callBack, Resolver resolver) {
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1)) ;
        try {
            consistencyCheck();
            for (int i = 0; i < OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT); i++) {
                if (OffHeapLongArray.get(elementType_ptr, i) != CoreConstants.OFFHEAP_NULL_PTR) {
                    callBack.on(resolver.hashToString(OffHeapLongArray.get(elementK_ptr, i)),
                            (int) OffHeapLongArray.get(elementType_ptr, i),
                            internal_getElementV(i) /*OffHeapLongArray.get(elementV_ptr, i)*/);
                }
            }
        } finally {
            if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
        }
    }

    @Override
    public final long world() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_WORLD);
    }

    @Override
    public final long time() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_TIME);
    }

    @Override
    public final long id() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_ID);
    }

    @Override
    public final void save(Buffer buffer) {
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1)) ; // lock
        try {
            consistencyCheck();
            long elementCount = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
            Base64.encodeLongToBuffer(elementCount, buffer);
            for (int i = 0; i < elementCount; i++) {
                byte elementType = (byte) OffHeapLongArray.get(elementType_ptr, i); // can be safely casted
                if (elementType != CoreConstants.OFFHEAP_NULL_PTR) { //there is a real value
                    long loopKey = OffHeapLongArray.get(elementK_ptr, i);
                    Object loopValue = internal_getElementV(i);
                    if (loopValue != null) {
                        buffer.write(CoreConstants.CHUNK_SEP);
                        Base64.encodeLongToBuffer(loopKey, buffer);
                        buffer.write(CoreConstants.CHUNK_SUB_SEP);
                        /** Encode to type of elem, for unSerialization */
                        Base64.encodeIntToBuffer(elementType, buffer);
                        buffer.write(CoreConstants.CHUNK_SUB_SEP);
                        switch (elementType) {
                            /** Primitive Types */
                            case Type.STRING:
                                Base64.encodeStringToBuffer((String) loopValue, buffer);
                                break;
                            case Type.BOOL:
                                if ((boolean) loopValue) {
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
                            case Type.STRING_LONG_MAP:
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
                            case Type.LONG_LONG_MAP:
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
                            case Type.LONG_LONG_ARRAY_MAP:
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
        } finally {
            if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    private void load(Buffer buffer) {
        if (buffer == null || buffer.size() == 0) {
            return;
        }
        inLoadMode = true;

        //future map elements
        long newElementK_ptr = CoreConstants.OFFHEAP_NULL_PTR;
        long newElementV_ptr = CoreConstants.OFFHEAP_NULL_PTR;
        long newElementType_ptr = CoreConstants.OFFHEAP_NULL_PTR;
        long newElementNext_ptr = CoreConstants.OFFHEAP_NULL_PTR;
        long newElementHash_ptr = CoreConstants.OFFHEAP_NULL_PTR;
        long newNumberElement = 0;
        long newStateCapacity = 0;
        //reset size
        long currentElemIndex = 0;

        int cursor = 0;
        long payloadSize = buffer.size();

        int previousStart = -1;
        long currentChunkElemKey = CoreConstants.NULL_LONG;
        int currentChunkElemType = -1;

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
        int currentSubSize = -1;
        int currentSubIndex = 0;

        //map key variables
        long currentMapLongKey = CoreConstants.NULL_LONG;
        String currentMapStringKey = null;

        while (cursor < payloadSize) {
            if (buffer.read(cursor) == CoreConstants.CHUNK_SEP) {
                if (isFirstElem) {
                    //initial the map
                    isFirstElem = false;
                    long stateChunkSize = Base64.decodeToLongWithBounds(buffer, 0, cursor);
                    newNumberElement = stateChunkSize;
                    long newStateChunkSize = (stateChunkSize == 0 ? 1 : stateChunkSize << 1);
                    //init map element
                    newElementK_ptr = OffHeapLongArray.allocate(newStateChunkSize);
                    newElementV_ptr = OffHeapLongArray.allocate(newStateChunkSize);
                    newElementType_ptr = OffHeapLongArray.allocate(newStateChunkSize);
                    newStateCapacity = newStateChunkSize;
                    //init hash and chaining
                    newElementNext_ptr = OffHeapLongArray.allocate(newStateChunkSize + 1); //cow counter + size
                    OffHeapLongArray.set(newElementNext_ptr, 0, 1); //init cow counter
                    newElementHash_ptr = OffHeapLongArray.allocate(newStateChunkSize);
                    previousStart = cursor + 1;
                } else {
                    //beginning of the Chunk elem
                    //check if something is still in buffer
                    if (currentChunkElemType != -1) {
                        Object toInsert = null;
                        switch (currentChunkElemType) {
                            /** Primitive Object */
                            case Type.BOOL:
                                if (buffer.read(previousStart) == CoreConstants.BOOL_FALSE) {
                                    toInsert = false;
                                } else if (buffer.read(previousStart) == CoreConstants.BOOL_TRUE) {
                                    toInsert = true;
                                }
                                break;
                            case Type.STRING:
                                toInsert = Base64.decodeToStringWithBounds(buffer, previousStart, cursor);
                                break;

                            case Type.DOUBLE:
                                toInsert = Base64.decodeToDoubleWithBounds(buffer, previousStart, cursor);
                                break;

                            case Type.LONG:
                                toInsert = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                                break;

                            case Type.INT:
                                toInsert = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                                break;
                            /** Arrays */
                            case Type.DOUBLE_ARRAY:
                                currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(buffer, previousStart, cursor);
                                toInsert = currentDoubleArr;
                                break;

                            case Type.LONG_ARRAY:
                                currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                                toInsert = currentLongArr;
                                break;

                            case Type.INT_ARRAY:
                                currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                                toInsert = currentIntArr;
                                break;
                            /** Maps */
                            case Type.STRING_LONG_MAP:
                                if (currentMapStringKey != null) {
                                    currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                                }
                                toInsert = currentStringLongMap;
                                break;
                            case Type.LONG_LONG_MAP:
                                if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                    currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                                }
                                toInsert = currentLongLongMap;
                                break;
                            case Type.LONG_LONG_ARRAY_MAP:
                                if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                    currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                                }
                                toInsert = currentLongLongArrayMap;
                                break;
                        }
                        if (toInsert != null) {
                            //insert K/V
                            long newIndex = currentElemIndex;
                            OffHeapLongArray.set(newElementK_ptr, newIndex, currentChunkElemKey);
                            internal_setElementV(newElementV_ptr, newIndex, newElementType_ptr, (byte) currentChunkElemType, toInsert);
                            OffHeapLongArray.set(newElementType_ptr, newIndex, currentChunkElemType);

                            long hashIndex = PrimitiveHelper.longHash(currentChunkElemKey, newStateCapacity);
                            long currentHashedIndex = OffHeapLongArray.get(newElementHash_ptr, hashIndex);
                            if (currentHashedIndex != -1) {
                                OffHeapLongArray.set(newElementNext_ptr + 8, newIndex, currentHashedIndex);
                            }
                            OffHeapLongArray.set(newElementHash_ptr, hashIndex, newIndex);
                            currentElemIndex++;
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
            } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SEP) { //SEPARATION BETWEEN KEY,TYPE,VALUE
                if (currentChunkElemKey == CoreConstants.NULL_LONG) {
                    currentChunkElemKey = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                    previousStart = cursor + 1;
                } else if (currentChunkElemType == -1) {
                    currentChunkElemType = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                    previousStart = cursor + 1;
                }
            } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SUB_SEP) { //SEPARATION BETWEEN ARRAY VALUES AND MAP KEY/VALUE TUPLES
                if (currentSubSize == -1) {
                    currentSubSize = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                    //init array or maps
                    switch (currentChunkElemType) {
                        /** Arrays */
                        case Type.DOUBLE_ARRAY:
                            currentDoubleArr = new double[currentSubSize];
                            break;
                        case Type.LONG_ARRAY:
                            currentLongArr = new long[currentSubSize];
                            break;
                        case Type.INT_ARRAY:
                            currentIntArr = new int[currentSubSize];
                            break;
                        /** Maps */
                        case Type.STRING_LONG_MAP:
                            currentStringLongMap = new ArrayStringLongMap(this, currentSubSize, CoreConstants.OFFHEAP_NULL_PTR);
                            break;
                        case Type.LONG_LONG_MAP:
                            currentLongLongMap = new ArrayLongLongMap(this, currentSubSize, CoreConstants.OFFHEAP_NULL_PTR);
                            break;
                        case Type.LONG_LONG_ARRAY_MAP:
                            currentLongLongArrayMap = new ArrayLongLongArrayMap(this, currentSubSize, CoreConstants.OFFHEAP_NULL_PTR);
                            break;
                    }
                } else {
                    switch (currentChunkElemType) {
                        /** Arrays */
                        case Type.DOUBLE_ARRAY:
                            currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(buffer, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        case Type.LONG_ARRAY:
                            currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        case Type.INT_ARRAY:
                            currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                            currentSubIndex++;
                            break;
                        /** Maps */
                        case Type.STRING_LONG_MAP:
                            if (currentMapStringKey != null) {
                                currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                                currentMapStringKey = null;
                            }
                            break;
                        case Type.LONG_LONG_MAP:
                            if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                                currentMapLongKey = CoreConstants.NULL_LONG;
                            }
                            break;
                        case Type.LONG_LONG_ARRAY_MAP:
                            if (currentMapLongKey != CoreConstants.NULL_LONG) {
                                currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                                currentMapLongKey = CoreConstants.NULL_LONG;
                            }
                            break;

                    }
                }
                previousStart = cursor + 1;
            } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SUB_SUB_SEP) {
                switch (currentChunkElemType) {
                    case Type.STRING_LONG_MAP:
                        if (currentMapStringKey == null) {
                            currentMapStringKey = Base64.decodeToStringWithBounds(buffer, previousStart, cursor);
                        } else {
                            currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                            //reset key for next loop
                            currentMapStringKey = null;
                        }
                        break;
                    case Type.LONG_LONG_MAP:
                        if (currentMapLongKey == CoreConstants.NULL_LONG) {
                            currentMapLongKey = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                        } else {
                            currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                            //reset key for next loop
                            currentMapLongKey = CoreConstants.NULL_LONG;
                        }
                        break;
                    case Type.LONG_LONG_ARRAY_MAP:
                        if (currentMapLongKey == CoreConstants.NULL_LONG) {
                            currentMapLongKey = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                        } else {
                            currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
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
                    if (buffer.read(previousStart) == CoreConstants.BOOL_FALSE) {
                        toInsert = false;
                    } else if (buffer.read(previousStart) == CoreConstants.BOOL_TRUE) {
                        toInsert = true;
                    }
                    break;
                case Type.STRING:
                    toInsert = Base64.decodeToStringWithBounds(buffer, previousStart, cursor);
                    break;
                case Type.DOUBLE:
                    toInsert = Base64.decodeToDoubleWithBounds(buffer, previousStart, cursor);
                    break;
                case Type.LONG:
                    toInsert = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                    break;
                case Type.INT:
                    toInsert = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                    break;
                /** Arrays */
                case Type.DOUBLE_ARRAY:
                    currentDoubleArr[currentSubIndex] = Base64.decodeToDoubleWithBounds(buffer, previousStart, cursor);
                    toInsert = currentDoubleArr;
                    break;
                case Type.LONG_ARRAY:
                    currentLongArr[currentSubIndex] = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                    toInsert = currentLongArr;
                    break;
                case Type.INT_ARRAY:
                    currentIntArr[currentSubIndex] = Base64.decodeToIntWithBounds(buffer, previousStart, cursor);
                    toInsert = currentIntArr;
                    break;
                /** Maps */
                case Type.STRING_LONG_MAP:
                    if (currentMapStringKey != null) {
                        currentStringLongMap.put(currentMapStringKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                    }
                    toInsert = currentStringLongMap;
                    break;
                case Type.LONG_LONG_MAP:
                    if (currentMapLongKey != CoreConstants.NULL_LONG) {
                        currentLongLongMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                    }
                    toInsert = currentLongLongMap;
                    break;
                case Type.LONG_LONG_ARRAY_MAP:
                    if (currentMapLongKey != CoreConstants.NULL_LONG) {
                        currentLongLongArrayMap.put(currentMapLongKey, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                    }
                    toInsert = currentLongLongArrayMap;
                    break;


            }
            if (toInsert != null) {
                //insert K/V
                OffHeapLongArray.set(newElementK_ptr, currentElemIndex, currentChunkElemKey);
                internal_setElementV(newElementV_ptr, currentElemIndex, newElementType_ptr, (byte) currentChunkElemType, toInsert);
                OffHeapLongArray.set(newElementType_ptr, currentElemIndex, currentChunkElemType);

                long hashIndex = PrimitiveHelper.longHash(currentChunkElemKey, newStateCapacity);
                long currentHashedIndex = OffHeapLongArray.get(newElementHash_ptr, hashIndex);
                if (currentHashedIndex != -1) {
                    OffHeapLongArray.set(newElementNext_ptr + 8, currentElemIndex, currentHashedIndex);
                }
                OffHeapLongArray.set(newElementHash_ptr, hashIndex, currentElemIndex);
            }
        }

        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, newElementK_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, newElementV_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, newElementHash_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, newElementType_ptr);

        elementK_ptr = newElementK_ptr;
        elementV_ptr = newElementV_ptr;
        elementNext_ptr = newElementNext_ptr;
        elementHash_ptr = newElementHash_ptr;
        elementType_ptr = newElementType_ptr;

        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, newStateCapacity);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, newNumberElement);
        long threshold = (long) (newStateCapacity * CoreConstants.MAP_LOAD_FACTOR);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_THRESHOLD, threshold);

        OffHeapLongArray.set(root_array_ptr, INDEX_HASH_READ_ONLY, 0);

        inLoadMode = false;
    }

    @Override
    public long marks() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_COUNTER);
    }

    public static void free(long root_array_ptr) {

        long elementType_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE);
        long elementDataSize = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        long elementV_array_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V);
        for (long i = 0; i < elementDataSize; i++) {
            long elemV_ptr = OffHeapLongArray.get(elementV_array_ptr, i);
            if (elemV_ptr != CoreConstants.OFFHEAP_NULL_PTR) {
                freeElement(elemV_ptr, (byte) OffHeapLongArray.get(elementType_ptr, i));
            }
        }

        OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V));
        long thisCowCounter = decrementCopyOnWriteCounter(root_array_ptr);
        if (thisCowCounter == 0) {
            OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K));
            OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT));
            OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH));
            OffHeapLongArray.free(elementType_ptr);
        }
        OffHeapLongArray.free(root_array_ptr);
    }

    private static void freeElement(long addr, byte elemType) {
        long cowCounter;
        switch (elemType) {
            /** Primitive Object */
            case Type.STRING:
                cowCounter = unsafe.getAndAddLong(null, addr, -1) - 1;
                if (cowCounter == 0) {
                    unsafe.freeMemory(addr);
                }
                break;
            /** Arrays */
            case Type.DOUBLE_ARRAY:
                cowCounter = unsafe.getAndAddLong(null, addr, -1) - 1;
                if (cowCounter == 0) {
                    OffHeapDoubleArray.free(addr);
                }
                break;
            case Type.LONG_ARRAY:
                cowCounter = unsafe.getAndAddLong(null, addr, -1) - 1;
                if (cowCounter == 0) {
                    OffHeapLongArray.free(addr);
                }
                break;
            case Type.INT_ARRAY:
                cowCounter = unsafe.getAndAddLong(null, addr, -1) - 1;
                if (cowCounter == 0) {
                    OffHeapLongArray.free(addr);
                }
                break;
            /** Maps */
            case Type.STRING_LONG_MAP:
                ArrayStringLongMap.free(addr);
                break;
            case Type.LONG_LONG_MAP:
                ArrayLongLongMap.free(addr);
                break;
            case Type.LONG_LONG_ARRAY_MAP:
                ArrayLongLongArrayMap.free(addr);
                break;
        }
    }


    @Override
    public final long flags() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_FLAGS);
    }

    @Override
    public final byte chunkType() {
        return CoreConstants.STATE_CHUNK;
    }

    @Override
    public final void declareDirty(Chunk chunk) {
        if (!inLoadMode) {
            internal_set_dirty();
        }
    }

    @Override
    public final Graph graph() {
        return _space.graph();
    }

    @Override
    public final void set(long index, byte elemType, Object elem) {
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1)) ; // lock
        try {
            internal_set(index, elemType, elem, true);
        } finally {
            if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    @Override
    public void setFromKey(String key, byte elemType, Object elem) {
        set(_space.graph().resolver().stringToHash(key, true), elemType, elem);
    }

    private void internal_set(final long p_elementIndex, final byte p_elemType, final Object p_unsafe_elem, boolean replaceIfPresent) {
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
                        param_elem = (double[]) p_unsafe_elem;
                        break;
                    case Type.LONG_ARRAY:
                        param_elem = (long[]) p_unsafe_elem;
                        break;
                    case Type.INT_ARRAY:
                        param_elem = (int[]) p_unsafe_elem;
                        break;
                    /** Maps */
                    case Type.STRING_LONG_MAP:
                        param_elem = (StringLongMap) p_unsafe_elem;
                        break;
                    case Type.LONG_LONG_MAP:
                        param_elem = (LongLongMap) p_unsafe_elem;
                        break;
                    case Type.LONG_LONG_ARRAY_MAP:
                        param_elem = (LongLongArrayMap) p_unsafe_elem;
                        break;
                    default:
                        throw new RuntimeException("mwDB usage error, set method called selectWith an unknown type " + p_elemType);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                throw new RuntimeException("mwDB usage error, set method called with type " + p_elemType + " while param object is " + param_elem);
            }
        }

        long entry = -1;
        long hashIndex = -1;
        long elementDataSize = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        if (elementDataSize > 0) {
            hashIndex = PrimitiveHelper.longHash(p_elementIndex, elementDataSize);
            long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            while (m != -1) {
                if (p_elementIndex == OffHeapLongArray.get(elementK_ptr, m)) {
                    entry = m;
                    break;
                }
                m = OffHeapLongArray.get(elementNext_ptr + 8, m);
            }
        }
        if (entry == -1) {
            long elementCount = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
            long threshold = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_THRESHOLD);
            if (elementCount + 1 > threshold) {
                long newLength = (elementDataSize == 0 ? 1 : elementDataSize << 1);
                long newElementK_ptr = OffHeapLongArray.allocate(newLength);
                long newElementV_ptr = OffHeapLongArray.allocate(newLength);
                long newElementType_ptr = OffHeapLongArray.allocate(newLength);

                unsafe.copyMemory(elementK_ptr, newElementK_ptr, elementDataSize * 8);
                unsafe.copyMemory(elementV_ptr, newElementV_ptr, elementDataSize * 8);
                unsafe.copyMemory(elementType_ptr, newElementType_ptr, elementDataSize * 8);

                long newElementNext_ptr = OffHeapLongArray.allocate(newLength + 1); //cow counter + length
                OffHeapLongArray.set(newElementNext_ptr, 0, 1); //init cow counter
                long newElementHash_ptr = OffHeapLongArray.allocate(newLength);

                //rehashEveryThing
                for (long i = 0; i < elementDataSize; i++) {
                    if (OffHeapLongArray.get(newElementType_ptr, i) != CoreConstants.OFFHEAP_NULL_PTR) { //there is a real value
                        long keyHash = PrimitiveHelper.longHash(OffHeapLongArray.get(newElementK_ptr, i), newLength);
                        long currentHashedIndex = OffHeapLongArray.get(newElementHash_ptr, keyHash);
                        if (currentHashedIndex != -1) {
                            OffHeapLongArray.set(newElementNext_ptr + 8, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(newElementHash_ptr, keyHash, i);
                    }
                }

                OffHeapLongArray.free(elementK_ptr);
                OffHeapLongArray.free(elementV_ptr);
                OffHeapLongArray.free(elementType_ptr);

                OffHeapLongArray.free(elementNext_ptr);
                OffHeapLongArray.free(elementHash_ptr);

                //setPrimitiveType value for all
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, newLength);
                // elementCount stays the same
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_THRESHOLD, (long) (newLength * CoreConstants.MAP_LOAD_FACTOR));
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, newElementK_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, newElementV_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, newElementHash_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, newElementType_ptr);

                OffHeapLongArray.set(root_array_ptr, INDEX_HASH_READ_ONLY, 0);

                elementK_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K);
                elementV_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V);
                elementNext_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT);
                elementHash_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH);
                elementType_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE);

                hashIndex = PrimitiveHelper.longHash(p_elementIndex, newLength);
            } else if (OffHeapLongArray.get(root_array_ptr, INDEX_HASH_READ_ONLY) == 1) {
                //deepClone state
                decrementCopyOnWriteCounter(this.root_array_ptr);
                shallowClone(this);
                OffHeapLongArray.set(elementNext_ptr, 0, 1); //set cow counter
            }
            long newIndex = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT) + 1);
            OffHeapLongArray.set(elementK_ptr, newIndex, p_elementIndex);
            internal_setElementV(elementV_ptr, newIndex, elementType_ptr, p_elemType, param_elem);

            long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            if (currentHashedIndex != -1) {
                OffHeapLongArray.set(elementNext_ptr + 8, newIndex, currentHashedIndex);
            }
            //now the object is reachable to other thread everything should be ready
            OffHeapLongArray.set(elementHash_ptr, hashIndex, newIndex);
        } else {
            if (replaceIfPresent || (p_elemType != OffHeapLongArray.get(elementType_ptr, entry))) {
                internal_setElementV(elementV_ptr, entry, elementType_ptr, p_elemType, param_elem); /*setValue*/
            }
        }
        internal_set_dirty();
    }

    private void internal_setElementV(long addr, long index, long elementTypeAddr, byte elemType, Object elem) {
        long tempPtr = OffHeapLongArray.get(addr, index);
        byte tempType = (byte) OffHeapLongArray.get(elementTypeAddr, index);

        if (elem != null) {
            switch (elemType) {
                /** Primitives */
                case Type.BOOL:
                    OffHeapLongArray.set(addr, index, ((boolean) elem) ? 1 : 0);
                    break;
                case Type.DOUBLE:
                    OffHeapDoubleArray.set(addr, index, ((double) elem));
                    break;
                case Type.LONG:
                    OffHeapLongArray.set(addr, index, ((long) elem));
                    break;
                case Type.INT:
                    OffHeapLongArray.set(addr, index, ((int) elem));
                    break;
                /** String */
                case Type.STRING:
                    String stringToInsert = (String) elem;
                    if (stringToInsert == null) {
                        OffHeapLongArray.set(addr, index, CoreConstants.OFFHEAP_NULL_PTR);
                    } else {
                        byte[] valueAsByte = stringToInsert.getBytes();
                        long newStringPtr = unsafe.allocateMemory(8 + 4 + valueAsByte.length); //counter for copy on write, length, and string content
                        //init counter for copy on write
                        unsafe.putLong(newStringPtr, 1);
                        //set size of the string
                        unsafe.putInt(newStringPtr + 8, valueAsByte.length);
                        //copy string content
                        for (int i = 0; i < valueAsByte.length; i++) {
                            unsafe.putByte(8 + 4 + newStringPtr + i, valueAsByte[i]);
                        }
                        OffHeapLongArray.set(addr, index, newStringPtr);
                    }
                    break;
                /** Arrays */
                case Type.DOUBLE_ARRAY:
                    double[] doubleArrayToInsert = (double[]) elem;
                    if (doubleArrayToInsert != null) {
                        long doubleArrayToInsert_ptr = OffHeapDoubleArray.allocate(2 + doubleArrayToInsert.length); // cow counter + length + content of the array
                        OffHeapLongArray.set(doubleArrayToInsert_ptr, 0, 1);// set cow counter
                        OffHeapLongArray.set(doubleArrayToInsert_ptr, 1, doubleArrayToInsert.length);// set length
                        for (int i = 0; i < doubleArrayToInsert.length; i++) {
                            OffHeapDoubleArray.set(doubleArrayToInsert_ptr, 2 + i, doubleArrayToInsert[i]);
                        }
                        OffHeapLongArray.set(addr, index, doubleArrayToInsert_ptr);
                    } else {
                        OffHeapLongArray.set(addr, index, CoreConstants.OFFHEAP_NULL_PTR);
                    }
                    break;
                case Type.LONG_ARRAY:
                    long[] longArrayToInsert = (long[]) elem;
                    if (longArrayToInsert != null) {
                        long longArrayToInsert_ptr = OffHeapLongArray.allocate(2 + longArrayToInsert.length); // cow counter + length + content of the array
                        OffHeapLongArray.set(longArrayToInsert_ptr, 0, 1);// init cow counter
                        OffHeapLongArray.set(longArrayToInsert_ptr, 1, longArrayToInsert.length);// set length
                        for (int i = 0; i < longArrayToInsert.length; i++) {
                            OffHeapLongArray.set(longArrayToInsert_ptr, 2 + i, longArrayToInsert[i]);
                        }
                        OffHeapLongArray.set(addr, index, longArrayToInsert_ptr);
                    } else {
                        OffHeapLongArray.set(addr, index, CoreConstants.OFFHEAP_NULL_PTR);
                    }
                    break;
                case Type.INT_ARRAY:
                    int[] intArrayToInsert = (int[]) elem;
                    if (intArrayToInsert != null) {
                        long intArrayToInsert_ptr = OffHeapLongArray.allocate(2 + intArrayToInsert.length); // cow counter + length + content of the array
                        OffHeapLongArray.set(intArrayToInsert_ptr, 0, 1);// init cow counter
                        OffHeapLongArray.set(intArrayToInsert_ptr, 1, intArrayToInsert.length);// set length
                        for (int i = 0; i < intArrayToInsert.length; i++) {
                            OffHeapLongArray.set(intArrayToInsert_ptr, 2 + i, intArrayToInsert[i]);
                        }
                        OffHeapLongArray.set(addr, index, intArrayToInsert_ptr);
                    } else {
                        OffHeapLongArray.set(addr, index, CoreConstants.OFFHEAP_NULL_PTR);
                    }
                    break;
                /** Maps */
                case Type.STRING_LONG_MAP:
                    long stringLongMap_ptr = ((ArrayStringLongMap) elem).rootAddress();
                    ArrayStringLongMap.incrementCopyOnWriteCounter(stringLongMap_ptr);
                    OffHeapLongArray.set(addr, index, stringLongMap_ptr);
                    break;
                case Type.LONG_LONG_MAP:
                    long longLongMap_ptr = ((ArrayLongLongMap) elem).rootAddress();
                    ArrayLongLongMap.incrementCopyOnWriteCounter(longLongMap_ptr);
                    OffHeapLongArray.set(addr, index, longLongMap_ptr);
                    break;
                case Type.LONG_LONG_ARRAY_MAP:
                    long longLongArrayMap_ptr = ((ArrayLongLongArrayMap) elem).rootAddress();
                    ArrayLongLongArrayMap.incrementCopyOnWriteCounter(longLongArrayMap_ptr);
                    OffHeapLongArray.set(addr, index, longLongArrayMap_ptr);
                    break;
                default:
                    throw new RuntimeException("Should never happen...");
            }
        } else {
            OffHeapLongArray.set(addr, index, CoreConstants.OFFHEAP_NULL_PTR);
            OffHeapLongArray.set(elementTypeAddr, index, CoreConstants.OFFHEAP_NULL_PTR);
        }

        // free the previous elements
        if (tempPtr != CoreConstants.OFFHEAP_NULL_PTR) {
            freeElement(tempPtr, tempType);
        }
        if (elem != null) {
            OffHeapLongArray.set(elementTypeAddr, index, elemType);
        }
    }

    private void internal_set_dirty() {
        if (this._space != null) {
            if ((OffHeapLongArray.get(root_array_ptr, INDEX_FLAGS) & CoreConstants.DIRTY_BIT) != CoreConstants.DIRTY_BIT) {
                this._space.declareDirty(this);
            }
        }
    }

    @Override
    public final Object get(long index) {
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 0, 1)) ;
        try {
            consistencyCheck();
            long elementDataSize = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
            if (elementDataSize == 0) {
                return null;
            }
            long hashIndex = PrimitiveHelper.longHash(index, elementDataSize);
            long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            while (m >= 0) {
                if (index == OffHeapLongArray.get(elementK_ptr, m) /* getKey */) {
                    return internal_getElementV(m); /* getValue */
                } else {
                    m = OffHeapLongArray.get(elementNext_ptr + 8, m);
                }
            }
            return null;
        } finally {
            if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
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

    private Object internal_getElementV(long index) {
        byte elemType = (byte) OffHeapLongArray.get(elementType_ptr, index); // can be safely casted
        switch (elemType) {
            /** Primitives */
            case Type.BOOL:
                return OffHeapLongArray.get(elementV_ptr, index) == 1 ? true : false;
            case Type.DOUBLE:
                return OffHeapDoubleArray.get(elementV_ptr, index); // no indirection, value is directly inside
            case Type.LONG:
                return OffHeapLongArray.get(elementV_ptr, index);  // no indirection, value is directly inside
            case Type.INT:
                return (int) OffHeapLongArray.get(elementV_ptr, index); // no indirection, value is directly inside
            /** String */
            case Type.STRING:
                long elemStringPtr = OffHeapLongArray.get(elementV_ptr, index);
                if (elemStringPtr == CoreConstants.OFFHEAP_NULL_PTR) {
                    return null;
                }
                int length = unsafe.getInt(elemStringPtr + 8); //cow counter
                byte[] bytes = new byte[length];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = unsafe.getByte(elemStringPtr + 4 + 8 + i);
                }
                return new String(bytes);
            /** Arrays */
            case Type.DOUBLE_ARRAY:
                long elemDoublePtr = OffHeapLongArray.get(elementV_ptr, index);
                if (elemDoublePtr == CoreConstants.OFFHEAP_NULL_PTR) {
                    return null;
                }
                int doubleArrayLength = (int) OffHeapLongArray.get(elemDoublePtr, 1); // can be safely casted
                double[] doubleArray = new double[doubleArrayLength];
                for (int i = 0; i < doubleArrayLength; i++) {
                    doubleArray[i] = OffHeapDoubleArray.get(elemDoublePtr, 2 + i);
                }
                return doubleArray;
            case Type.LONG_ARRAY:
                long elemLongPtr = OffHeapLongArray.get(elementV_ptr, index);
                if (elemLongPtr == CoreConstants.OFFHEAP_NULL_PTR) {
                    return null;
                }
                int longArrayLength = (int) OffHeapLongArray.get(elemLongPtr, 1); // can be safely casted
                long[] longArray = new long[longArrayLength];
                for (int i = 0; i < longArrayLength; i++) {
                    longArray[i] = OffHeapLongArray.get(elemLongPtr, 2 + i);
                }
                return longArray;
            case Type.INT_ARRAY:
                long elemIntPtr = OffHeapLongArray.get(elementV_ptr, index);
                if (elemIntPtr == CoreConstants.OFFHEAP_NULL_PTR) {
                    return null;
                }
                int intArrayLength = (int) OffHeapLongArray.get(elemIntPtr, 1); // can be safely casted
                int[] intArray = new int[intArrayLength];
                for (int i = 0; i < intArrayLength; i++) {
                    intArray[i] = (int) OffHeapLongArray.get(elemIntPtr, 2 + i);
                }
                return intArray;
            /** Maps */
            case Type.STRING_LONG_MAP:
                long elemStringLongMapPtr = OffHeapLongArray.get(elementV_ptr, index);
                return new ArrayStringLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, elemStringLongMapPtr);
            case Type.LONG_LONG_MAP:
                long elemLongLongMapPtr = OffHeapLongArray.get(elementV_ptr, index);
                return new ArrayLongLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, elemLongLongMapPtr);
            case Type.LONG_LONG_ARRAY_MAP:
                long elemLongLongArrayMapPtr = OffHeapLongArray.get(elementV_ptr, index);
                return new ArrayLongLongArrayMap(this, CoreConstants.MAP_INITIAL_CAPACITY, elemLongLongArrayMapPtr);
            case CoreConstants.OFFHEAP_NULL_PTR:
                return null;
            default:
                throw new RuntimeException("Should never happen");
        }
    }

    @Override
    public final Object getOrCreate(long index, byte elemType) {
        Object previousObject = get(index);
        byte previousType = getType(index);
        if (previousObject != null && previousType == elemType) {
            return previousObject;
        }
        switch (elemType) {
            case Type.STRING_LONG_MAP:
                internal_set(index, elemType, new ArrayStringLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, CoreConstants.OFFHEAP_NULL_PTR), false);
                break;
            case Type.LONG_LONG_MAP:
                internal_set(index, elemType, new ArrayLongLongMap(this, CoreConstants.MAP_INITIAL_CAPACITY, CoreConstants.OFFHEAP_NULL_PTR), false);
                break;
            case Type.LONG_LONG_ARRAY_MAP:
                internal_set(index, elemType, new ArrayLongLongArrayMap(this, CoreConstants.MAP_INITIAL_CAPACITY, CoreConstants.OFFHEAP_NULL_PTR), false);
                break;
        }
        return get(index);

    }

    @Override
    public Object getOrCreateFromKey(String key, byte elemType) {
        return getOrCreate(_space.graph().resolver().stringToHash(key, true), elemType);
    }

    @Override
    public final byte getType(long index) {
        long elementDataSize = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        if (elementDataSize == 0) {
            return -1;
        }
        long hashIndex = PrimitiveHelper.longHash(index, elementDataSize);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m >= 0) {
            if (index == OffHeapLongArray.get(elementK_ptr, m) /* getKey */) {
                return (byte) OffHeapLongArray.get(elementType_ptr, m); /* getValue */
            } else {
                m = OffHeapLongArray.get(elementNext_ptr + 8, m);
            }
        }
        return -1;
    }

    @Override
    public byte getTypeFromKey(String key) {
        return getType(_space.graph().resolver().stringToHash(key, false));
    }

    private static long incrementCopyOnWriteCounter(long root_addr) {
        long elemNext_ptr = OffHeapLongArray.get(root_addr, INDEX_ELEMENT_NEXT);
        return unsafe.getAndAddLong(null, elemNext_ptr, 1) + 1;
    }

    private static long decrementCopyOnWriteCounter(long root_addr) {
        long elemNext_ptr = OffHeapLongArray.get(root_addr, INDEX_ELEMENT_NEXT);
        return unsafe.getAndAddLong(null, elemNext_ptr, -1) - 1;
    }
}
