
package org.mwg.core.chunk.offheap;

import org.mwg.Graph;
import org.mwg.core.CoreConstants;
import org.mwg.struct.*;
import org.mwg.core.chunk.*;
import org.mwg.core.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ignore ts
 */
public class OffHeapChunkSpace implements ChunkSpace, ChunkListener {

    /**
     * Global variables
     */
    private final long _capacity;
    private final long _saveBatchSize;
    private final Stack _lru;

    private Graph _graph;

    /**
     * HashMap variables
     */
    private final AtomicInteger _elementCount;

    private final long _elementNext;
    private final long _elementHash;
    private final long _elementValues;
    private final long _elementHashLock;

    private final AtomicReference<InternalDirtyStateList> _dirtyState;

    @Override
    public void setGraph(Graph p_graph) {
        this._graph = p_graph;
    }

    @Override
    public Graph graph() {
        return _graph;
    }

    final class InternalDirtyStateList implements ChunkIterator {

        private final long _dirtyElements;
        private final long _max;
        private final AtomicLong _iterationCounter;
        private final AtomicLong _nextCounter;
        private final OffHeapChunkSpace _parent;

        public InternalDirtyStateList(long dirtiesCapacity, OffHeapChunkSpace p_parent) {
            this._dirtyElements = OffHeapLongArray.allocate(dirtiesCapacity);
            this._nextCounter = new AtomicLong(0);
            this._iterationCounter = new AtomicLong(0);
            this._max = dirtiesCapacity;
            this._parent = p_parent;
        }

        @Override
        public void free() {
            OffHeapLongArray.free(_dirtyElements);
        }

        @Override
        public boolean hasNext() {
            return this._iterationCounter.get() < this._nextCounter.get();
        }

        @Override
        public Chunk next() {
            long previous;
            long next;
            boolean shouldReturnNull = false;
            do {
                previous = this._iterationCounter.get();
                if (this._nextCounter.get() == previous) {
                    OffHeapLongArray.free(this._dirtyElements);
                    return null;
                }
                next = previous + 1;
            } while (!this._iterationCounter.compareAndSet(previous, next));
            long chunkIndex = OffHeapLongArray.get(_dirtyElements, previous);
            long chunkRootAddr = OffHeapLongArray.get(_elementValues, chunkIndex);
            return this._parent.internal_create(chunkRootAddr);
        }

        public boolean declareDirty(long dirtyIndex) {
            long previousDirty;
            long nextDirty;
            do {
                previousDirty = this._nextCounter.get();
                if (previousDirty == this._max) {
                    return false;
                }
                nextDirty = previousDirty + 1;
            } while (!this._nextCounter.compareAndSet(previousDirty, nextDirty));
            //ok we have the token previous
            OffHeapLongArray.set(_dirtyElements, previousDirty, dirtyIndex);
            return true;
        }

        @Override
        public long size() {
            return this._nextCounter.get();
        }

    }

    public OffHeapChunkSpace(long initialCapacity, long saveBatchSize) {

        if (saveBatchSize > initialCapacity) {
            throw new RuntimeException("Save Batch Size can't be bigger than cache size");
        }

        this._capacity = initialCapacity;
        this._saveBatchSize = saveBatchSize;
        this._lru = new OffHeapFixedStack(initialCapacity); //only one object
        this._dirtyState = new AtomicReference<InternalDirtyStateList>();
        this._dirtyState.set(new InternalDirtyStateList(this._saveBatchSize, this));

        //init std variables
        this._elementNext = OffHeapLongArray.allocate(initialCapacity);
        this._elementHash = OffHeapLongArray.allocate(initialCapacity);
        this._elementValues = OffHeapLongArray.allocate(initialCapacity);
        this._elementHashLock = OffHeapLongArray.allocate(initialCapacity);
        this._elementCount = new AtomicInteger(0);
    }

    @Override
    public final Chunk getAndMark(byte type, long world, long time, long id) {
        long hashIndex = PrimitiveHelper.tripleHash(type, world, time, id, this._capacity);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == type
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                //CAS on the mark of the chunk
                long previousFlag;
                long newFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS);
                    newFlag = previousFlag + 1;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));

                if (newFlag == 1) {
                    //was at zero before, risky operation, check selectWith LRU
                    if (this._lru.dequeue(m)) {
                        return internal_create(foundChunkPtr);
                    } else {
                        if (OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS) > 1) {
                            //ok fine we are several on the same object...
                            return internal_create(foundChunkPtr);
                        } else {
                            //better return null the object will be recycled by somebody else...
                            return null;
                        }
                    }
                } else {
                    return internal_create(foundChunkPtr);
                }
            } else {
                m = OffHeapLongArray.get(_elementNext, m);
            }
        }
        return null;
    }

    private OffHeapChunk internal_create(long addr) {
        byte chunkType = (byte) OffHeapLongArray.get(addr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE);
        switch (chunkType) {
            case CoreConstants.STATE_CHUNK:
                return new OffHeapStateChunk(this, addr, null, null);
            case CoreConstants.TIME_TREE_CHUNK:
                return new OffHeapTimeTreeChunk(this, addr, null);
            case CoreConstants.WORLD_ORDER_CHUNK:
                return new OffHeapWorldOrderChunk(this, addr, null);
            default:
                return null;
        }
    }

    @Override
    public void unmark(byte type, long world, long time, long id) {
        long index = PrimitiveHelper.tripleHash(type, world, time, id, this._capacity);
        long m = OffHeapLongArray.get(_elementHash, index);
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == type
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                //CAS on the mark of the chunk
                long previousFlag;
                long newFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS);
                    newFlag = previousFlag - 1;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));
                //check if this object hasField to be re-enqueue to the list of available
                if (newFlag == 0) {
                    //declare available for recycling
                    this._lru.enqueue(m);
                }
                //in any case we go out, we have found the good chunk
                return;
            } else {
                m = OffHeapLongArray.get(_elementNext, m);
            }
        }
    }

    @Override
    public void unmarkChunk(Chunk chunk) {

        long chunkAddr = ((OffHeapChunk) chunk).addr();

        long previousMarks;
        long newMarks;
        do {
            previousMarks = OffHeapLongArray.get(chunkAddr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS);
            newMarks = previousMarks - 1;
        }
        while (!OffHeapLongArray.compareAndSwap(chunkAddr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, previousMarks, newMarks));
        if (newMarks == 0) {

            long world = chunk.world();
            long time = chunk.time();
            long id = chunk.id();
            byte type = chunk.chunkType();
            long hashIndex = PrimitiveHelper.tripleHash(type, world, time, id, this._capacity);
            long m = OffHeapLongArray.get(_elementHash, hashIndex);
            while (m != CoreConstants.OFFHEAP_NULL_PTR) {
                long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
                if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                        && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == type
                        && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                        && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == time
                        && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == id
                        ) {
                    //declare available for recycling
                    this._lru.enqueue(m);
                    break;
                } else {
                    m = OffHeapLongArray.get(_elementNext, m);
                }
            }
        }
    }

    @Override
    public Chunk create(byte p_type, long p_world, long p_time, long p_id, Buffer initialPayload, Chunk previousChunk) {
        OffHeapChunk newChunk = null;
        switch (p_type) {
            case CoreConstants.STATE_CHUNK:
                newChunk = new OffHeapStateChunk(this, CoreConstants.OFFHEAP_NULL_PTR, initialPayload, previousChunk);
                break;
            case CoreConstants.WORLD_ORDER_CHUNK:
                newChunk = new OffHeapWorldOrderChunk(this, CoreConstants.OFFHEAP_NULL_PTR, initialPayload);
                break;
            case CoreConstants.TIME_TREE_CHUNK:
                newChunk = new OffHeapTimeTreeChunk(this, CoreConstants.OFFHEAP_NULL_PTR, initialPayload);
                break;
        }
        if (newChunk != null) {
            long newChunkPtr = newChunk.addr();
            OffHeapLongArray.set(newChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD, p_world);
            OffHeapLongArray.set(newChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME, p_time);
            OffHeapLongArray.set(newChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID, p_id);

            OffHeapLongArray.set(newChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS, 0);
            OffHeapLongArray.set(newChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE, p_type);
            OffHeapLongArray.set(newChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, 0);
        }
        return newChunk;
    }

    @Override
    public Chunk putAndMark(Chunk p_elem) {

        final long elemPtr = ((OffHeapChunk) p_elem).addr();

        //First try to mark the chunk, the mark should be previously to zero
        long previousFlag;
        long newFlag;
        do {
            previousFlag = OffHeapLongArray.get(elemPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS);
            newFlag = previousFlag + 1;
        }
        while (!OffHeapLongArray.compareAndSwap(elemPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));

        if (newFlag != 1) {
            throw new RuntimeException("Warning, trying to put an unsafe object " + p_elem);
        }

        final long world = OffHeapLongArray.get(elemPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD);
        final long time = OffHeapLongArray.get(elemPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME);
        final long id = OffHeapLongArray.get(elemPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID);
        final byte type = (byte) OffHeapLongArray.get(elemPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE);

        long entry = -1;
        long hashIndex = PrimitiveHelper.tripleHash(type, world, time, id, this._capacity);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m != -1) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == type
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {
                entry = m;
                break;
            }
            m = OffHeapLongArray.get(_elementNext, m);
        }
        if (entry == -1) {
            //we look for nextIndex
            long currentVictimIndex = this._lru.dequeueTail();
            if (currentVictimIndex == -1) {
                //TODO cache is full :(
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentVictimIndex = this._lru.dequeueTail();
                if (currentVictimIndex == -1) {
                    throw new RuntimeException("mwDB crashed, cache is full, please avoid to much retention of nodes or augment cache capacity!");
                }
            }
            long currentVictimPtr = OffHeapLongArray.get(_elementValues, currentVictimIndex);
            if (currentVictimPtr != CoreConstants.OFFHEAP_NULL_PTR) {
                long victimWorld = OffHeapLongArray.get(currentVictimPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD);
                long victimTime = OffHeapLongArray.get(currentVictimPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME);
                long victimObj = OffHeapLongArray.get(currentVictimPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID);
                byte victimType = (byte) OffHeapLongArray.get(currentVictimPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE);

                long indexVictim = PrimitiveHelper.tripleHash(victimType, victimWorld, victimTime, victimObj, this._capacity);

                //negociate a lock on the indexVictim hash
                while (!OffHeapLongArray.compareAndSwap(_elementHashLock, indexVictim, -1, 0)) ;
                //we obtains the token, now remove the element
                m = OffHeapLongArray.get(_elementHash, indexVictim);
                long last = CoreConstants.OFFHEAP_NULL_PTR;
                while (m != CoreConstants.OFFHEAP_NULL_PTR) {
                    long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
                    if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                            && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == victimType
                            && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == victimWorld
                            && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == victimTime
                            && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == victimObj
                            ) {
                        break;
                    }
                    last = m;
                    m = OffHeapLongArray.get(_elementNext, m);
                }
                //POP THE VALUE FROM THE NEXT LIST
                if (last == CoreConstants.OFFHEAP_NULL_PTR) {
                    OffHeapLongArray.set(_elementHash, indexVictim, OffHeapLongArray.get(_elementNext, m));
                } else {
                    OffHeapLongArray.set(_elementNext, last, OffHeapLongArray.get(_elementNext, m));
                }
                OffHeapLongArray.set(_elementNext, m, CoreConstants.OFFHEAP_NULL_PTR);
                //free the lock

                if (!OffHeapLongArray.compareAndSwap(_elementHashLock, indexVictim, 0, -1)) {
                    throw new RuntimeException("CAS Error !!!");
                }
                this._elementCount.decrementAndGet();
                //FREE VICTIM FROM MEMORY
                byte chunkType = (byte) OffHeapLongArray.get(currentVictimPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE);
                switch (chunkType) {
                    case CoreConstants.STATE_CHUNK:
                        OffHeapStateChunk.free(currentVictimPtr);
                        break;
                    case CoreConstants.TIME_TREE_CHUNK:
                        OffHeapTimeTreeChunk.free(currentVictimPtr);
                        break;
                    case CoreConstants.WORLD_ORDER_CHUNK:
                        OffHeapWorldOrderChunk.free(currentVictimPtr);
                        break;
                }
            }
            OffHeapLongArray.set(_elementValues, currentVictimIndex, elemPtr);
            //negociate the lock to write on hashIndex
            while (!OffHeapLongArray.compareAndSwap(_elementHashLock, hashIndex, -1, 0)) ;
            OffHeapLongArray.set(_elementNext, currentVictimIndex, OffHeapLongArray.get(_elementHash, hashIndex));
            OffHeapLongArray.set(_elementHash, hashIndex, currentVictimIndex);
            //free the lock
            if (!OffHeapLongArray.compareAndSwap(_elementHashLock, hashIndex, 0, -1)) {
                throw new RuntimeException("CAS Error !!!");
            }

            this._elementCount.incrementAndGet();
            return p_elem;
        } else {
            //return the previous chunk
            return internal_create(OffHeapLongArray.get(_elementValues, entry));
        }
    }

    @Override
    public ChunkIterator detachDirties() {
        return _dirtyState.getAndSet(new InternalDirtyStateList(this._saveBatchSize, this));
    }

    @Override
    public void declareDirty(Chunk dirtyChunk) {

        long world = dirtyChunk.world();
        long time = dirtyChunk.time();
        long id = dirtyChunk.id();
        byte type = dirtyChunk.chunkType();
        long hashIndex = PrimitiveHelper.tripleHash(type, world, time, id, this._capacity);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == type
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                long previousFlag;
                long nextFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS);
                    nextFlag = previousFlag | CoreConstants.DIRTY_BIT;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS, previousFlag, nextFlag));
                if (previousFlag != nextFlag) {
                    //add an additional mark
                    long previousMarks;
                    long nextMarks;
                    do {
                        previousMarks = OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS);
                        nextMarks = previousMarks + 1;
                    }
                    while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, previousMarks, nextMarks));
                    //add to dirty list
                    boolean success = false;
                    while (!success) {
                        InternalDirtyStateList previousState = this._dirtyState.get();
                        success = previousState.declareDirty(m);
                        if (!success) {
                            this._graph.save(null);
                        }
                    }
                }
                return;
            }
            m = OffHeapLongArray.get(_elementNext, m);
        }
        throw new RuntimeException("Try to declare a non existing object!");
    }

    @Override
    public void declareClean(Chunk cleanChunk) {
        long world = cleanChunk.world();
        long time = cleanChunk.time();
        long id = cleanChunk.id();
        byte type = cleanChunk.chunkType();
        long hashIndex = PrimitiveHelper.tripleHash(type, world, time, id, this._capacity);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m != CoreConstants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != CoreConstants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE) == type
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                //remove the dirty bit
                long previousFlag;
                long nextFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS);
                    nextFlag = previousFlag & ~CoreConstants.DIRTY_BIT;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_FLAGS, previousFlag, nextFlag));
                //unmark
                long previousMarks;
                long nextMarks;
                do {
                    previousMarks = OffHeapLongArray.get(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS);
                    nextMarks = previousMarks - 1;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_MARKS, previousMarks, nextMarks));
                if (nextMarks == 0) {
                    this._lru.enqueue(m);
                }
                return;
            }
            m = OffHeapLongArray.get(_elementNext, m);
        }
        throw new RuntimeException("Try to declare a non existing object!");
    }

    @Override
    public final void clear() {
        //TODO
    }

    @Override
    public void free() {
        for (long i = 0; i < this._capacity; i++) {
            long previousPtr = OffHeapLongArray.get(_elementValues, i);
            if (previousPtr != CoreConstants.OFFHEAP_NULL_PTR) {
                byte chunkType = (byte) OffHeapLongArray.get(previousPtr, CoreConstants.OFFHEAP_CHUNK_INDEX_TYPE);
                switch (chunkType) {
                    case CoreConstants.STATE_CHUNK:
                        OffHeapStateChunk.free(previousPtr);
                        break;
                    case CoreConstants.TIME_TREE_CHUNK:
                        OffHeapTimeTreeChunk.free(previousPtr);
                        break;
                    case CoreConstants.WORLD_ORDER_CHUNK:
                        OffHeapWorldOrderChunk.free(previousPtr);
                        break;
                }
            }
        }
        _dirtyState.get().free();
        OffHeapLongArray.free(_elementNext);
        OffHeapLongArray.free(_elementHash);
        OffHeapLongArray.free(_elementValues);
        OffHeapLongArray.free(_elementHashLock);
        _lru.free();
    }

    @Override
    public void freeChunk(Chunk chunk) {
        OffHeapChunk casted = (OffHeapChunk) chunk;
        switch (casted.chunkType()) {
            case CoreConstants.STATE_CHUNK:
                OffHeapStateChunk.free(casted.addr());
                break;
            case CoreConstants.TIME_TREE_CHUNK:
                OffHeapTimeTreeChunk.free(casted.addr());
                break;
            case CoreConstants.WORLD_ORDER_CHUNK:
                OffHeapWorldOrderChunk.free(casted.addr());
                break;
        }
    }


    @Override
    public final long size() {
        return this._elementCount.get();
    }

}



