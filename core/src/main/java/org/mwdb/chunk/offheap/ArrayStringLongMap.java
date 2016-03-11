package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStringLongMap;
import org.mwdb.chunk.KStringLongMapCallBack;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 */
public class ArrayStringLongMap implements KStringLongMap {
    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    private final KChunkListener listener;
    private final long root_array_ptr;
    //LongArrays
    private static final int INDEX_ELEMENT_V = 0;
    private static final int INDEX_ELEMENT_K_H = 1;
    private static final int INDEX_ELEMENT_NEXT = 2;
    private static final int INDEX_ELEMENT_HASH = 3;
    //StringArrays
    private static final int INDEX_ELEMENT_K = 4;
    //Long values
    private static final int INDEX_ELEMENT_LOCK = 5;
    private static final int INDEX_THRESHOLD = 6;
    private static final int INDEX_ELEMENT_COUNT = 7;
    private static final int INDEX_CAPACITY = 8;

    //long[]
    private long elementK_H_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;
    //string[]
    private long elementK_ptr;

    public ArrayStringLongMap(KChunkListener listener, long initialCapacity, long previousAddr) {
        this.listener = listener;
        if (previousAddr == Constants.OFFHEAP_NULL_PTR) {
            this.root_array_ptr = OffHeapLongArray.allocate(9);
            /** Init long variables */
            //init lock
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_LOCK, 0);
            //init capacity
            OffHeapLongArray.set(this.root_array_ptr, INDEX_CAPACITY, initialCapacity);
            //init threshold
            OffHeapLongArray.set(this.root_array_ptr, INDEX_THRESHOLD, (long) (initialCapacity * Constants.MAP_LOAD_FACTOR));
            //init elementCount
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_COUNT, 0);

            /** Init Long[] variables */
            //init elementV
            elementV_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);
            //init elementK_H
            elementK_H_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_K_H, elementK_H_ptr);
            //init elementNext
            elementNext_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);
            //init elementHash
            elementHash_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
            /** Init String[] variables */
            //init elementK
            elementK_ptr = OffHeapStringArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
        } else {
            this.root_array_ptr = previousAddr;
            elementK_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K);
            elementHash_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_NEXT);
            elementK_H_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K_H);
            elementV_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V);
        }

    }

    private final void consistencyCheck() {
        if (OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V) != elementV_ptr) {
            elementK_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K);
            elementHash_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_NEXT);
            elementK_H_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K_H);
            elementV_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V);
        }
    }

    @Override
    public final long getValue(String key) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long hashedKey = PrimitiveHelper.stringHash(key);
        long hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        long result = Constants.NULL_LONG;
        while (m != -1) {
            //optimization to avoid string comparison for all collisions
            if (OffHeapLongArray.get(elementK_H_ptr, m) == hashedKey) {
                if (PrimitiveHelper.equals(key, OffHeapStringArray.get(elementK_ptr, m))) {
                    result = OffHeapLongArray.get(elementV_ptr, m);
                    break;
                }
            }
            m = OffHeapLongArray.get(elementNext_ptr, m);
        }

        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }

        return result;
    }

    @Override
    public String getKey(long index) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long capacity = OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY);
        String resultKey = null;
        if (index < capacity) {
            resultKey = OffHeapStringArray.get(elementK_ptr, index);
        }
        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
        return resultKey;
    }

    @Override
    public void each(KStringLongMapCallBack callback) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long elementCount = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_COUNT);
        for (long i = 0; i < elementCount; i++) {
            String loopKey = OffHeapStringArray.get(elementK_ptr, i);
            if (loopKey != null) {
                callback.on(loopKey, OffHeapLongArray.get(elementV_ptr, i));
            }
        }

        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
    }

    @Override
    public long size() {
        return OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_COUNT);
    }

    @Override
    public void remove(String key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    public static void free(long addr) {
        long capacity = OffHeapLongArray.get(addr, INDEX_CAPACITY);
        //free String[]
        OffHeapStringArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_K), capacity);
        //free all long[]
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_V));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_NEXT));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_HASH));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_K_H));
        //free master array
        OffHeapLongArray.free(addr);
    }

    @Override
    public final void put(String key, long value) {
        //compute the hash of the key
        long hashedKey = PrimitiveHelper.stringHash(key);

        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long entry = -1;
        long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        long count = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
        long hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), capacity);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            //optimization to avoid string comparison for all collisions
            if (OffHeapLongArray.get(elementK_H_ptr, m) == hashedKey) {
                if (PrimitiveHelper.equals(key, OffHeapStringArray.get(elementK_ptr, m))) {
                    entry = m;
                    break;
                }
            }
            m = OffHeapLongArray.get(elementNext_ptr, m);
        }
        if (entry == -1) {
            //if need to reHash (too small or too much collisions)
            if ((count + 1) > OffHeapLongArray.get(root_array_ptr, INDEX_THRESHOLD)) {

                long newCapacity = capacity << 1;
                //reallocate the string[], indexes are not changed
                elementK_ptr = OffHeapStringArray.reallocate(elementK_ptr, capacity, newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
                //reallocate the long[] values
                elementV_ptr = OffHeapLongArray.reallocate(elementV_ptr, capacity, newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);
                elementK_H_ptr = OffHeapLongArray.reallocate(elementK_H_ptr, capacity, newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K_H, elementK_H_ptr);

                //Create two new Hash and Next structures
                OffHeapLongArray.free(elementHash_ptr);
                OffHeapLongArray.free(elementNext_ptr);
                elementHash_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
                elementNext_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);

                //rehashEveryThing
                for (long i = 0; i < count; i++) {
                    long previousHash = OffHeapLongArray.get(elementK_H_ptr, i);
                    if (previousHash != Constants.OFFHEAP_NULL_PTR) {
                        long newHashIndex = PrimitiveHelper.longHash(OffHeapLongArray.get(elementK_H_ptr, i), newCapacity);
                        long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, newHashIndex);
                        if (currentHashedIndex != Constants.OFFHEAP_NULL_PTR) {
                            OffHeapLongArray.set(elementNext_ptr, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(elementHash_ptr, newHashIndex, i);
                    }
                }

                capacity = newCapacity;
                OffHeapLongArray.set(root_array_ptr, INDEX_CAPACITY, capacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_THRESHOLD, (long) (newCapacity * Constants.MAP_LOAD_FACTOR));
                hashIndex = PrimitiveHelper.longHash(hashedKey, capacity);
            }
            //set K and associated K_H
            OffHeapStringArray.set(elementK_ptr, count, key);
            OffHeapLongArray.set(elementK_H_ptr, count, hashedKey);
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
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, count + 1);
            //inform the listener
            this.listener.declareDirty(null);
        } else {
            if (OffHeapLongArray.get(elementV_ptr, entry) != value && value != Constants.NULL_LONG) {
                //setValue
                OffHeapLongArray.set(elementV_ptr, entry, value);
                this.listener.declareDirty(null);
            }
        }
        if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
    }

    public long rootAddress() {
        return root_array_ptr;
    }

    public static long cloneMap(long srcAddr) {
        // capacity
        long capacity = OffHeapLongArray.get(srcAddr, INDEX_CAPACITY);

        long newSrcAddr = OffHeapLongArray.allocate(capacity);
        unsafe.copyMemory(srcAddr, newSrcAddr, capacity * 8);

        long elementVAddr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_V);
        long newElementVAddr = OffHeapLongArray.allocate(capacity);
        unsafe.copyMemory(elementVAddr, newElementVAddr, capacity * 8);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, newElementVAddr);

        long elementKHashAddr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_K_H);
        long newElementKHashAddr = OffHeapLongArray.allocate(capacity);
        unsafe.copyMemory(elementKHashAddr, newElementKHashAddr, capacity * 8);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K_H, newElementKHashAddr);

        long elementKAddr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_K);
        long newElementKAddr = OffHeapStringArray.allocate(capacity);
        // TODO copy the content
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K, newElementKAddr);

        long elementNextAddr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_NEXT);
        long newElementNextAddr = OffHeapLongArray.allocate(capacity);
        unsafe.copyMemory(elementNextAddr, newElementNextAddr, capacity * 8);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_NEXT, newElementNextAddr);

        long elementHashAddr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_HASH);
        long newElementHashAddr = OffHeapLongArray.allocate(capacity);
        unsafe.copyMemory(elementHashAddr, newElementHashAddr, capacity * 8);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_HASH, newElementHashAddr);

        return newSrcAddr;
    }
}
