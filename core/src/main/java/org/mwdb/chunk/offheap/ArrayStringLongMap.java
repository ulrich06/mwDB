package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStringLongMap;
import org.mwdb.chunk.KStringLongMapCallBack;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 * Memory layout: all structures are memory blocks of either primitive values (as longs)
 * or pointers to memory blocks
 */
public class ArrayStringLongMap implements KStringLongMap, KOffHeapStateChunkElem {
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

    private long elementK_ptr;
    private long elementK_H_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;

    public ArrayStringLongMap(KChunkListener listener, long initialCapacity, long previousAddr) {
        this.listener = listener;
        if (previousAddr == -1) {
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
            //init elementV
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

    @Override
    public final long getValue(String key) {
        //TODO cas protection
        long hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != -1) {
            //TODO accelerate with the stored hash
            if (PrimitiveHelper.equals(key, OffHeapStringArray.get(elementK_ptr, m))) {
                return OffHeapLongArray.get(elementV_ptr, m);
            } else {
                m = OffHeapLongArray.get(elementNext_ptr, m);
            }
        }
        return Constants.NULL_LONG;
    }

    @Override
    public String getKey(long index) {
        long capacity = OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY);
        if (index < capacity) {
            //TODO CAS protection
            return OffHeapStringArray.get(elementK_ptr, index);
        }
        return null;
    }

    @Override
    public void each(KStringLongMapCallBack callback) {
        long elementCount = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_COUNT);
        for (int i = 0; i < elementCount; i++) {
            String loopKey = OffHeapStringArray.get(elementK_ptr, i);
            if (loopKey != null) {
                callback.on(loopKey, OffHeapLongArray.get(elementV_ptr, i));
            }
        }
    }

    @Override
    public int size() {
        return (int) OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_COUNT);
    }

    @Override
    public void remove(String key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    @Override
    public void free() {
        long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        OffHeapStringArray.free(elementK_ptr, capacity);

        OffHeapLongArray.free(elementV_ptr);
        OffHeapLongArray.free(elementNext_ptr);
        OffHeapLongArray.free(elementHash_ptr);
        OffHeapLongArray.free(elementK_H_ptr);

        OffHeapLongArray.free(root_array_ptr);
    }

    @Override
    public final void put(String key, long value) {
        /*

        //cas to put a lock flag
        while (OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;

        long entry = -1;
        long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        long hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), capacity);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            //TODO optimize with the second stored hash
            if (PrimitiveHelper.equals(key, getElementKValue(m))) {
                entry = m;
                break;
            }
            m = OffHeapLongArray.get(elementNext_ptr, hashIndex);
        }


        if (entry == -1) {
            //if need to reHash (too small or too much collisions)
            if ((getElementCount() + 1) > getThreshold()) {

                long reHash = System.currentTimeMillis();

                long newCapacity = size << 1;

                long elementK_ptr_tmp = getElementKPtr();
                long elementK_ptr = unsafe.allocateMemory(newCapacity * 8);
                unsafe.setMemory(elementK_ptr, newCapacity * 8, (byte) -1);
                unsafe.copyMemory(elementK_ptr_tmp, elementK_ptr, size * 8);
                setElementKPtr(elementK_ptr);
                unsafe.freeMemory(elementK_ptr_tmp);

                long elementK_H_ptr_tmp = getElementKHPtr();
                long elementK_H_ptr = unsafe.allocateMemory(newCapacity * 8);
                unsafe.setMemory(elementK_H_ptr, newCapacity * 8, (byte) -1);
                unsafe.copyMemory(elementK_H_ptr_tmp, elementK_H_ptr, size * 8);
                setElementKHashPtr(elementK_ptr);
                unsafe.freeMemory(elementK_H_ptr_tmp);

                long elementV_ptr_tmp = getElementVPtr();
                long elementV_ptr = unsafe.allocateMemory(newCapacity * 8);
                unsafe.setMemory(elementV_ptr, newCapacity * 8, (byte) -1);
                unsafe.copyMemory(elementV_ptr_tmp, elementV_ptr, size * 8);
                setElementVPtr(elementV_ptr);
                unsafe.freeMemory(elementV_ptr_tmp);

                long elementNext_ptr_tmp = getElementNextPtr();
                long elementNext_ptr = unsafe.allocateMemory(newCapacity * 8);
                unsafe.setMemory(elementNext_ptr, newCapacity * 8, (byte) -1);
                setElementNextPtr(elementNext_ptr);
                unsafe.freeMemory(elementNext_ptr_tmp);

                long elementHash_ptr_tmp = getElementHashPtr();
                long elementHash_ptr = unsafe.allocateMemory(newCapacity * 8);
                unsafe.setMemory(elementHash_ptr, newCapacity * 8, (byte) -1);
                setElementHashPtr(elementHash_ptr);
                unsafe.freeMemory(elementHash_ptr_tmp);

                long afterCopy = System.currentTimeMillis();

                //rehashEveryThing
                for (int i = 0; i < getElementCount(); i++) {
                    if (getElementKValue(i) != null) { //there is a real value
                        long newHashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(getElementKValue(i)), newCapacity);
                        long currentHashedIndex = getElementHashValue(newHashIndex);
                        if (currentHashedIndex != -1) {
                            setElementNextValue(i, currentHashedIndex);
                        } else {
                            setElementNextValue(i, -2);
                        }
                        setElementHashValue(newHashIndex, i);
                    }
                }

                long afterRehash = System.currentTimeMillis();

                System.out.println((afterCopy - reHash) / 1000);
                System.out.println((afterRehash - afterCopy) / 1000);

                //setPrimitiveType value for all
                setSize(newCapacity);
                setThreshold((long) (newCapacity * Constants.MAP_LOAD_FACTOR));
                hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), getSize());
            }
            long newIndex = getElementCount();
            setElementKValue(newIndex, key);
            if (value == Constants.NULL_LONG) {
                setElementVValue(newIndex, getElementCount());
            } else {
                setElementVValue(newIndex, value);
            }

            long currentHashedElemIndex = getElementHashValue(hashIndex);
            if (currentHashedElemIndex != -1) {
                setElementNextValue(newIndex, currentHashedElemIndex);
            } else {
                setElementNextValue(newIndex, -2);
            }
            //now the object is reachable to other thread everything should be ready
            setElementHashValue(hashIndex, newIndex);
            setElementCount(getElementCount() + 1);

            this.listener.declareDirty(null);
        } else {
            if (getElementVValue(entry) != value && value != Constants.NULL_LONG) {
                //setValue
                setElementVValue(entry, value);
                this.listener.declareDirty(null);
            }
        }

        unlock();
        */
    }

}
