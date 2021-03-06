package org.mwg.core.chunk.offheap;

import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.ChunkListener;
import org.mwg.core.utility.DataHasher;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;
import org.mwg.struct.StringLongMap;
import org.mwg.struct.StringLongMapCallBack;

/**
 * @ignore ts
 */
public class ArrayStringLongMap implements StringLongMap {
    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    private final ChunkListener listener;
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

    private static final int ROOT_ARRAY_SIZE = 9;

    //long[]
    private long elementK_H_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;
    //string[]
    private long elementK_ptr;

    public ArrayStringLongMap(ChunkListener listener, long initialCapacity, long previousAddr) {
        this.listener = listener;
        if (previousAddr == CoreConstants.OFFHEAP_NULL_PTR) {
            this.root_array_ptr = OffHeapLongArray.allocate(ROOT_ARRAY_SIZE);
            /** Init long variables */
            //init lock
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_LOCK, 0);
            //init capacity
            OffHeapLongArray.set(this.root_array_ptr, INDEX_CAPACITY, initialCapacity);
            //init threshold
            OffHeapLongArray.set(this.root_array_ptr, INDEX_THRESHOLD, (long) (initialCapacity * CoreConstants.MAP_LOAD_FACTOR));
            //init elementCount
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_COUNT, 0);

            /** Init Long[] variables */
            //init elementV
            elementV_ptr = OffHeapLongArray.allocate(initialCapacity + 1); // cow counter + capacity
            OffHeapLongArray.set(elementV_ptr, 0, 0); //init cow counter
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

        final long hashedKey = DataHasher.hash(key);
        final long hashIndex = PrimitiveHelper.longHash(hashedKey, OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        long result = CoreConstants.NULL_LONG;
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            //optimization to avoid string comparison for all collisions
            if (OffHeapLongArray.get(elementK_H_ptr, m) == hashedKey) {
                //if (PrimitiveHelper.equals(key, OffHeapStringArray.get(elementK_ptr, m))) {
                result = OffHeapLongArray.get(elementV_ptr + 8, m);
                break;
                //}
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
    public String getByHash(long hashedKey) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        final long hashIndex = PrimitiveHelper.longHash(hashedKey, OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        String result = null;
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            //optimization to avoid string comparison for all collisions
            if (OffHeapLongArray.get(elementK_H_ptr, m) == hashedKey) {
                result = OffHeapStringArray.get(elementK_ptr, m);
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
    public boolean containsHash(long hashedKey) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        final long hashIndex = PrimitiveHelper.longHash(hashedKey, OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        boolean result = false;
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            //optimization to avoid string comparison for all collisions
            if (OffHeapLongArray.get(elementK_H_ptr, m) == hashedKey) {
                result = true;
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
    public void each(StringLongMapCallBack callback) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long elementCount = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_COUNT);
        for (long i = 0; i < elementCount; i++) {
            String loopKey = OffHeapStringArray.get(elementK_ptr, i);
            if (loopKey != null) {
                callback.on(loopKey, OffHeapLongArray.get(elementV_ptr + 8, i));
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
        long thisCowCounter = decrementCopyOnWriteCounter(addr);
        if (thisCowCounter == 0) {
            long capacity = OffHeapLongArray.get(addr, INDEX_CAPACITY);
            //free String[]
            OffHeapStringArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_K), capacity);
            //free all long[]
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_V));
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_NEXT));
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_HASH));
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_K_H));
        }
        //free master array -> it is just a proxy
        OffHeapLongArray.free(addr);
    }

    @Override
    public final void put(String key, long value) {

        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long thisCowCounter = decrementCopyOnWriteCounter(root_array_ptr);
        if (thisCowCounter > 0) {
            /** all fields must be copied: real deep clone */
            long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);

            // the root array itself is already copied
            // copy elementK array
            long newElementK_ptr = OffHeapStringArray.cloneArray(elementK_ptr, capacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, newElementK_ptr);
            // copy elementV array
            long newElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, capacity + 1);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, newElementV_ptr);
            // copy elementKHash array
            long newElementKHash_ptr = OffHeapLongArray.cloneArray(elementK_H_ptr, capacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K_H, newElementKHash_ptr);
            // copy elementNext array
            long newElementNext_ptr = OffHeapLongArray.cloneArray(elementNext_ptr, capacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
            // copy elementHash array
            long newElementHash_ptr = OffHeapLongArray.cloneArray(elementHash_ptr, capacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, newElementHash_ptr);

            elementK_ptr = newElementK_ptr;
            elementV_ptr = newElementV_ptr;
            elementHash_ptr = newElementHash_ptr;
            elementNext_ptr = newElementNext_ptr;

            // cow counter
            OffHeapLongArray.set(newElementV_ptr, 0, 1);
        } else {
            incrementCopyOnWriteCounter(root_array_ptr);
        }

        //compute the hash of the key
        long hashedKey = DataHasher.hash(key);

        long entry = -1;
        long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        long count = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);
        long hashIndex = PrimitiveHelper.longHash(hashedKey, capacity);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
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
                elementV_ptr = OffHeapLongArray.reallocate(elementV_ptr, capacity + 1, newCapacity + 1);
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
                    if (previousHash != CoreConstants.OFFHEAP_NULL_PTR) {
                        long newHashIndex = PrimitiveHelper.longHash(OffHeapLongArray.get(elementK_H_ptr, i), newCapacity);
                        long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, newHashIndex);
                        if (currentHashedIndex != CoreConstants.OFFHEAP_NULL_PTR) {
                            OffHeapLongArray.set(elementNext_ptr, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(elementHash_ptr, newHashIndex, i);
                    }
                }

                capacity = newCapacity;
                OffHeapLongArray.set(root_array_ptr, INDEX_CAPACITY, capacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_THRESHOLD, (long) (newCapacity * CoreConstants.MAP_LOAD_FACTOR));
                hashIndex = PrimitiveHelper.longHash(hashedKey, capacity);
            }
            //set K, associated K_H and V
            OffHeapStringArray.set(elementK_ptr, count, key);
            OffHeapLongArray.set(elementK_H_ptr, count, hashedKey);
            OffHeapLongArray.set(elementV_ptr + 8, count, value);

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
            if (OffHeapLongArray.get(elementV_ptr + 8, entry) != value && value != CoreConstants.NULL_LONG) {
                //setValue
                OffHeapLongArray.set(elementV_ptr + 8, entry, value);
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

    public static long incrementCopyOnWriteCounter(long addr) {
        long elemV_ptr = OffHeapLongArray.get(addr, INDEX_ELEMENT_V);
        return unsafe.getAndAddLong(null, elemV_ptr, 1) + 1;
    }

    public static long decrementCopyOnWriteCounter(long addr) {
        long elemV_ptr = OffHeapLongArray.get(addr, INDEX_ELEMENT_V);
        return unsafe.getAndAddLong(null, elemV_ptr, -1) - 1;
    }


    public static long softClone(long srcAddr) {
        // copy root array
        long newSrcAddr = OffHeapLongArray.cloneArray(srcAddr, ROOT_ARRAY_SIZE);
        // link elementK array
        long elementK_ptr = OffHeapLongArray.get(newSrcAddr, INDEX_ELEMENT_K); // OffHeapStringArray
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K, elementK_ptr);
        // link elementV array
        long elementV_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_V);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, elementV_ptr);
        // link elementKHash array
        long elementKHash_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_K_H);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K_H, elementKHash_ptr);
        // link elementNext array
        long elementNext_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_NEXT);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_NEXT, elementNext_ptr);
        // copy elementHash array
        long elementHashA_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_HASH);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_HASH, elementHashA_ptr);

        return newSrcAddr;
    }

    /*
    public static long cloneMap(long srcAddr) {
        // capacity
        long capacity = OffHeapLongArray.get(srcAddr, INDEX_CAPACITY);

        // copy root array
        long newSrcAddr = OffHeapLongArray.cloneArray(srcAddr, 10);
        // copy elementK array
        long elementK_ptr = OffHeapLongArray.get(newSrcAddr, INDEX_ELEMENT_K); // OffHeapStringArray
        long newElementK_ptr = OffHeapStringArray.cloneArray(elementK_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K, newElementK_ptr);
        // copy elementV array
        long elementV_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_V);
        long newElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, newElementV_ptr);
        // copy elementKHash array
        long elementKHash_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_K_H);
        long newElementKHash_ptr = OffHeapLongArray.cloneArray(elementKHash_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K_H, newElementKHash_ptr);
        // copy elementNext array
        long elementNext_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_NEXT);
        long newElementNext_ptr = OffHeapLongArray.cloneArray(elementNext_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
        // copy elementHash array
        long elementHashA_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_HASH);
        long newElementHash_ptr = OffHeapLongArray.cloneArray(elementHashA_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_HASH, newElementHash_ptr);

        return newSrcAddr;
    }
    */

}
