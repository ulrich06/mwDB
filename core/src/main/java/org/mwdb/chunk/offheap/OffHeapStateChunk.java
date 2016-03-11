package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.KType;
import org.mwdb.chunk.*;
import org.mwdb.plugin.KResolver;
import org.mwdb.utility.Base64;
import org.mwdb.utility.PrimitiveHelper;

/**
 * @ignore ts
 * Memory layout: all structures are memory blocks of either primitive values (as longs)
 * or pointers to memory blocks
 */
// TODO check synchronization
public class OffHeapStateChunk implements KStateChunk, KChunkListener {
    // keys
    private static final int INDEX_WORLD = Constants.OFFHEAP_CHUNK_INDEX_WORLD;
    private static final int INDEX_TIME = Constants.OFFHEAP_CHUNK_INDEX_TIME;
    private static final int INDEX_ID = Constants.OFFHEAP_CHUNK_INDEX_ID;
    private static final int INDEX_TYPE = Constants.OFFHEAP_CHUNK_INDEX_TYPE;
    private static final int INDEX_FLAGS = Constants.OFFHEAP_CHUNK_INDEX_FLAGS;
    private static final int INDEX_MARKS = Constants.OFFHEAP_CHUNK_INDEX_MARKS;


    // long arrays
    private static final int INDEX_ELEMENT_K = 6;
    private static final int INDEX_ELEMENT_V = 7;
    private static final int INDEX_ELEMENT_NEXT = 8;
    private static final int INDEX_ELEMENT_HASH = 9;
    private static final int INDEX_ELEMENT_TYPE = 10;

    // long values
    private static final int INDEX_COUNTER = 11;
    private static final int INDEX_IN_LOAD_MODE = 12;
    private static final int INDEX_ELEMENT_DATA_SIZE = 13;
    private static final int INDEX_ELEMENT_THRESHOLD = 14;
    private static final int INDEX_ELEMENT_COUNT = 15;

    //pointer values
    private final KChunkListener _listener;
    private long elementK_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;
    private long elementType_ptr;
    private final long root_array_ptr;

    public OffHeapStateChunk(long world, long time, long id, KChunkListener listener, long previousAddr) {
        _listener = listener;
        if (previousAddr == Constants.OFFHEAP_NULL_PTR) {
            root_array_ptr = OffHeapLongArray.allocate(16);
            /** init long values */
            OffHeapLongArray.set(root_array_ptr, INDEX_IN_LOAD_MODE, 0);
            OffHeapLongArray.set(root_array_ptr, INDEX_WORLD, world);
            OffHeapLongArray.set(root_array_ptr, INDEX_TIME, time);
            OffHeapLongArray.set(root_array_ptr, INDEX_ID, id);
            OffHeapLongArray.set(root_array_ptr, INDEX_FLAGS, 0);
            OffHeapLongArray.set(root_array_ptr, INDEX_COUNTER, 0);
            long initialCapacity = Constants.MAP_INITIAL_CAPACITY;
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, 0);
            long threshold = (long) (initialCapacity * Constants.MAP_LOAD_FACTOR);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_THRESHOLD, threshold);

            /** init long[] variables */
            elementK_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
            elementV_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);
            elementNext_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);
            elementHash_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
            elementType_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, elementType_ptr);

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
    public void cloneFrom(KStateChunk origin) {
        //brutal cast, but mixed implementation is not allowed per space
        OffHeapStateChunk casted = (OffHeapStateChunk) origin;
        long elementDataSize = OffHeapLongArray.get(casted.root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        long elementCount = OffHeapLongArray.get(casted.root_array_ptr, INDEX_ELEMENT_COUNT);

        long clonedElementK_ptr = OffHeapLongArray.allocate(elementDataSize);
        OffHeapLongArray.copy(casted.elementK_ptr, clonedElementK_ptr, elementDataSize);
        long clonedElementV_ptr = OffHeapLongArray.allocate(elementDataSize);
        OffHeapLongArray.copy(casted.elementV_ptr, clonedElementV_ptr, elementDataSize);
        long clonedElementNext_ptr = OffHeapLongArray.allocate(elementDataSize);
        OffHeapLongArray.copy(casted.elementNext_ptr, clonedElementNext_ptr, elementDataSize);
        long clonedElementHash_ptr = OffHeapLongArray.allocate(elementDataSize);
        OffHeapLongArray.copy(casted.elementHash_ptr, clonedElementHash_ptr, elementDataSize);
        long clonedElementType_ptr = OffHeapLongArray.allocate(elementDataSize);
        OffHeapLongArray.copy(casted.elementType_ptr, clonedElementType_ptr, elementDataSize);

        OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K));
        OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V));
        OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT));
        OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH));
        OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE));

        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, elementDataSize);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, elementCount);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, clonedElementK_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, clonedElementV_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, clonedElementNext_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, clonedElementHash_ptr);
        OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, clonedElementType_ptr);

        elementK_ptr = clonedElementK_ptr;
        elementV_ptr = clonedElementV_ptr;
        elementNext_ptr = clonedElementNext_ptr;
        elementHash_ptr = clonedElementHash_ptr;
        elementType_ptr = clonedElementType_ptr;

    }

    @Override
    public void each(KStateChunkCallBack callBack, KResolver resolver) {
        for (int i = 0; i < OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT); i++) {
            if (OffHeapLongArray.get(elementV_ptr, i) != Constants.OFFHEAP_NULL_PTR) {
                callBack.on(resolver.longKeyToString(OffHeapLongArray.get(elementK_ptr, i)),
                        (int) OffHeapLongArray.get(elementType_ptr, i),
                        OffHeapLongArray.get(elementV_ptr, i));
            }
        }
    }

    @Override
    public long world() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_WORLD);
    }

    @Override
    public long time() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_TIME);
    }

    @Override
    public long id() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_ID);
    }

    @Override
    public String save() {
        final StringBuilder buffer = new StringBuilder();
        long elementCount = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
        Base64.encodeLongToBuffer(elementCount, buffer);
        for (int i = 0; i < elementCount; i++) {
            if (OffHeapLongArray.get(elementV_ptr, i) != Constants.OFFHEAP_NULL_PTR) { //there is a real value
                long loopKey = OffHeapLongArray.get(elementK_ptr, i);
                Object loopValue = internal_getElementV(i);
                if (loopValue != null) {
                    buffer.append(Constants.CHUNK_SEP);
                    Base64.encodeLongToBuffer(loopKey, buffer);
                    buffer.append(Constants.CHUNK_SUB_SEP);
                    /** Encode to type of elem, for unSerialization */
                    byte elementType = (byte) OffHeapLongArray.get(elementType_ptr, i); // can be safely casted
                    Base64.encodeLongToBuffer(elementType, buffer);
                    buffer.append(Constants.CHUNK_SUB_SEP);
                    switch (elementType) {
                        /** Primitive Types */
                        case KType.STRING:
                            Base64.encodeStringToBuffer((String) loopValue, buffer);
                            break;
                        case KType.BOOL:
                            if ((boolean) loopValue) {
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

    @Override
    public void load(String payload) {

    }

    @Override
    public int marks() {
        return (int) OffHeapLongArray.get(root_array_ptr, INDEX_COUNTER);
    }

    @Override
    public int mark() {
        return (int) OffHeapLongArray.incrementAndGet(root_array_ptr, INDEX_COUNTER);
    }

    @Override
    public int unmark() {
        return (int) OffHeapLongArray.decrementAndGet(root_array_ptr, INDEX_COUNTER);
    }

    public void free() {
        // TODO
    }

    @Override
    public long flags() {
        return OffHeapLongArray.get(root_array_ptr, INDEX_FLAGS);
    }

    @Override
    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = OffHeapLongArray.get(root_array_ptr, INDEX_FLAGS);
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_FLAGS, val, nval));
    }

    @Override
    public byte chunkType() {
        return Constants.STATE_CHUNK;
    }

    @Override
    public void declareDirty(KChunk chunk) {
        if (OffHeapLongArray.get(root_array_ptr, INDEX_IN_LOAD_MODE) == 0) {
            internal_set_dirty();
        }
    }

    @Override
    public void set(long index, byte elemType, Object elem) {
        internal_set(index, elemType, elem, true);

    }

    // TODO synchronize method
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
                m = OffHeapLongArray.get(elementNext_ptr, m);
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

                OffHeapLongArray.copy(OffHeapLongArray.get(root_array_ptr, elementK_ptr), newElementK_ptr, elementDataSize);
                OffHeapLongArray.copy(OffHeapLongArray.get(root_array_ptr, elementV_ptr), newElementV_ptr, elementDataSize);
                OffHeapLongArray.copy(OffHeapLongArray.get(root_array_ptr, elementType_ptr), newElementType_ptr, elementDataSize);
                long newElementNext_ptr = OffHeapLongArray.allocate(newLength);
                long newElementHash_ptr = OffHeapLongArray.allocate(newLength);

                //rehashEveryThing
                for (int i = 0; i < elementDataSize; i++) {
                    if (OffHeapLongArray.get(root_array_ptr, elementV_ptr) != Constants.OFFHEAP_NULL_PTR) { //there is a real value
                        long keyHash = PrimitiveHelper.longHash(OffHeapLongArray.get(elementK_ptr, i), newLength);
                        long currentHashedIndex = OffHeapLongArray.get(newElementHash_ptr, keyHash);
                        if (currentHashedIndex != -1) {
                            OffHeapLongArray.set(newElementNext_ptr, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(newElementHash_ptr, keyHash, i);
                    }
                }

                OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K));
                OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V));
                OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT));
                OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH));
                OffHeapLongArray.free(OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE));

                //setPrimitiveType value for all
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_DATA_SIZE, newLength);
                // elementCount stays the same
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_THRESHOLD, (long) (newLength * Constants.MAP_LOAD_FACTOR));
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, newElementK_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, newElementV_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, newElementHash_ptr);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_TYPE, newElementType_ptr);

                elementK_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_K);
                elementV_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_V);
                elementNext_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_NEXT);
                elementHash_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_HASH);
                elementType_ptr = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_TYPE);

                hashIndex = PrimitiveHelper.longHash(p_elementIndex, OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_DATA_SIZE));
            }
            long newIndex = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT) + 1);
            OffHeapLongArray.set(elementK_ptr, newIndex, p_elementIndex);
            internal_setElementV(newIndex, p_elemType, param_elem);
            OffHeapLongArray.set(elementType_ptr, newIndex, p_elemType);

            long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            if (currentHashedIndex != -1) {
                OffHeapLongArray.set(elementNext_ptr, newIndex, currentHashedIndex);
            }
            //now the object is reachable to other thread everything should be ready
            OffHeapLongArray.set(elementHash_ptr, hashIndex, newIndex);
        } else {
            if (replaceIfPresent) {
                internal_setElementV(entry, p_elemType, param_elem); /*setValue*/
                OffHeapLongArray.set(elementType_ptr, entry, p_elemType);
            }
        }
        internal_set_dirty();
    }

    // TODO before we set something, we first have to check what was there before and free the memory in case
    private void internal_setElementV(long index, byte elemType, Object elem) {
        // no additional check needed, we are sure it is one of these types
        switch (elemType) {
            /** Primitives */
            case KType.BOOL:
                OffHeapLongArray.set(elementV_ptr, index, ((boolean) elem) ? 1 : 0);
                break;
            case KType.DOUBLE:
                OffHeapDoubleArray.set(elementV_ptr, index, ((double) elem));
                break;
            case KType.LONG:
                OffHeapLongArray.set(elementV_ptr, index, ((long) elem));
                break;
            case KType.INT:
                OffHeapLongArray.set(elementV_ptr, index, ((int) elem));
                break;
            case KType.STRING:
                String stringToInsert = (String) elem;
                long stringToInsert_ptr = OffHeapStringArray.allocate(stringToInsert.length());
                OffHeapStringArray.set(stringToInsert_ptr, 0, stringToInsert);
                OffHeapLongArray.set(elementV_ptr, index, stringToInsert_ptr);
                break;
            /** Arrays */
            case KType.DOUBLE_ARRAY:
                double[] doubleArrayToInsert = (double[]) elem;
                long doubleArrayToInsert_ptr = OffHeapDoubleArray.allocate(1 + doubleArrayToInsert.length); // length + content of the array
                OffHeapLongArray.set(doubleArrayToInsert_ptr, 0, doubleArrayToInsert.length);// set length
                for (int i = 0; i < doubleArrayToInsert.length; i++) {
                    OffHeapDoubleArray.set(doubleArrayToInsert_ptr, 1 + i, doubleArrayToInsert[i]);
                }
                OffHeapLongArray.set(elementV_ptr, index, doubleArrayToInsert_ptr);
                break;
            case KType.LONG_ARRAY:
                long[] longArrayToInsert = (long[]) elem;
                long longArrayToInsert_ptr = OffHeapLongArray.allocate(1 + longArrayToInsert.length); // length + content of the array
                OffHeapLongArray.set(longArrayToInsert_ptr, 0, longArrayToInsert.length);// set length
                for (int i = 0; i < longArrayToInsert.length; i++) {
                    OffHeapLongArray.set(longArrayToInsert_ptr, 1 + i, longArrayToInsert[i]);
                }
                OffHeapLongArray.set(elementV_ptr, index, longArrayToInsert_ptr);
                break;
            case KType.INT_ARRAY:
                int[] intArrayToInsert = (int[]) elem;
                long intArrayToInsert_ptr = OffHeapLongArray.allocate(1 + intArrayToInsert.length); // length + content of the array
                OffHeapLongArray.set(intArrayToInsert_ptr, 0, intArrayToInsert.length);// set length
                for (int i = 0; i < intArrayToInsert.length; i++) {
                    OffHeapLongArray.set(intArrayToInsert_ptr, 1 + i, intArrayToInsert[i]);
                }
                OffHeapLongArray.set(elementV_ptr, index, intArrayToInsert_ptr);
                break;
            /** Maps */
            case KType.STRING_LONG_MAP:
                long stringLongMap_ptr = ((ArrayStringLongMap) elem).rootAddress();// cast directly to the offheap map
                OffHeapLongArray.set(elementV_ptr, index, stringLongMap_ptr);
                break;
            case KType.LONG_LONG_MAP:
                long longLongMap_ptr = ((ArrayLongLongMap) elem).rootAddress();// cast directly to the offheap map
                OffHeapLongArray.set(elementV_ptr, index, longLongMap_ptr);
                break;
            case KType.LONG_LONG_ARRAY_MAP:
                long longLongArrayMap_ptr = ((ArrayLongLongArrayMap) elem).rootAddress();// cast directly to the offheap map
                OffHeapLongArray.set(elementV_ptr, index, longLongArrayMap_ptr);
                break;
            default:

        }
    }

    private void internal_set_dirty() {
        if (this._listener != null) {
            if ((OffHeapLongArray.get(root_array_ptr, INDEX_FLAGS) & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                this._listener.declareDirty(this);
            }
        }
    }


    @Override
    public Object get(long index) {
        long elementDataSize = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_DATA_SIZE);
        if (elementDataSize == 0) {
            return null;
        }
        long hashIndex = PrimitiveHelper.longHash(index, elementDataSize);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m >= 0) {
            if (index == OffHeapLongArray.get(elementK_ptr, m) /* getKey */) {
                return internal_getElementV(index); /* getValue */
            } else {
                m = OffHeapLongArray.get(elementNext_ptr, m);
            }
        }
        return null;
    }

    public Object internal_getElementV(long index) {
        // TODO
        return null;
    }

    @Override
    public Object getOrCreate(long index, byte elemType) {
        return null;
    }


    @Override
    public int getType(long index) {
        return 0;
    }
}
