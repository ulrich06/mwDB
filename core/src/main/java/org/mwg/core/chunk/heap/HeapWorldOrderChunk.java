
package org.mwg.core.chunk.heap;

import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.WorldOrderChunk;
import org.mwg.struct.Buffer;
import org.mwg.core.chunk.ChunkListener;
import org.mwg.struct.LongLongMapCallBack;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Unsafe;

/**
 * @ignore ts
 */
public class HeapWorldOrderChunk implements WorldOrderChunk, HeapChunk {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();
    private final long _world;
    private final long _time;
    private final long _id;
    private final ChunkListener _listener;

    private volatile int _lock;
    private volatile long _marks;
    private volatile long _magic;
    private volatile long _flags;
    private volatile long _extra;

    private static final long _flagsOffset;
    private static final long _marksOffset;
    private static final long _lockOffset;
    private static final long _magicOffset;

    static {
        try {
            _flagsOffset = unsafe.objectFieldOffset(HeapWorldOrderChunk.class.getDeclaredField("_flags"));
            _marksOffset = unsafe.objectFieldOffset(HeapWorldOrderChunk.class.getDeclaredField("_marks"));
            _lockOffset = unsafe.objectFieldOffset(HeapWorldOrderChunk.class.getDeclaredField("_lock"));
            _magicOffset = unsafe.objectFieldOffset(HeapWorldOrderChunk.class.getDeclaredField("_magic"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private volatile InternalState state;

    @Override
    public long world() {
        return this._world;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long id() {
        return this._id;
    }

    @Override
    public long extra() {
        return this._extra;
    }

    @Override
    public void setExtra(long extraValue) {
        this._extra = extraValue;
    }

    public HeapWorldOrderChunk(long p_universe, long p_time, long p_obj, ChunkListener p_listener, Buffer initialPayload) {
        this._world = p_universe;
        this._time = p_time;
        this._id = p_obj;
        this._flags = 0;
        this._marks = 0;
        this._lock = 0;
        this._magic = 0;
        this._extra = CoreConstants.NULL_LONG;

        this._listener = p_listener;
        if (initialPayload != null) {
            load(initialPayload);
        } else {
            int initialCapacity = CoreConstants.MAP_INITIAL_CAPACITY;
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity], 0);
            for (int i = 0; i < initialCapacity; i++) {
                newstate.elementNext[i] = -1;
                newstate.elementHash[i] = -1;
            }
            this.state = newstate;
        }
    }

    @Override
    public void lock() {
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;
    }

    @Override
    public void unlock() {
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    /**
     * Internal Map state, to be replace in a compare and swap manner
     */
    private final class InternalState {

        final int threshold;

        final int elementDataSize;

        final long[] elementKV;

        final int[] elementNext;

        final int[] elementHash;

        volatile int elementCount;

        InternalState(int elementDataSize, long[] elementKV, int[] elementNext, int[] elementHash, int elemCount) {
            this.elementDataSize = elementDataSize;
            this.elementKV = elementKV;
            this.elementNext = elementNext;
            this.elementHash = elementHash;
            this.threshold = (int) (elementDataSize * CoreConstants.MAP_LOAD_FACTOR);
            this.elementCount = elemCount;
        }
    }

    @Override
    public final long marks() {
        return this._marks;
    }


    @Override
    public final long mark() {
        long before;
        long after;
        do {
            before = _marks;
            after = before + 1;
        } while (!unsafe.compareAndSwapLong(this, _marksOffset, before, after));
        return after;
    }

    @Override
    public final long unmark() {
        long before;
        long after;
        do {
            before = _marks;
            after = before - 1;
        } while (!unsafe.compareAndSwapLong(this, _marksOffset, before, after));
        return after;
    }

    @Override
    public final long magic() {
        return this._magic;
    }

    private void rehashCapacity(int capacity, InternalState previousState) {
        int length = (capacity == 0 ? 1 : capacity << 1);
        long[] newElementKV = new long[length * 2];
        System.arraycopy(previousState.elementKV, 0, newElementKV, 0, previousState.elementKV.length);
        int[] newElementNext = new int[length];
        int[] newElementHash = new int[length];
        for (int i = 0; i < length; i++) {
            newElementNext[i] = -1;
            newElementHash[i] = -1;
        }
        //rehashEveryThing
        for (int i = 0; i < previousState.elementNext.length; i++) {
            if (previousState.elementNext[i] != -1) { //there is a real value
                int index = (int) PrimitiveHelper.longHash(previousState.elementKV[i * 2], length);
                int currentHashedIndex = newElementHash[index];
                if (currentHashedIndex != -1) {
                    newElementNext[i] = currentHashedIndex;
                } else {
                    newElementNext[i] = -2; //special char to tag used values
                }
                newElementHash[index] = i;
            }
        }
        //setPrimitiveType value for all
        state = new InternalState(length, newElementKV, newElementNext, newElementHash, state.elementCount);
    }

    @Override
    public final void each(LongLongMapCallBack callback) {
        final InternalState internalState = state;
        for (int i = 0; i < internalState.elementCount; i++) {
            callback.on(internalState.elementKV[i * 2], internalState.elementKV[i * 2 + 1]);
        }
    }

    @Override
    public final long get(long key) {
        final InternalState internalState = state;
        if (internalState.elementDataSize == 0) {
            return CoreConstants.NULL_LONG;
        }
        int index = (int) PrimitiveHelper.longHash(key, internalState.elementDataSize);
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (key == internalState.elementKV[m * 2]) {
                return internalState.elementKV[(m * 2) + 1];
            } else {
                m = internalState.elementNext[m];
            }
        }
        return CoreConstants.NULL_LONG;
    }

    @Override
    public final synchronized void put(long key, long value) {
        InternalState internalState = state;
        int entry = -1;
        int index = -1;
        if (internalState.elementDataSize != 0) {
            index = (int) PrimitiveHelper.longHash(key, internalState.elementDataSize);
            entry = findNonNullKeyEntry(key, index, internalState);
        }
        if (entry == -1) {
            if (++internalState.elementCount > internalState.threshold) {
                rehashCapacity(internalState.elementDataSize, internalState);
                internalState = state;
                index = (int) PrimitiveHelper.longHash(key, internalState.elementDataSize);
            }
            int newIndex = (internalState.elementCount - 1);
            internalState.elementKV[newIndex * 2] = key;
            internalState.elementKV[newIndex * 2 + 1] = value;
            int currentHashedIndex = internalState.elementHash[index];
            if (currentHashedIndex != -1) {
                internalState.elementNext[newIndex] = currentHashedIndex;
            } else {
                internalState.elementNext[newIndex] = -2; //special char to tag used values
            }
            //now the object is reachable to other thread everything should be ready
            internalState.elementHash[index] = newIndex;
            internal_set_dirty();
        } else {
            if (internalState.elementKV[entry + 1] != value) {
                //setValue
                internalState.elementKV[entry + 1] = value;
                internal_set_dirty();
            }
        }
    }

    private int findNonNullKeyEntry(long key, int index, InternalState internalState) {
        int m = internalState.elementHash[index];
        while (m >= 0) {
            if (key == internalState.elementKV[m * 2] /* getKey */) {
                return m;
            }
            m = internalState.elementNext[m];
        }
        return -1;
    }

    //TODO check intersection of remove and put
    @Override
    public synchronized final void remove(long key) {
        throw new RuntimeException("Not implemented yet!!!");
    }

    @Override
    public final long size() {
        return state.elementCount;
    }

    private void load(Buffer buffer) {
        if (buffer == null || buffer.size() == 0) {
            return;
        }
        int cursor = 0;
        long loopKey = CoreConstants.NULL_LONG;
        int previousStart = -1;
        long capacity = -1;
        int insertIndex = 0;
        InternalState temp_state = null;
        long bufferSize = buffer.size();
        boolean initDone = false;
        while (cursor < bufferSize) {
            if (buffer.read(cursor) == CoreConstants.CHUNK_SEP) {
                if (!initDone) {
                    long size = Base64.decodeToLongWithBounds(buffer, 0, cursor);
                    if (size == 0) {
                        capacity = 1;
                    } else {
                        capacity = size << 1;
                    }
                    long[] newElementKV = new long[(int) (capacity * 2)];
                    int[] newElementNext = new int[(int) capacity];
                    int[] newElementHash = new int[(int) capacity];
                    for (int i = 0; i < capacity; i++) {
                        newElementNext[i] = -1;
                        newElementHash[i] = -1;
                    }
                    //setPrimitiveType value for all
                    temp_state = new InternalState((int) capacity, newElementKV, newElementNext, newElementHash, (int) size);
                    //reset for next round
                    initDone = true;
                } else {
                    //extra char read
                    _extra = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                }
                previousStart = cursor + 1;
            } else if (buffer.read(cursor) == CoreConstants.CHUNK_SUB_SEP && temp_state != null) {
                if (loopKey != CoreConstants.NULL_LONG) {
                    long loopValue = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                    //insert raw
                    temp_state.elementKV[insertIndex * 2] = loopKey;
                    temp_state.elementKV[insertIndex * 2 + 1] = loopValue;

                    //insert hash
                    int hashIndex = (int) PrimitiveHelper.longHash(loopKey, capacity);
                    int currentHashedIndex = temp_state.elementHash[hashIndex];
                    if (currentHashedIndex != -1) {
                        temp_state.elementNext[insertIndex] = currentHashedIndex;
                    }
                    temp_state.elementHash[hashIndex] = insertIndex;
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
        if (loopKey != CoreConstants.NULL_LONG && temp_state != null) {
            long loopValue = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
            //insert raw
            temp_state.elementKV[insertIndex * 2] = loopKey;
            temp_state.elementKV[insertIndex * 2 + 1] = loopValue;

            //insert hash
            int hashIndex = (int) PrimitiveHelper.longHash(loopKey, capacity);
            int currentHashedIndex = temp_state.elementHash[hashIndex];
            if (currentHashedIndex != -1) {
                temp_state.elementNext[insertIndex] = currentHashedIndex;
            }
            temp_state.elementHash[hashIndex] = insertIndex;
        }
        if (temp_state != null) {
            this.state = temp_state;
        }
    }

    @Override
    public final void save(Buffer buffer) {
        final InternalState internalState = state;
        Base64.encodeLongToBuffer(internalState.elementCount, buffer);
        buffer.write(CoreConstants.CHUNK_SEP);
        if (_extra != CoreConstants.NULL_LONG) {
            Base64.encodeLongToBuffer(_extra, buffer);
            buffer.write(CoreConstants.CHUNK_SEP);
        }
        boolean isFirst = true;
        for (int i = 0; i < internalState.elementCount; i++) {
            long loopKey = internalState.elementKV[i * 2];
            long loopValue = internalState.elementKV[i * 2 + 1];
            if (!isFirst) {
                buffer.write(CoreConstants.CHUNK_SUB_SEP);
            }
            isFirst = false;
            Base64.encodeLongToBuffer(loopKey, buffer);
            buffer.write(CoreConstants.CHUNK_SUB_SUB_SEP);
            Base64.encodeLongToBuffer(loopValue, buffer);
        }
    }

    @Override
    public final byte chunkType() {
        return CoreConstants.WORLD_ORDER_CHUNK;
    }

    private void internal_set_dirty() {
        long magicBefore;
        long magicAfter;
        do {
            magicBefore = _magic;
            magicAfter = magicBefore + 1;
        } while (!unsafe.compareAndSwapLong(this, _magicOffset, magicBefore, magicAfter));
        if (_listener != null) {
            if ((_flags & CoreConstants.DIRTY_BIT) != CoreConstants.DIRTY_BIT) {
                _listener.declareDirty(this);
            }
        }
    }

    @Override
    public final long flags() {
        return _flags;
    }

    @Override
    public final boolean setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags;
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!unsafe.compareAndSwapLong(this, _flagsOffset, val, nval));
        return val != nval;
    }

}



