package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KStringLongMap;
import org.mwdb.chunk.KStringLongMapCallBack;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Unsafe;

/**
 * @ignore ts
 * <p>
 * Memory layout: all structures are memory blocks of either primitive values (as longs)
 * or pointers to memory blocks
 * <p>
 * <b>root structure:</b>
 * | size (long) | elementK (ptr) | elementV (ptr) | elementNext (ptr) | elementHash (ptr) | threshold (long) | elementCount (long) |
 * <b>elementK:</b>
 * | size * long |
 * <b>elementV:</b>
 * | size * long |
 * <b>elementNext:</b>
 * | size * long |
 * <b>elementHash:</b>
 * | size * long |
 */
// TODO synchronize put method
public class ArrayStringLongMap implements KStringLongMap, KOffHeapStateChunkElem {
    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    private final KChunkListener listener;
    private final long start_ptr;

    private static final int OFFSET_SIZE = 0;
    private static final int OFFSET_ELEMENT_K = OFFSET_SIZE + 8;
    private static final int OFFSET_ELEMENT_V = OFFSET_ELEMENT_K + 8;
    private static final int OFFSET_ELEMENT_NEXT = OFFSET_ELEMENT_V + 8;
    private static final int OFFSET_ELEMENT_HASH = OFFSET_ELEMENT_NEXT + 8;
    private static final int OFFSET_THRESHOLD = OFFSET_ELEMENT_HASH + 8;
    private static final int OFFSET_ELEMENT_COUNT = OFFSET_THRESHOLD + 8;


    public ArrayStringLongMap(KChunkListener listener, long initialCapacity) {
        this.listener = listener;

        // 7 fields of either long or ptr
        this.start_ptr = unsafe.allocateMemory(7 * 8);

        // size
        setSize(initialCapacity);
        // elementK
        long elementK_ptr = unsafe.allocateMemory(initialCapacity * 8);
        unsafe.setMemory(elementK_ptr, initialCapacity * 8, (byte) -1);
        setElementKPtr(elementK_ptr);
        // elementV
        long elementV_ptr = unsafe.allocateMemory(initialCapacity * 8);
        unsafe.setMemory(elementV_ptr, initialCapacity * 8, (byte) -1);
        setElementVPtr(elementV_ptr);
        // elementNext
        long elementNext_ptr = unsafe.allocateMemory(initialCapacity * 8);
        unsafe.setMemory(elementNext_ptr, initialCapacity * 8, (byte) -1);
        setElementNextPtr(elementNext_ptr);
        // elementHash
        long elementHash_ptr = unsafe.allocateMemory(initialCapacity * 8);
        unsafe.setMemory(elementHash_ptr, initialCapacity * 8, (byte) -1);
        setElementHashPtr(elementHash_ptr);
        // threshold
        setThreshold((long) (initialCapacity * Constants.MAP_LOAD_FACTOR));
        // elementCount
        setElementCount(0);
    }

    @Override
    public final long getValue(String key) {
        long size = getSize();
        if (size == 0) {
            return Constants.NULL_LONG;
        }
        long hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), size);
        long m = getElementHashValue(hashIndex);
        while (m >= 0) {
            if (PrimitiveHelper.equals(key, getElementKValue(m))) {
                return getElementVValue(m);
            } else {
                m = getElementNextValue(m);
            }
        }
        return Constants.NULL_LONG;
    }

    @Override
    public String getKey(long index) {
        long size = getSize();
        if (index < size) {
            return getElementKValue(index);
        }
        return null;
    }

    @Override
    public final synchronized void put(String key, long value) {
        long entry = -1;
        long hashIndex = -1;

        long size = getSize();
        if (size > 0) {
            hashIndex = PrimitiveHelper.longHash(PrimitiveHelper.stringHash(key), size);
            long m = getElementHashValue(hashIndex);
            while (m >= 0) {
                if (PrimitiveHelper.equals(key, getElementKValue(m))) {
                    entry = m;
                    break;
                }
                m = getElementNextValue(m);
            }
        }
        if (entry == -1) {
            //if need to reHash (too small or too much collisions)
            if ((getElementCount() + 1) > getThreshold()) {

                //rehashCapacity(state.elementDataSize);
                long newCapacity = size << 1;

                long elementK_ptr_tmp = getElementKPtr();
                long elementK_ptr = unsafe.allocateMemory(newCapacity * 8);
                unsafe.setMemory(elementK_ptr, newCapacity * 8, (byte) -1);
                unsafe.copyMemory(elementK_ptr_tmp, elementK_ptr, size * 8);
                setElementKPtr(elementK_ptr);
                unsafe.freeMemory(elementK_ptr_tmp);

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

    }

    @Override
    public void each(KStringLongMapCallBack callback) {
        long elementCount = getElementCount();
        for (int i = 0; i < elementCount; i++) {
            if (getElementNextValue(i) != -1) { //there is a real value
                callback.on(getElementKValue(i), getElementVValue(i));
            }
        }
    }

    @Override
    public int size() {
        return (int) getElementCount();
    }

    @Override
    public void remove(String key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    /**
     * @param ptr
     */
    private void setElementHashPtr(long ptr) {
        unsafe.putLong(this.start_ptr + OFFSET_ELEMENT_HASH, ptr);
    }

    /**
     * @return
     */
    private long getElementHashPtr() {
        return unsafe.getLong(this.start_ptr + OFFSET_ELEMENT_HASH);
    }

    /**
     * @param value
     */
    private void setElementNextPtr(long value) {
        unsafe.putLong(this.start_ptr + OFFSET_ELEMENT_NEXT, value);
    }

    /**
     * @return
     */
    private long getElementNextPtr() {
        return unsafe.getLong(this.start_ptr + OFFSET_ELEMENT_NEXT);
    }

    /**
     * @param value
     */
    private void setElementKPtr(long value) {
        unsafe.putLong(this.start_ptr + OFFSET_ELEMENT_K, value);
    }

    /**
     * @return
     */
    private long getElementKPtr() {
        return unsafe.getLong(this.start_ptr + OFFSET_ELEMENT_K);
    }

    /**
     * @param value
     */
    private void setElementVPtr(long value) {
        unsafe.putLong(this.start_ptr + OFFSET_ELEMENT_V, value);
    }

    /**
     * @return
     */
    private long getElementVPtr() {
        return unsafe.getLong(this.start_ptr + OFFSET_ELEMENT_V);
    }

    /**
     * @return
     */
    private long getSize() {
        return unsafe.getLong(this.start_ptr + OFFSET_SIZE);
    }

    /**
     * @param value
     */
    private void setSize(long value) {
        unsafe.putLong(this.start_ptr + OFFSET_SIZE, value);
    }

    /**
     * @return
     */
    private long getThreshold() {
        return unsafe.getLong(this.start_ptr + OFFSET_THRESHOLD);
    }

    /**
     * @param value
     */
    private void setThreshold(long value) {
        unsafe.putLong(this.start_ptr + OFFSET_THRESHOLD, value);
    }

    /**
     * @return
     */
    private long getElementCount() {
        return unsafe.getLong(this.start_ptr + OFFSET_ELEMENT_COUNT);
    }

    /**
     * @param value
     */
    private void setElementCount(long value) {
        unsafe.putLong(this.start_ptr + OFFSET_ELEMENT_COUNT, value);
    }

    /**
     * @param index
     * @return
     */
    private String getElementKValue(long index) {
        long stringPtr = getRelativeTo(getElementKPtr(), index);
        int length = unsafe.getInt(stringPtr);
        byte[] bytes = new byte[length];
        unsafe.copyMemory(null, stringPtr + 4, bytes, offset, length);
        return new String(bytes);
    }

    private static long offset = unsafe.arrayBaseOffset(byte[].class);

    private void setElementKValue(long index, String value) {
        long temp_stringPtr = getRelativeTo(getElementKPtr(), index);
        byte[] valueAsByte = value.getBytes();
        long newStringPtr = unsafe.allocateMemory(4 + valueAsByte.length);
        //copy size of the string
        unsafe.putInt(newStringPtr, value.length());
        for (int i = 0; i < valueAsByte.length; i++) {
            unsafe.putByte(4 + newStringPtr + i, valueAsByte[i]);
        }
        //register the new stringPtr
        setRelativeTo(getElementKPtr(), index, newStringPtr);
        //freeMemory if notNull
        if (temp_stringPtr != -1) {
            unsafe.freeMemory(temp_stringPtr);
        }
    }

    /**
     * @param index
     * @return
     */
    private long getElementVValue(long index) {
        return getRelativeTo(getElementVPtr(), index);
    }

    /**
     * @param index
     * @param value
     */
    private void setElementVValue(long index, long value) {
        setRelativeTo(getElementVPtr(), index, value);
    }

    /**
     * @param index
     * @return
     */
    private long getElementNextValue(long index) {
        return getRelativeTo(getElementNextPtr(), index);
    }

    /**
     * @param index
     * @param value
     */
    private void setElementNextValue(long index, long value) {
        setRelativeTo(getElementNextPtr(), index, value);
    }

    /**
     * @param index
     * @return
     */
    private long getElementHashValue(long index) {
        return getRelativeTo(getElementHashPtr(), index);
    }

    private void setElementHashValue(long index, long value) {
        setRelativeTo(getElementHashPtr(), index, value);
    }

    /**
     * @param address
     * @param index
     * @return
     */
    private long getRelativeTo(long address, long index) {
        if (address == -1) {
            throw new RuntimeException("memory is not initialized");
        }
        return unsafe.getLong(address + index * 8);
    }

    /**
     * @param address
     * @param index
     * @param value
     */
    private void setRelativeTo(long address, long index, long value) {
        if (address == -1) {
            throw new RuntimeException("memory is not initialized");
        }
        unsafe.putLong(address + index * 8, value);
    }

    @Override
    public void free() {
        unsafe.freeMemory(getElementHashPtr());
        unsafe.freeMemory(getElementNextPtr());
        unsafe.freeMemory(getElementVPtr());
        //free all String values
        long keys_adr = getElementKPtr();
        for (long i = 0; i < getElementCount(); i++) {
            unsafe.freeMemory(getRelativeTo(keys_adr, i));
        }
        unsafe.freeMemory(keys_adr);
        unsafe.freeMemory(start_ptr);
    }

}
