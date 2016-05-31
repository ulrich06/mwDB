package org.mwg.core.chunk.offheap;

import org.mwg.core.CoreConstants;
import org.mwg.plugin.ChunkType;
import org.mwg.struct.Buffer;
import org.mwg.core.chunk.ChunkListener;
import org.mwg.struct.LongLongMapCallBack;
import org.mwg.core.chunk.WorldOrderChunk;
import org.mwg.plugin.Base64;
import org.mwg.core.utility.PrimitiveHelper;

/**
 * @ignore ts
 */
public class OffHeapWorldOrderChunk implements WorldOrderChunk, OffHeapChunk {

    private final ChunkListener listener;
    private final long rootPtr;

    /**
     * Global Chunk indexes
     */
    private static final int INDEX_WORLD = CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD;
    private static final int INDEX_TIME = CoreConstants.OFFHEAP_CHUNK_INDEX_TIME;
    private static final int INDEX_ID = CoreConstants.OFFHEAP_CHUNK_INDEX_ID;
    private static final int INDEX_TYPE = CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE;
    private static final int INDEX_FLAGS = CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS;
    private static final int INDEX_MARKS = CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS;

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
    private static final int INDEX_LOCK_EXT = 15;
    private static final int INDEX_EXTRA = 16;

    private static final int ROOT_SIZE = 17;

    //long[]
    private long elementK_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;

    public OffHeapWorldOrderChunk(ChunkListener listener, long previousAddr, Buffer initialString) {
        this.listener = listener;

        if (previousAddr != CoreConstants.OFFHEAP_NULL_PTR) {
            this.rootPtr = previousAddr;
            elementK_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_V);
            elementHash_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.rootPtr, INDEX_ELEMENT_NEXT);
        } else if (initialString != null) {
            this.rootPtr = OffHeapLongArray.allocate(ROOT_SIZE);
            load(initialString);
        } else {
            this.rootPtr = OffHeapLongArray.allocate(ROOT_SIZE);
            long initialCapacity = CoreConstants.MAP_INITIAL_CAPACITY;
            /** Init long variables */
            //init lock
            OffHeapLongArray.set(this.rootPtr, INDEX_LOCK, 0);
            //init flags
            OffHeapLongArray.set(this.rootPtr, INDEX_FLAGS, 0);
            //init capacity
            OffHeapLongArray.set(this.rootPtr, INDEX_CAPACITY, initialCapacity);
            //init threshold
            OffHeapLongArray.set(this.rootPtr, INDEX_THRESHOLD, (long) (initialCapacity * CoreConstants.MAP_LOAD_FACTOR));
            //init elementCount
            OffHeapLongArray.set(this.rootPtr, INDEX_SIZE, 0);

            OffHeapLongArray.set(this.rootPtr, INDEX_EXTRA, CoreConstants.NULL_LONG);

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
    public long extra() {
        return OffHeapLongArray.get(this.rootPtr, INDEX_EXTRA);
    }

    @Override
    public void setExtra(long extraValue) {
        OffHeapLongArray.set(this.rootPtr, INDEX_EXTRA, extraValue);
    }

    @Override
    public final void lock() {
        while (!OffHeapLongArray.compareAndSwap(this.rootPtr, INDEX_LOCK_EXT, -1, 1)) ;
    }

    @Override
    public final void unlock() {
        if (!OffHeapLongArray.compareAndSwap(this.rootPtr, INDEX_LOCK_EXT, 1, -1)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    @Override
    public final long addr() {
        return rootPtr;
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
    public final long magic() {
        return OffHeapLongArray.get(rootPtr, INDEX_MAGIC);
    }

    @Override
    public final long world() {
        return OffHeapLongArray.get(rootPtr, INDEX_WORLD);
    }

    @Override
    public final long id() {
        return OffHeapLongArray.get(rootPtr, INDEX_ID);
    }

    @Override
    public final long time() {
        return OffHeapLongArray.get(rootPtr, INDEX_TIME);
    }

    @Override
    public final long marks() {
        return OffHeapLongArray.get(rootPtr, INDEX_MARKS);
    }

    @Override
    public final byte chunkType() {
        return ChunkType.WORLD_ORDER_CHUNK;
    }

    @Override
    public final long flags() {
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
        long result = CoreConstants.NULL_LONG;
        try {
            consistencyCheck();

            long hashIndex = PrimitiveHelper.longHash(key, OffHeapLongArray.get(this.rootPtr, INDEX_CAPACITY));
            long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);

            while (m != -1) {
                if (key == OffHeapLongArray.get(elementK_ptr, m)) {
                    result = OffHeapLongArray.get(elementV_ptr, m);
                    break;
                }
                m = OffHeapLongArray.get(elementNext_ptr, m);
            }
        } finally {
            //UNLOCK
            if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
        }
        return result;
    }

    @Override
    public final void each(LongLongMapCallBack callback) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        try {
            consistencyCheck();

            long elementCount = OffHeapLongArray.get(this.rootPtr, INDEX_SIZE);
            for (long i = 0; i < elementCount; i++) {
                long loopValue = OffHeapLongArray.get(elementV_ptr, i);
                if (loopValue != CoreConstants.NULL_LONG) {
                    callback.on(OffHeapLongArray.get(elementK_ptr, i), loopValue);
                }
            }
        } finally {
            //UNLOCK
            if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
        }
    }

    @Override
    public final long size() {
        return OffHeapLongArray.get(this.rootPtr, INDEX_SIZE);
    }

    @Override
    public final void remove(long key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    @Override
    public void merge(Buffer buffer) {
        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        try {
            consistencyCheck();
            long cursor = 0;
            long bufferSize = buffer.size();
            boolean initDone = false;
            long previousStart = 0;
            long loopKey = CoreConstants.NULL_LONG;
            while (cursor < bufferSize) {
                if (buffer.read(cursor) == CoreConstants.CHUNK_SEP) {
                    if (!initDone) {
                        initDone = true;
                    } else {
                        //extra char read
                        OffHeapLongArray.set(this.rootPtr, INDEX_EXTRA, Base64.decodeToLongWithBounds(buffer, previousStart, cursor));
                    }
                    previousStart = cursor + 1;
                } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SEP) {
                    if (loopKey != CoreConstants.NULL_LONG) {
                        long loopValue = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                        internal_put(loopKey, loopValue);
                        //reset key for next round
                        loopKey = CoreConstants.NULL_LONG;
                    }
                    previousStart = cursor + 1;
                } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SUB_SEP) {
                    loopKey = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                    previousStart = cursor + 1;
                }
                //loop in all case
                cursor++;
            }
            if (loopKey != CoreConstants.NULL_LONG) {
                long loopValue = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                internal_put(loopKey, loopValue);
            }
        } finally {
            if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
            internal_set_dirty();
        }
    }

    @Override
    public final void put(long key, long value) {
        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        try {
            consistencyCheck();
            internal_put(key, value);
        } finally {
            if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
        }
    }

    private void internal_put(long key, long value) {
        long entry = -1;
        long capacity = OffHeapLongArray.get(rootPtr, INDEX_CAPACITY);
        long count = OffHeapLongArray.get(rootPtr, INDEX_SIZE);
        long hashIndex = PrimitiveHelper.longHash(key, capacity);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
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
                    if (previousValue != CoreConstants.NULL_LONG) {
                        long newHashIndex = PrimitiveHelper.longHash(previousKey, newCapacity);
                        long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, newHashIndex);
                        if (currentHashedIndex != CoreConstants.OFFHEAP_NULL_PTR) {
                            OffHeapLongArray.set(elementNext_ptr, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(elementHash_ptr, newHashIndex, i);
                    }
                }
                capacity = newCapacity;
                OffHeapLongArray.set(rootPtr, INDEX_CAPACITY, capacity);
                OffHeapLongArray.set(rootPtr, INDEX_THRESHOLD, (long) (newCapacity * CoreConstants.MAP_LOAD_FACTOR));
                hashIndex = PrimitiveHelper.longHash(key, capacity);
            }
            //set K and associated K_H
            OffHeapLongArray.set(elementK_ptr, count, key);
            //set value or index if null
            if (value == CoreConstants.NULL_LONG) {
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
            if (OffHeapLongArray.get(elementV_ptr, entry) != value && value != CoreConstants.NULL_LONG) {
                //setValue
                OffHeapLongArray.set(elementV_ptr, entry, value);
                internal_set_dirty();
            }
        }
    }


    @Override
    public final void save(Buffer buffer) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 0, 1)) ;
        try {
            consistencyCheck();

            long size = OffHeapLongArray.get(rootPtr, INDEX_SIZE);
            Base64.encodeLongToBuffer(size, buffer);
            buffer.write(CoreConstants.CHUNK_SEP);
            long extra = OffHeapLongArray.get(rootPtr, INDEX_EXTRA);
            if (extra != CoreConstants.NULL_LONG) {
                Base64.encodeLongToBuffer(extra, buffer);
                buffer.write(CoreConstants.CHUNK_SEP);
            }

            boolean isFirst = true;

            long elementCount = OffHeapLongArray.get(this.rootPtr, INDEX_SIZE);
            for (long i = 0; i < elementCount; i++) {
                long loopValue = OffHeapLongArray.get(elementV_ptr, i);
                if (loopValue != CoreConstants.NULL_LONG) {
                    if (!isFirst) {
                        buffer.write(CoreConstants.CHUNK_SUB_SEP);
                    }
                    isFirst = false;
                    Base64.encodeLongToBuffer(OffHeapLongArray.get(elementK_ptr, i), buffer);
                    buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
                    Base64.encodeLongToBuffer(loopValue, buffer);
                }
            }
        } finally {
            //UNLOCK
            if (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
        }
    }

    private void load(Buffer buffer) {
        int cursor = 0;
        long loopKey = CoreConstants.NULL_LONG;
        int previousStart = -1;
        long capacity = -1;
        int insertIndex = 0;

        boolean initDone = false;

        while (cursor < buffer.size()) {

            if (buffer.read(cursor) == CoreConstants.CHUNK_SEP) {

                if (!initDone) {
                    long size = Base64.decodeToLongWithBounds(buffer, 0, cursor);
                    if (size == 0) {
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
                    OffHeapLongArray.set(this.rootPtr, INDEX_THRESHOLD, (long) (capacity * CoreConstants.MAP_LOAD_FACTOR));
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
                    initDone = true;
                } else {
                    long extra = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                    OffHeapLongArray.set(rootPtr, INDEX_EXTRA, extra);
                }

                //reset for next round
                previousStart = cursor + 1;

            } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SEP) {
                if (loopKey != CoreConstants.NULL_LONG) {
                    long loopValue = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
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
                    loopKey = CoreConstants.NULL_LONG;
                }
                previousStart = cursor + 1;
            } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SUB_SEP) {
                loopKey = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                previousStart = cursor + 1;
            }
            //loop in all case
            cursor++;
        }
        if (loopKey != CoreConstants.NULL_LONG) {
            long loopValue = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
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

        long previousMagic;
        long nextMagic;
        do {
            previousMagic = OffHeapLongArray.get(rootPtr, INDEX_MAGIC);
            nextMagic = previousMagic + 1;
        } while (!OffHeapLongArray.compareAndSwap(rootPtr, INDEX_MAGIC, previousMagic, nextMagic));

        if (listener != null) {
            if ((OffHeapLongArray.get(rootPtr, INDEX_FLAGS) & CoreConstants.DIRTY_BIT) != CoreConstants.DIRTY_BIT) {
                listener.declareDirty(this);
            }
        }
    }

}
