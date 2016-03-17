
package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.chunk.KLongLongMapCallBack;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KWorldOrderChunk;
import org.mwdb.plugin.KStorage;
import org.mwdb.utility.Base64;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class HeapWorldOrderChunk implements KWorldOrderChunk, KHeapChunk {

    private final long _world;
    private final long _time;
    private final long _id;
    private final AtomicLong _flags;
    private final AtomicInteger _counter;
    private final KChunkListener _listener;
    private final AtomicLong _lock;


    private volatile int elementCount;
    private AtomicReference<InternalState> state;
    private int threshold;

    private volatile long _magic;

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

    public HeapWorldOrderChunk(long p_universe, long p_time, long p_obj, KChunkListener p_listener, KStorage.KBuffer initialPayload) {
        this._world = p_universe;
        this._time = p_time;
        this._id = p_obj;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);

        this._lock = new AtomicLong();
        this._lock.set(0);

        this._listener = p_listener;
        this.elementCount = 0;
        this.state = new AtomicReference<InternalState>();
        if (initialPayload != null) {
            load(initialPayload);
        } else {
            int initialCapacity = Constants.MAP_INITIAL_CAPACITY;
            InternalState newstate = new InternalState(initialCapacity, new long[initialCapacity * 2], new int[initialCapacity], new int[initialCapacity]);
            for (int i = 0; i < initialCapacity; i++) {
                newstate.elementNext[i] = -1;
                newstate.elementHash[i] = -1;
            }
            this.state.set(newstate);
            this.threshold = (int) (newstate.elementDataSize * Constants.MAP_LOAD_FACTOR);
        }

        this._magic = PrimitiveHelper.rand();
    }

    @Override
    public void lock() {
        while (!this._lock.compareAndSet(0, 1)) ;
    }

    @Override
    public void unlock() {
        if (!this._lock.compareAndSet(1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }


    /**
     * Internal Map state, to be replace in a compare and swap manner
     */
    final class InternalState {

        public final int elementDataSize;

        public final long[] elementKV;

        public final int[] elementNext;

        public final int[] elementHash;

        public InternalState(int elementDataSize, long[] elementKV, int[] elementNext, int[] elementHash) {
            this.elementDataSize = elementDataSize;
            this.elementKV = elementKV;
            this.elementNext = elementNext;
            this.elementHash = elementHash;
        }
    }

    @Override
    public final long marks() {
        return this._counter.get();
    }

    @Override
    public final int mark() {
        return this._counter.incrementAndGet();
    }

    @Override
    public final int unmark() {
        return this._counter.decrementAndGet();
    }

    @Override
    public long magic() {
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
        state.set(new InternalState(length, newElementKV, newElementNext, newElementHash));
        this.threshold = (int) (length * Constants.MAP_LOAD_FACTOR);
    }

    @Override
    public final void each(KLongLongMapCallBack callback) {
        InternalState internalState = state.get();
        for (int i = 0; i < elementCount; i++) {
            callback.on(internalState.elementKV[i * 2], internalState.elementKV[i * 2 + 1]);
        }
    }

    @Override
    public final long get(long key) {
        InternalState internalState = state.get();
        if (internalState.elementDataSize == 0) {
            return Constants.NULL_LONG;
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
        return Constants.NULL_LONG;
    }

    @Override
    public final synchronized void put(long key, long value) {
        InternalState internalState = state.get();
        int entry = -1;
        int index = -1;
        if (internalState.elementDataSize != 0) {
            index = (int) PrimitiveHelper.longHash(key, internalState.elementDataSize);
            entry = findNonNullKeyEntry(key, index, internalState);
        }
        if (entry == -1) {
            if (++elementCount > threshold) {
                rehashCapacity(internalState.elementDataSize, internalState);
                internalState = state.get();
                index = (int) PrimitiveHelper.longHash(key, internalState.elementDataSize);
            }
            int newIndex = (this.elementCount - 1);
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
            this._magic = PrimitiveHelper.rand();
        } else {
            if (internalState.elementKV[entry + 1] != value) {
                //setValue
                internalState.elementKV[entry + 1] = value;
                internal_set_dirty();
                this._magic = PrimitiveHelper.rand();
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
        /*
        InternalState internalState = state;
        if (state.elementDataSize == 0) {
            return;
        }
        int index = ((int) (key) & 0x7FFFFFFF) % internalState.elementDataSize;
        int m = state.elementHash[index];
        int last = -1;
        while (m >= 0) {
            if (key == state.elementKV[m * 2]) {
                break;
            }
            last = m;
            m = state.elementNext[m];
        }
        if (m == -1) {
            return;
        }
        if (last == -1) {
            if (state.elementNext[m] > 0) {
                state.elementHash[index] = m;
            } else {
                state.elementHash[index] = -1;
            }
        } else {
            state.elementNext[last] = state.elementNext[m];
        }
        state.elementNext[m] = -1;//flag to dropped value
        this.elementCount--;
        this.droppedCount++;
        */
    }

    @Override
    public final long size() {
        return this.elementCount;
    }

    private void load(KStorage.KBuffer buffer) {
        if (buffer == null || buffer.size() == 0) {
            return;
        }

        int cursor = 0;

        long loopKey = Constants.NULL_LONG;
        int previousStart = -1;
        long capacity = -1;
        int insertIndex = 0;
        InternalState temp_state = null;
        long bufferSize = buffer.size();
        while (cursor < bufferSize) {
            if (buffer.read(cursor) == Constants.CHUNK_SEP) {
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
                temp_state = new InternalState((int) capacity, newElementKV, newElementNext, newElementHash);

                this.elementCount = (int) size;
                this.threshold = (int) (capacity * Constants.MAP_LOAD_FACTOR);

                //reset for next round
                previousStart = cursor + 1;

            } else if (buffer.read(cursor) == Constants.CHUNK_SUB_SEP && temp_state != null) {
                if (loopKey != Constants.NULL_LONG) {
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
                    loopKey = Constants.NULL_LONG;
                }
                previousStart = cursor + 1;
            } else if (buffer.read(cursor) == Constants.CHUNK_SUB_SUB_SEP) {
                loopKey = Base64.decodeToLongWithBounds(buffer, previousStart, cursor);
                previousStart = cursor + 1;
            }
            //loop in all case
            cursor++;
        }
        if (loopKey != Constants.NULL_LONG && temp_state != null) {
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
            this.state.set(temp_state);
        }
    }

    @Override
    public void save(KStorage.KBuffer buffer) {
        InternalState internalState = state.get();
        Base64.encodeIntToBuffer(elementCount, buffer);
        buffer.write(Constants.CHUNK_SEP);
        boolean isFirst = true;
        for (int i = 0; i < elementCount; i++) {
            long loopKey = internalState.elementKV[i * 2];
            long loopValue = internalState.elementKV[i * 2 + 1];
            if (!isFirst) {
                buffer.write(Constants.CHUNK_SUB_SEP);
            }
            isFirst = false;
            Base64.encodeLongToBuffer(loopKey, buffer);
            buffer.write(Constants.CHUNK_SUB_SUB_SEP);
            Base64.encodeLongToBuffer(loopValue, buffer);
        }
    }

    @Override
    public byte chunkType() {
        return Constants.WORLD_ORDER_CHUNK;
    }

    private void internal_set_dirty() {
        this._magic = PrimitiveHelper.rand();
        if (_listener != null) {
            if ((_flags.get() & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                _listener.declareDirty(this);
            }
        }
    }

    @Override
    public long flags() {
        return _flags.get();
    }

    @Override
    public boolean setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!_flags.compareAndSet(val, nval));
        return val != nval;
    }


}



