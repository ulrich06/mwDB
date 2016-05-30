package org.mwg.core.chunk.heap;

import org.mwg.Constants;
import org.mwg.core.CoreConstants;
import org.mwg.core.chunk.ChunkListener;
import org.mwg.core.chunk.GenChunk;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.Base64;
import org.mwg.plugin.ChunkType;
import org.mwg.struct.Buffer;

import java.util.concurrent.atomic.AtomicLong;

public class HeapGenChunk implements GenChunk, HeapChunk {

    /**
     * @ignore ts
     */
    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    private final long _world;
    private final long _time;
    private final long _id;
    private final ChunkListener _space;

    private volatile long _flags;
    private volatile long _marks;


    /**
     * @ignore ts
     */
    private static final long _flagsOffset;
    /**
     * @ignore ts
     */
    private static final long _marksOffset;

    /** @ignore ts */
    static {
        try {
            _flagsOffset = unsafe.objectFieldOffset(HeapStateChunk.class.getDeclaredField("_flags"));
            _marksOffset = unsafe.objectFieldOffset(HeapStateChunk.class.getDeclaredField("_marks"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }


    /**
     * internal variable
     */

    /**
     * @native ts
     * private _prefix: Long;
     */
    private final long _prefix;

    private final AtomicLong _currentIndex;

    /**
     * @native ts
     * this._world = p_world;
     * this._time = p_time;
     * this._id = p_id;
     * this._space = p_space;
     * this._flags = 0;
     * this._marks = 0;
     * this._prefix = Long.fromNumber(p_id).shiftLeft((org.mwg.Constants.LONG_SIZE - org.mwg.Constants.PREFIX_SIZE));
     * this._currentIndex = new java.util.concurrent.atomic.AtomicLong(0);
     * this.load(initialPayload);
     */
    public HeapGenChunk(final long p_world, final long p_time, final long p_id, final ChunkListener p_space, Buffer initialPayload) {
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._space = p_space;

        this._flags = 0;
        this._marks = 0;

        //moves the prefix 53-size(short) times to the left;
        this._prefix = p_id << (Constants.LONG_SIZE - Constants.PREFIX_SIZE);
        this._currentIndex = new AtomicLong(0);
        load(initialPayload);

    }

    private void load(Buffer payload) {
        if (payload != null) {
            _currentIndex.compareAndSet(_currentIndex.get(), Base64.decodeToLongWithBounds(payload, 0, payload.size()));
        } else {
            _currentIndex.compareAndSet(_currentIndex.get(), 0);
        }
    }

    @Override
    public void save(Buffer buffer) {
        Base64.encodeLongToBuffer(_currentIndex.get(), buffer);
    }

    /**
     * @native ts
     * if (this._currentIndex.get() == org.mwg.Constants.KEY_PREFIX_MASK) {
     * throw new Error("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
     * }
     * var previousIndex = this._currentIndex.get();
     * this._currentIndex.compareAndSet(previousIndex,previousIndex+1);
     * this.internal_set_dirty();
     * var newIndex = this._currentIndex.get();
     * var objectKey = this._prefix.add(newIndex).toNumber();
     * if (objectKey >= org.mwg.Constants.NULL_LONG) {
     * throw new Error("Object Index exceeds the maximum JavaScript number capacity. (2^"+org.mwg.Constants.LONG_SIZE+")");
     * }
     * return objectKey;
     */
    @Override
    public long newKey() {
        long nextIndex = _currentIndex.incrementAndGet();
        if (_currentIndex.get() == Constants.KEY_PREFIX_MASK) {
            throw new IndexOutOfBoundsException("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
        }

        long objectKey = _prefix + nextIndex;
        internal_set_dirty();
        if (objectKey >= Constants.END_OF_TIME) {
            throw new IndexOutOfBoundsException("Object Index exceeds the maximum JavaScript number capacity. (2^" + Constants.LONG_SIZE + ")");
        }
        return objectKey;
    }


    @Override
    public final long world() {
        return this._world;
    }

    @Override
    public final long time() {
        return this._time;
    }

    @Override
    public final long id() {
        return this._id;
    }

    @Override
    public final byte chunkType() {
        return ChunkType.GEN_CHUNK;
    }

    @Override
    public final long marks() {
        return this._marks;
    }

    /**
     * @native ts
     * this._marks = this._marks + 1;
     * return this._marks
     */
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

    /**
     * @native ts
     * this._marks = this._marks - 1;
     * return this._marks
     */
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
    public final long flags() {
        return _flags;
    }

    /**
     * @native ts
     * var val = this._flags
     * var nval = val & ~bitsToDisable | bitsToEnable;
     * this._flags = nval;
     * return val != nval;
     */
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

    private void internal_set_dirty() {
        if (_space != null) {
            if ((_flags & CoreConstants.DIRTY_BIT) != CoreConstants.DIRTY_BIT) {
                _space.declareDirty(this);
            }
        }
    }

}
