package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KLongLongMapCallBack;
import org.mwdb.chunk.KWorldOrderChunk;
import org.mwdb.utility.Base64;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapWorldOrderChunk implements KWorldOrderChunk, KOffHeapChunk {

    private final KChunkListener listener;
    private final long rootPtr;

    /**
     * Global KChunk indexes
     */
    private static final int INDEX_WORLD = Constants.OFFHEAP_CHUNK_INDEX_WORLD;
    private static final int INDEX_TIME = Constants.OFFHEAP_CHUNK_INDEX_TIME;
    private static final int INDEX_ID = Constants.OFFHEAP_CHUNK_INDEX_ID;
    private static final int INDEX_TYPE = Constants.OFFHEAP_CHUNK_INDEX_TYPE;
    private static final int INDEX_FLAGS = Constants.OFFHEAP_CHUNK_INDEX_FLAGS;
    private static final int INDEX_MARKS = Constants.OFFHEAP_CHUNK_INDEX_MARKS;

    //LongArrays
    private static final int INDEX_ELEMENT_V = 6;
    private static final int INDEX_ELEMENT_NEXT = 7;
    private static final int INDEX_ELEMENT_HASH = 8;
    private static final int INDEX_ELEMENT_K = 9;
    //Long values
    private static final int INDEX_LOCK = 10;
    private static final int INDEX_THRESHOLD = 11;
    private static final int INDEX_SIZE = 12;
    private static final int INDEX_CAPACITY = 13;
    private static final int INDEX_MAGIC = 14;

    //long[]
    private long elementK_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;

    public OffHeapWorldOrderChunk(KChunkListener listener, long previousAddr, String initialString) {
        this.listener = listener;

        if (previousAddr != Constants.OFFHEAP_NULL_PTR) {
            this.rootPtr = previousAddr;
            elementK_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_V);
            elementHash_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_NEXT);
        } else if (initialString != null) {
            this.rootPtr = OffHeapLongArray.allocate(15);
            load(initialString);
        } else {
            this.rootPtr = OffHeapLongArray.allocate(15);
            long initialCapacity = Constants.MAP_INITIAL_CAPACITY;
            /** Init long variables */
            //init lock
            OffHeapLongArray.set(this.rootPtr, INDEX_LOCK, 0);
            //init flags
            OffHeapLongArray.set(this.rootPtr, INDEX_FLAGS, 0);
            //init capacity
            OffHeapLongArray.set(this.rootPtr, INDEX_CAPACITY, initialCapacity);
            //init threshold
            OffHeapLongArray.set(this.rootPtr, INDEX_THRESHOLD, (long) (initialCapacity * Constants.MAP_LOAD_FACTOR));
            //init elementCount
            OffHeapLongArray.set(this.rootPtr, INDEX_SIZE, 0);

            /** Init Long[] variables */
            //init elementK
            elementK_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_K, elementK_ptr);
            //init elementV
            elementV_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_V, elementV_ptr);
            //init elementNext
            elementNext_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_NEXT, elementNext_ptr);
            //init elementHash
            elementHash_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_HASH, elementHash_ptr);
        }

    }

    @Override
    public long addr() {
        return rootPtr;
    }

    @Override
    public void free() {
        OffHeapLongArray.free(elementK_ptr);
        OffHeapLongArray.free(elementV_ptr);
        OffHeapLongArray.free(elementNext_ptr);
        OffHeapLongArray.free(elementHash_ptr);
        OffHeapLongArray.free(rootPtr);
    }

    public static void free(long addr) {
        //free all long[]
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_K));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_V));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_NEXT));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_HASH));
        //free master array
        OffHeapLongArray.free(addr);
    }

    @Override
    public long magic() {
        return OffHeapLongArray.get(rootPtr, INDEX_MAGIC);
    }

    @Override
    public long world() {
        return OffHeapLongArray.get(rootPtr, INDEX_WORLD);
    }

    @Override
    public long id() {
        return OffHeapLongArray.get(rootPtr, INDEX_ID);
    }

    @Override
    public long time() {
        return OffHeapLongArray.get(rootPtr, INDEX_TIME);
    }

    @Override
    public long marks() {
        return OffHeapLongArray.get(rootPtr, INDEX_MARKS);
    }

    @Override
    public byte chunkType() {
        return Constants.WORLD_ORDER_CHUNK;
    }

    @Override
    public long flags() {
        return OffHeapLongArray.get(rootPtr, INDEX_FLAGS);
    }

    private void consistencyCheck() {
        if (OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_V) != elementV_ptr) {
            elementK_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_V);
            elementHash_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_NEXT);
        }
    }

    @Override
    public final long get(long key) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        consistencyCheck();

        long hashIndex = PrimitiveHelper.longHash(key, OffHeapLongArray.get(this.rootPtr, INDEX_CAPACITY));
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        long result = Constants.NULL_LONG;
        while (m != -1) {
            if (key == OffHeapLongArray.get(elementK_ptr, m)) {
                result = OffHeapLongArray.get(elementV_ptr, m);
                break;
            }
            m = OffHeapLongArray.get(elementNext_ptr, m);
        }

        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }

        return result;
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        consistencyCheck();

        long elementCount = OffHeapLongArray.get(this.rootPtr, INDEX_SIZE);
        for (long i = 0; i < elementCount; i++) {
            long loopValue = OffHeapLongArray.get(elementV_ptr, i);
            if (loopValue != Constants.NULL_LONG) {
                callback.on(OffHeapLongArray.get(elementK_ptr, i), loopValue);
            }
        }

        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
    }

    @Override
    public long size() {
        return OffHeapLongArray.get(this.rootPtr, INDEX_SIZE);
    }

    @Override
    public void remove(long key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    @Override
    public final void put(long key, long value) {
        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        consistencyCheck();

        long entry = -1;
        long capacity = OffHeapLongArray.get(rootPtr, INDEX_CAPACITY);
        long count = OffHeapLongArray.get(rootPtr, INDEX_SIZE);
        long hashIndex = PrimitiveHelper.longHash(key, capacity);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            if (key == OffHeapLongArray.get(elementK_ptr, m)) {
                entry = m;
                break;
            }
            m = OffHeapLongArray.get(elementNext_ptr, m);
        }
        if (entry == -1) {
            //if need to reHash (too small or too much collisions)
            if ((count + 1) > OffHeapLongArray.get(rootPtr, INDEX_THRESHOLD)) {

                long newCapacity = capacity << 1;
                //reallocate the string[], indexes are not changed
                elementK_ptr = OffHeapStringArray.reallocate(elementK_ptr, capacity, newCapacity);
                OffHeapLongArray.set(rootPtr, INDEX_ELEMENT_K, elementK_ptr);
                //reallocate the long[] values
                elementV_ptr = OffHeapLongArray.reallocate(elementV_ptr, capacity, newCapacity);
                OffHeapLongArray.set(rootPtr, INDEX_ELEMENT_V, elementV_ptr);

                //Create two new Hash and Next structures
                OffHeapLongArray.free(elementHash_ptr);
                OffHeapLongArray.free(elementNext_ptr);
                elementHash_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(rootPtr, INDEX_ELEMENT_HASH, elementHash_ptr);
                elementNext_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(rootPtr, INDEX_ELEMENT_NEXT, elementNext_ptr);

                //rehashEveryThing
                for (long i = 0; i < count; i++) {
                    long previousValue = OffHeapLongArray.get(elementV_ptr, i);
                    long previousKey = OffHeapLongArray.get(elementK_ptr, i);
                    if (previousValue != Constants.NULL_LONG) {
                        long newHashIndex = PrimitiveHelper.longHash(previousKey, newCapacity);
                        long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, newHashIndex);
                        if (currentHashedIndex != Constants.OFFHEAP_NULL_PTR) {
                            OffHeapLongArray.set(elementNext_ptr, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(elementHash_ptr, newHashIndex, i);
                    }
                }

                capacity = newCapacity;
                OffHeapLongArray.set(rootPtr, INDEX_CAPACITY, capacity);
                OffHeapLongArray.set(rootPtr, INDEX_THRESHOLD, (long) (newCapacity * Constants.MAP_LOAD_FACTOR));
                hashIndex = PrimitiveHelper.longHash(key, capacity);
            }
            //set K and associated K_H
            OffHeapLongArray.set(elementK_ptr, count, key);
            //set value or index if null
            if (value == Constants.NULL_LONG) {
                OffHeapLongArray.set(elementV_ptr, count, count);
            } else {
                OffHeapLongArray.set(elementV_ptr, count, value);
            }
            long currentHashedElemIndex = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            if (currentHashedElemIndex != -1) {
                OffHeapLongArray.set(elementNext_ptr, count, currentHashedElemIndex);
            }
            //now the object is reachable to other thread everything should be ready
            OffHeapLongArray.set(elementHash_ptr, hashIndex, count);
            //increase element count
            OffHeapLongArray.set(rootPtr, INDEX_SIZE, count + 1);
            internal_set_dirty();
        } else {
            if (OffHeapLongArray.get(elementV_ptr, entry) != value && value != Constants.NULL_LONG) {
                //setValue
                OffHeapLongArray.set(elementV_ptr, entry, value);
                internal_set_dirty();
            }
        }
        if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
    }

    @Override
    public String save() {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        consistencyCheck();

        final StringBuilder buffer = new StringBuilder();
        long size = OffHeapLongArray.get(rootPtr, INDEX_SIZE);
        Base64.encodeLongToBuffer(size, buffer);
        buffer.append(Constants.CHUNK_SEP);
        boolean isFirst = true;

        long elementCount = OffHeapLongArray.get(this.rootPtr, INDEX_SIZE);
        for (long i = 0; i < elementCount; i++) {
            long loopValue = OffHeapLongArray.get(elementV_ptr, i);
            if (loopValue != Constants.NULL_LONG) {
                if (!isFirst) {
                    buffer.append(Constants.CHUNK_SUB_SEP);
                }
                isFirst = false;
                Base64.encodeLongToBuffer(OffHeapLongArray.get(elementK_ptr, i), buffer);
                buffer.append(Constants.CHUNK_SUB_SUB_SEP);
                Base64.encodeLongToBuffer(loopValue, buffer);
            }
        }

        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
        return buffer.toString();
    }

    private void load(String payload) {

        int cursor = 0;

        long loopKey = Constants.NULL_LONG;
        int previousStart = -1;
        long capacity = -1;
        int insertIndex = 0;
        while (cursor < payload.length()) {

            if (payload.charAt(cursor) == Constants.CHUNK_SEP) {
                long size = Base64.decodeToLongWithBounds(payload, 0, cursor);
                if(size == 0){
                    capacity = 1;
                } else {
                    capacity = size << 1;
                }

                //init lock
                OffHeapLongArray.set(this.rootPtr, INDEX_LOCK, 0);
                //init flags
                OffHeapLongArray.set(this.rootPtr, INDEX_FLAGS, 0);
                //init capacity
                OffHeapLongArray.set(this.rootPtr, INDEX_CAPACITY, capacity);
                //init threshold
                OffHeapLongArray.set(this.rootPtr, INDEX_THRESHOLD, (long) (capacity * Constants.MAP_LOAD_FACTOR));
                //init elementCount
                OffHeapLongArray.set(this.rootPtr, INDEX_SIZE, size);
                //init elementK
                elementK_ptr = OffHeapLongArray.allocate(capacity);
                OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_K, elementK_ptr);
                //init elementV
                elementV_ptr = OffHeapLongArray.allocate(capacity);
                OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_V, elementV_ptr);
                //init elementNext
                elementNext_ptr = OffHeapLongArray.allocate(capacity);
                OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_NEXT, elementNext_ptr);
                //init elementHash
                elementHash_ptr = OffHeapLongArray.allocate(capacity);
                OffHeapLongArray.set(this.rootPtr, INDEX_ELEMENT_HASH, elementHash_ptr);

                //reset for next round
                previousStart = cursor + 1;

            } else if (payload.charAt(cursor) == Constants.CHUNK_SUB_SEP) {
                if (loopKey != Constants.NULL_LONG) {
                    long loopValue = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                    //insert raw
                    OffHeapLongArray.set(elementK_ptr, insertIndex, loopKey);
                    OffHeapLongArray.set(elementV_ptr, insertIndex, loopValue);
                    //insert hash
                    long hashIndex = PrimitiveHelper.longHash(loopKey, capacity);
                    long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, hashIndex);
                    if (currentHashedIndex != -1) {
                        OffHeapLongArray.set(elementNext_ptr, insertIndex, currentHashedIndex);
                    }
                    OffHeapLongArray.set(elementHash_ptr, hashIndex, insertIndex);
                    insertIndex++;
                    //reset key for next round
                    loopKey = Constants.NULL_LONG;
                }
                previousStart = cursor + 1;
            } else if (payload.charAt(cursor) == Constants.CHUNK_SUB_SUB_SEP) {
                loopKey = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
                previousStart = cursor + 1;
            }
            //loop in all case
            cursor++;
        }
        if (loopKey != Constants.NULL_LONG) {
            long loopValue = Base64.decodeToLongWithBounds(payload, previousStart, cursor);
            //insert raw
            OffHeapLongArray.set(elementK_ptr, insertIndex, loopKey);
            OffHeapLongArray.set(elementV_ptr, insertIndex, loopValue);
            //insert hash
            long hashIndex = PrimitiveHelper.longHash(loopKey, capacity);
            long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            if (currentHashedIndex != -1) {
                OffHeapLongArray.set(elementNext_ptr, insertIndex, currentHashedIndex);
            }
            OffHeapLongArray.set(elementHash_ptr, hashIndex, insertIndex);
        }


    }

    private void internal_set_dirty() {
        if (listener != null) {
            if ((OffHeapLongArray.get(rootPtr, INDEX_FLAGS) & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                listener.declareDirty(this);
            }
        }
    }

}
