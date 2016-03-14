package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KLongLongMap;
import org.mwdb.chunk.KLongLongMapCallBack;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 */
public class ArrayLongLongMap implements KLongLongMap {
    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    private final KChunkListener listener;
    private final long root_array_ptr;
    //LongArrays
    private static final int INDEX_ELEMENT_V = 0;
    private static final int INDEX_ELEMENT_NEXT = 1;
    private static final int INDEX_ELEMENT_HASH = 2;
    private static final int INDEX_ELEMENT_K = 3;
    //Long values
    private static final int INDEX_ELEMENT_LOCK = 4;
    private static final int INDEX_THRESHOLD = 5;
    private static final int INDEX_ELEMENT_COUNT = 6;
    private static final int INDEX_CAPACITY = 7;

    //long[]
    private long elementK_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;

    public ArrayLongLongMap(KChunkListener listener, long initialCapacity, long previousAddr) {
        this.listener = listener;
        if (previousAddr == Constants.OFFHEAP_NULL_PTR) {
            this.root_array_ptr = OffHeapLongArray.allocate(8);
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
            //init elementK
            elementK_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
            //init elementV
            elementV_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);
            //init elementNext
            elementNext_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);
            //init elementHash
            elementHash_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
        } else {
            this.root_array_ptr = previousAddr;
            elementK_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V);
            elementHash_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_NEXT);
        }

    }

    private final void consistencyCheck() {
        if (OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V) != elementV_ptr) {
            elementK_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V);
            elementHash_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_NEXT);
        }
    }

    @Override
    public final long get(long key) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long hashIndex = PrimitiveHelper.longHash(key, OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
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
        if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }

        return result;
    }

    @Override
    public void each(KLongLongMapCallBack callback) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long elementCount = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_COUNT);
        for (long i = 0; i < elementCount; i++) {
            long loopValue = OffHeapLongArray.get(elementV_ptr, i);
            if (loopValue != Constants.NULL_LONG) {
                callback.on(OffHeapLongArray.get(elementK_ptr, i), loopValue);
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
    public void remove(long key) {
        throw new RuntimeException("Not implemented yet!!!");
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
    public final void put(long key, long value) {
        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long entry = -1;
        long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        long count = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
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
            if ((count + 1) > OffHeapLongArray.get(root_array_ptr, INDEX_THRESHOLD)) {

                long newCapacity = capacity << 1;
                //reallocate the string[], indexes are not changed
                elementK_ptr = OffHeapStringArray.reallocate(elementK_ptr, capacity, newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
                //reallocate the long[] values
                elementV_ptr = OffHeapLongArray.reallocate(elementV_ptr, capacity, newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);

                //Create two new Hash and Next structures
                OffHeapLongArray.free(elementHash_ptr);
                OffHeapLongArray.free(elementNext_ptr);
                elementHash_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
                elementNext_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);

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
                OffHeapLongArray.set(root_array_ptr, INDEX_CAPACITY, capacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_THRESHOLD, (long) (newCapacity * Constants.MAP_LOAD_FACTOR));
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

        // copy root array
        long newSrcAddr = OffHeapLongArray.cloneArray(srcAddr, capacity);
        // copy elementK array
        long elementK_ptr = OffHeapLongArray.get(newSrcAddr, INDEX_ELEMENT_K);
        long newElementK_ptr = OffHeapLongArray.cloneArray(elementK_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, newElementK_ptr);
        // copy elementV array
        long elementV_ptr = OffHeapLongArray.get(newSrcAddr, INDEX_ELEMENT_V);
        long newElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, newElementV_ptr);
        // copy elementNext array
        long elementNext_ptr = OffHeapLongArray.get(newSrcAddr, INDEX_ELEMENT_NEXT);
        long newElementNext_ptr = OffHeapLongArray.cloneArray(elementNext_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
        // copy elementHash array
        long elementHash_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_HASH);
        long newElementHash_ptr = OffHeapLongArray.cloneArray(elementHash_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_HASH, newElementHash_ptr);

        return newSrcAddr;
    }

}
