package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KLongLongArrayMap;
import org.mwdb.chunk.KLongLongArrayMapCallBack;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 */
public class ArrayLongLongArrayMap implements KLongLongArrayMap {
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
    private static final int INDEX_NEXT_EMPTY = 8;

    private static final int ROOT_ARRAY_SIZE = 9;

    //long[]
    private long elementK_ptr;
    private long elementV_ptr;
    private long elementNext_ptr;
    private long elementHash_ptr;

    public ArrayLongLongArrayMap(KChunkListener listener, long initialCapacity, long previousAddr) {
        this.listener = listener;
        if (previousAddr == Constants.OFFHEAP_NULL_PTR) {
            this.root_array_ptr = OffHeapLongArray.allocate(ROOT_ARRAY_SIZE);
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
            elementV_ptr = OffHeapLongArray.allocate(1 + initialCapacity); //cow counter + capacity
            OffHeapLongArray.set(elementV_ptr, 0, 0); //init cow counter
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);
            //init elementNext
            elementNext_ptr = OffHeapLongArray.allocate(initialCapacity);
            OffHeapLongArray.set(this.root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);

            //init next empty queue
            OffHeapLongArray.set(this.root_array_ptr, INDEX_NEXT_EMPTY, 0);
            for (long i = 0; i < initialCapacity; i++) {
                if (i == initialCapacity - 1) {
                    OffHeapLongArray.set(elementNext_ptr, i, Constants.OFFHEAP_NULL_PTR);
                } else {
                    OffHeapLongArray.set(elementNext_ptr, i, i + 1);
                }
                OffHeapLongArray.set(elementV_ptr + 8, i, Constants.NULL_LONG);
            }

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

    private void consistencyCheck() {
        if (OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V) != elementV_ptr) {
            elementK_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_K);
            elementV_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_V);
            elementHash_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_HASH);
            elementNext_ptr = OffHeapLongArray.get(this.root_array_ptr, INDEX_ELEMENT_NEXT);
        }
    }

    @Override
    public final long[] get(long key) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long hashIndex = PrimitiveHelper.longHash(key, OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY));
        long[] result = null;
        int capacity = 0;
        int resultIndex = 0;
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != -1) {
            if (key == OffHeapLongArray.get(elementK_ptr, m) && OffHeapLongArray.get(elementV_ptr + 8, m) != Constants.NULL_LONG) {
                if (resultIndex == capacity) {
                    if (capacity == 0) {
                        result = new long[1];
                        capacity = 1;
                    } else {
                        long[] temp_result = new long[capacity * 2];
                        System.arraycopy(result, 0, temp_result, 0, capacity);
                        capacity = capacity * 2;
                        result = temp_result;
                    }
                    result[resultIndex] = OffHeapLongArray.get(elementV_ptr + 8, m);
                    resultIndex++;
                } else {
                    result[resultIndex] = OffHeapLongArray.get(elementV_ptr + 8, m);
                    resultIndex++;
                }
            }
            m = OffHeapLongArray.get(elementNext_ptr, m);
        }
        //UNLOCK
        if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
            throw new RuntimeException("CAS error !!!");
        }
        if (resultIndex == 0) {
            return new long[0];
        } else {
            if (resultIndex == capacity) {
                return result;
            } else {
                //shrink result
                long[] shrinkedResult = new long[resultIndex];
                System.arraycopy(result, 0, shrinkedResult, 0, resultIndex);
                return shrinkedResult;
            }
        }
    }

    @Override
    public void each(KLongLongArrayMapCallBack callback) {
        //LOCK
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long capacity = OffHeapLongArray.get(this.root_array_ptr, INDEX_CAPACITY);
        for (long i = 0; i < capacity; i++) {
            long loopValue = OffHeapLongArray.get(elementV_ptr + 8, i);
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
    public void remove(long key, long value) {
        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();
        try {
            long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
            long hashIndex = PrimitiveHelper.longHash(key, capacity);
            long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            long previousM = Constants.OFFHEAP_NULL_PTR;
            while (m != Constants.OFFHEAP_NULL_PTR) {
                if (key == OffHeapLongArray.get(elementK_ptr, m) && value == OffHeapLongArray.get(elementV_ptr + 8, m)) {
                    OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT) - 1);
                    OffHeapLongArray.set(elementK_ptr, m, Constants.NULL_LONG);
                    OffHeapLongArray.set(elementV_ptr + 8, m, Constants.NULL_LONG);
                    if (previousM == -1) {
                        //we are in the top of hashFunction
                        OffHeapLongArray.set(elementHash_ptr, hashIndex, OffHeapLongArray.get(elementNext_ptr, m));
                    } else {
                        OffHeapLongArray.set(elementNext_ptr, previousM, OffHeapLongArray.get(elementNext_ptr, m));
                    }
                    //we enqueue m has in the available queue
                    OffHeapLongArray.set(elementNext_ptr, m, OffHeapLongArray.get(root_array_ptr, INDEX_NEXT_EMPTY));
                    OffHeapLongArray.set(root_array_ptr, INDEX_NEXT_EMPTY, m);
                    break;
                }
                previousM = m;
                m = OffHeapLongArray.get(elementNext_ptr, m);
            }

        } finally {
            if (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 1, 0)) {
                throw new RuntimeException("CAS error !!!");
            }
        }
    }

    public static void free(long addr) {
        long thisCowCounter = decrementCopyOnWriteCounter(addr);
        if (thisCowCounter == 0) {
            //free all long[]
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_K));
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_V));
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_NEXT));
            OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_ELEMENT_HASH));
        }
        //free master array -> it is just a proxy
        OffHeapLongArray.free(addr);
    }

    @Override
    public final void put(long key, long value) {

        //cas to put a lock flag
        while (!OffHeapLongArray.compareAndSwap(root_array_ptr, INDEX_ELEMENT_LOCK, 0, 1)) ;
        consistencyCheck();

        long thisCowCounter = decrementCopyOnWriteCounter(root_array_ptr);
        if (thisCowCounter > 0) {
            /** all fields must be copied: real deep clone */
            // the root array itself is already copied
            long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
            // copy elementK array
            long newElementK_ptr = OffHeapLongArray.cloneArray(elementK_ptr, capacity);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, newElementK_ptr);
            // copy elementV array
            long newElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, capacity + 1);
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, newElementV_ptr);
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

        long entry = -1;
        long capacity = OffHeapLongArray.get(root_array_ptr, INDEX_CAPACITY);
        long elementCount = OffHeapLongArray.get(root_array_ptr, INDEX_ELEMENT_COUNT);

        long hashIndex = PrimitiveHelper.longHash(key, capacity);
        long m = OffHeapLongArray.get(elementHash_ptr, hashIndex);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            if (key == OffHeapLongArray.get(elementK_ptr, m) && value == OffHeapLongArray.get(elementV_ptr + 8, m)) {
                entry = m;
                break;
            }
            m = OffHeapLongArray.get(elementNext_ptr, m);
        }
        if (entry == -1) {
            //if need to reHash (too small or too much collisions)
            if ((elementCount + 1) > OffHeapLongArray.get(root_array_ptr, INDEX_THRESHOLD)) {

                long newCapacity = capacity << 1;
                //reallocate the string[], indexes are not changed
                elementK_ptr = OffHeapStringArray.reallocate(elementK_ptr, capacity, newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_K, elementK_ptr);
                //reallocate the long[] values
                elementV_ptr = OffHeapLongArray.reallocate(elementV_ptr, capacity + 1, newCapacity + 1);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_V, elementV_ptr);

                //Create two new Hash and Next structures
                OffHeapLongArray.free(elementHash_ptr);
                OffHeapLongArray.free(elementNext_ptr);
                elementHash_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_HASH, elementHash_ptr);
                elementNext_ptr = OffHeapLongArray.allocate(newCapacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_NEXT, elementNext_ptr);

                //reset new values
                for (long i = capacity; i < newCapacity; i++) {
                    OffHeapLongArray.set(elementV_ptr + 8, i, Constants.NULL_LONG);
                }

                //rehashEveryThing
                long previousEmptySlot = -1;
                long emptySlotHEad = -1;
                for (long i = 0; i < newCapacity; i++) {
                    long previousValue = OffHeapLongArray.get(elementV_ptr + 8, i);
                    if (previousValue != Constants.NULL_LONG) {
                        long previousKey = OffHeapLongArray.get(elementK_ptr, i);
                        long newHashIndex = PrimitiveHelper.longHash(previousKey, newCapacity);
                        long currentHashedIndex = OffHeapLongArray.get(elementHash_ptr, newHashIndex);
                        if (currentHashedIndex != Constants.OFFHEAP_NULL_PTR) {
                            OffHeapLongArray.set(elementNext_ptr, i, currentHashedIndex);
                        }
                        OffHeapLongArray.set(elementHash_ptr, newHashIndex, i);
                    } else {
                        //enqueue empty val
                        if (previousEmptySlot == -1) {
                            emptySlotHEad = i;
                        } else {
                            OffHeapLongArray.set(elementNext_ptr, previousEmptySlot, i);
                        }
                        previousEmptySlot = i;
                    }
                }

                capacity = newCapacity;
                OffHeapLongArray.set(root_array_ptr, INDEX_NEXT_EMPTY, emptySlotHEad);
                OffHeapLongArray.set(root_array_ptr, INDEX_CAPACITY, capacity);
                OffHeapLongArray.set(root_array_ptr, INDEX_THRESHOLD, (long) (newCapacity * Constants.MAP_LOAD_FACTOR));
                hashIndex = PrimitiveHelper.longHash(key, capacity);
            }

            long nextIndex = OffHeapLongArray.get(root_array_ptr, INDEX_NEXT_EMPTY);
            if (nextIndex == Constants.OFFHEAP_NULL_PTR) {
                throw new RuntimeException("Implement Error, map should never be null");
            }
            OffHeapLongArray.set(root_array_ptr, INDEX_NEXT_EMPTY, OffHeapLongArray.get(elementNext_ptr, nextIndex));
            OffHeapLongArray.set(elementNext_ptr, nextIndex, Constants.OFFHEAP_NULL_PTR);

            //set K
            OffHeapLongArray.set(elementK_ptr, nextIndex, key);
            //set value or index if null
            if (value == Constants.NULL_LONG) {
                OffHeapLongArray.set(elementV_ptr + 8, nextIndex, elementCount);
            } else {
                OffHeapLongArray.set(elementV_ptr + 8, nextIndex, value);
            }
            long currentHashedElemIndex = OffHeapLongArray.get(elementHash_ptr, hashIndex);
            if (currentHashedElemIndex != -1) {
                OffHeapLongArray.set(elementNext_ptr, nextIndex, currentHashedElemIndex);
            }
            //now the object is reachable to other thread everything should be ready
            OffHeapLongArray.set(elementHash_ptr, hashIndex, nextIndex);
            //increase element count
            OffHeapLongArray.set(root_array_ptr, INDEX_ELEMENT_COUNT, elementCount + 1);
            //inform the listener
            this.listener.declareDirty(null);
        } else {
            if (OffHeapLongArray.get(elementV_ptr + 8, entry) != value && value != Constants.NULL_LONG) {
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


    public static long softClone(long srcAddr) {
        // clone root array
        long newSrcAddr = OffHeapLongArray.cloneArray(srcAddr, ROOT_ARRAY_SIZE);
        // link elementK array
        long elementK_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_K);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K, elementK_ptr);
        // link elementV array
        long elementV_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_V);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, elementV_ptr);
        // link elementNext array
        long elementNext_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_NEXT);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_NEXT, elementNext_ptr);
        // link elementHash array
        long elementHash_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_HASH);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_HASH, elementHash_ptr);

        return newSrcAddr;
    }

    /*
    public static long cloneMap(long srcAddr) {
        // capacity
        long capacity = OffHeapLongArray.get(srcAddr, INDEX_CAPACITY);

        // clone root array
        long newSrcAddr = OffHeapLongArray.cloneArray(srcAddr, 8);
        // copy elementK array
        long elementK_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_K);
        long newElementK_ptr = OffHeapLongArray.cloneArray(elementK_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_K, newElementK_ptr);
        // copy elementV array
        long elementV_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_V);
        long newElementV_ptr = OffHeapLongArray.cloneArray(elementV_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_V, newElementV_ptr);
        // copy elementNext array
        long elementNext_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_NEXT);
        long newElementNext_ptr = OffHeapLongArray.cloneArray(elementNext_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_NEXT, newElementNext_ptr);
        // copy elementHash array
        long elementHash_ptr = OffHeapLongArray.get(srcAddr, INDEX_ELEMENT_HASH);
        long newElementHash_ptr = OffHeapLongArray.cloneArray(elementHash_ptr, capacity);
        OffHeapLongArray.set(newSrcAddr, INDEX_ELEMENT_HASH, newElementHash_ptr);

        return newSrcAddr;
    }
    */

    public static long incrementCopyOnWriteCounter(long addr) {
        long elemV_ptr = OffHeapLongArray.get(addr, INDEX_ELEMENT_V);
        return unsafe.getAndAddLong(null, elemV_ptr, 1) + 1;
    }

    public static long decrementCopyOnWriteCounter(long addr) {
        long elemV_ptr = OffHeapLongArray.get(addr, INDEX_ELEMENT_V);
        return unsafe.getAndAddLong(null, elemV_ptr, -1) - 1;
    }
}

