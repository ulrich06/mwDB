
package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.KGraph;
import org.mwdb.chunk.*;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class OffHeapChunkSpace implements KChunkSpace, KChunkListener {

    /**
     * Global variables
     */
    private final long _maxEntries;
    private final long _threshold;
    private final KStack _lru;

    private KGraph _graph;

    /**
     * HashMap variables
     */
    private final AtomicInteger _elementCount;

    private final long _elementNext;
    private final long _elementHash;
    private final long _elementValues;

    private final long _elementHashLock;
    private final long _capacity;

    private final AtomicReference<InternalDirtyStateList> _dirtyState;

    @Override
    public void setGraph(KGraph p_graph) {
        this._graph = p_graph;
    }

    final class InternalDirtyStateList implements KChunkIterator {

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
        public boolean hasNext() {
            return this._iterationCounter.get() < this._nextCounter.get();
        }

        @Override
        public KChunk next() {
            long previous;
            long next;
            do {
                previous = this._iterationCounter.get();
                if (this._nextCounter.get() == previous) {
                    return null;
                }
                next = previous + 1;
            } while (!this._iterationCounter.compareAndSet(previous, next));
            return this._parent.internal_create(OffHeapLongArray.get(_dirtyElements, previous));
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

    public OffHeapChunkSpace(long capacity, int autoSavePercent) {
        this._capacity = capacity;
        this._maxEntries = _capacity;
        this._threshold = _capacity / 100 * autoSavePercent;
        this._lru = new OffHeapFixedStack(_capacity, Constants.OFFHEAP_NULL_PTR); //only one object
        this._dirtyState = new AtomicReference<InternalDirtyStateList>();
        this._dirtyState.set(new InternalDirtyStateList(this._threshold, this));

        //init std variables
        this._elementNext = OffHeapLongArray.allocate(_capacity);
        this._elementHash = OffHeapLongArray.allocate(_capacity);
        this._elementValues = OffHeapLongArray.allocate(_capacity);
        this._elementHashLock = OffHeapLongArray.allocate(_capacity);

        this._elementCount = new AtomicInteger(0);

    }

    @Override
    public final KChunk getAndMark(long world, long time, long id) {
        int index = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        long m = OffHeapLongArray.get(_elementHash, index);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != Constants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                //CAS on the mark of the chunk
                long previousFlag;
                long newFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS);
                    newFlag = previousFlag + 1;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));

                if (newFlag == 1) {
                    //was at zero before, risky operation, check with LRU
                    if (this._lru.dequeue(m)) {
                        return internal_create(foundChunkPtr);
                    } else {
                        if (OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS) > 1) {
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

    private KOffHeapChunk internal_create(long addr) {
        byte chunkType = (byte) OffHeapLongArray.get(addr, Constants.OFFHEAP_CHUNK_INDEX_TYPE);
        switch (chunkType) {
            case Constants.STATE_CHUNK:
                //TODO
                return null;
            case Constants.TIME_TREE_CHUNK:
                //TODO
                return null;
            case Constants.WORLD_ORDER_CHUNK:
                //TODO
                return null;
            default:
                return null;
        }
    }

    @Override
    public void unmark(long world, long time, long id) {
        int index = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        long m = OffHeapLongArray.get(_elementHash, index);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != Constants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                //CAS on the mark of the chunk
                long previousFlag;
                long newFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS);
                    newFlag = previousFlag - 1;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));
                //check if this object has to be re-enqueue to the list of available
                if (newFlag == 0) {
                    //check if object is dirty
                    if (((int) (OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS)) & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                        //declare available for recycling
                        this._lru.enqueue(m);
                    }
                }
                //in any case we go out, we have found the good chunk
                return;
            } else {
                m = OffHeapLongArray.get(_elementNext, m);
            }
        }
    }

    @Override
    public void unmarkChunk(KChunk chunk) {
        unmark(chunk.world(), chunk.time(), chunk.id());
    }

    @Override
    public KChunk create(long p_world, long p_time, long p_id, byte p_type, String initialPayload, KChunk previousChunk) {
        KOffHeapChunk newChunk;
        switch (p_type) {
            case Constants.STATE_CHUNK:
                newChunk = null; //TODO
            case Constants.WORLD_ORDER_CHUNK:
                newChunk = null; //TODO
            case Constants.TIME_TREE_CHUNK:
                newChunk = null; //TODO
            default:
                newChunk = null;
        }
        if (newChunk != null) {
            long newChunkPtr = newChunk.addr();
            OffHeapLongArray.set(newChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD, p_world);
            OffHeapLongArray.set(newChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME, p_world);
            OffHeapLongArray.set(newChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID, p_world);

            OffHeapLongArray.set(newChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS, 0);
            OffHeapLongArray.set(newChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TYPE, p_type);
            OffHeapLongArray.set(newChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS, 0);
        }
        return newChunk;
    }

    @Override
    public KChunk putAndMark(KChunk p_elem) {

        final long elemPtr = ((KOffHeapChunk) p_elem).addr();

        //First try to mark the chunk, the mark should be previously to zero
        long previousFlag;
        long newFlag;
        do {
            previousFlag = OffHeapLongArray.get(elemPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS);
            newFlag = previousFlag + 1;
        }
        while (!OffHeapLongArray.compareAndSwap(elemPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));

        if (newFlag != 1) {
            throw new RuntimeException("Warning, trying to put an unsafe object " + p_elem);
        }

        final long world = OffHeapLongArray.get(elemPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD);
        final long time = OffHeapLongArray.get(elemPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME);
        final long id = OffHeapLongArray.get(elemPtr, Constants.OFFHEAP_CHUNK_INDEX_ID);

        long entry = -1;
        long hashIndex = PrimitiveHelper.tripleHash(p_elem.world(), p_elem.time(), p_elem.id(), this._maxEntries);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m >= 0) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != Constants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID) == id
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
            if (currentVictimPtr != Constants.OFFHEAP_NULL_PTR) {
                long victimWorld = OffHeapLongArray.get(currentVictimPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD);
                long victimTime = OffHeapLongArray.get(currentVictimPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME);
                long victimObj = OffHeapLongArray.get(currentVictimPtr, Constants.OFFHEAP_CHUNK_INDEX_ID);
                int indexVictim = PrimitiveHelper.tripleHash(victimWorld, victimTime, victimObj, this._maxEntries);

                //negociate a lock on the indexVictim hash
                while (!OffHeapLongArray.compareAndSwap(_elementHashLock, indexVictim, 0, 1)) ;
                //we obtains the token, now remove the element
                m = OffHeapLongArray.get(_elementHash, indexVictim);
                long last = Constants.OFFHEAP_NULL_PTR;
                while (m != Constants.OFFHEAP_NULL_PTR) {
                    long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
                    if (foundChunkPtr != Constants.OFFHEAP_NULL_PTR
                            && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD) == victimWorld
                            && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME) == victimTime
                            && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID) == victimObj
                            ) {
                        break;
                    }
                    last = m;
                    m = OffHeapLongArray.get(_elementNext, m);
                }
                //POP THE VALUE FROM THE NEXT LIST
                if (last == Constants.OFFHEAP_NULL_PTR) {
                    OffHeapLongArray.set(_elementHash, indexVictim, OffHeapLongArray.get(_elementNext, m));
                } else {
                    OffHeapLongArray.set(_elementNext, last, OffHeapLongArray.get(_elementNext, m));
                }
                OffHeapLongArray.set(_elementNext, m, Constants.OFFHEAP_NULL_PTR);
                //free the lock

                if (!OffHeapLongArray.compareAndSwap(_elementHashLock, indexVictim, 1, 0)) {
                    throw new RuntimeException("CAS Error !!!");
                }
                this._elementCount.decrementAndGet();
                //FREE VICTIM FROM MEMORY
                internal_create(currentVictimPtr).free();
            }
            OffHeapLongArray.set(_elementValues, currentVictimIndex, elemPtr);
            //negociate the lock to write on hashIndex
            while (!OffHeapLongArray.compareAndSwap(_elementHashLock, hashIndex, 0, 1)) ;
            OffHeapLongArray.set(_elementNext, currentVictimIndex, OffHeapLongArray.get(_elementHash, hashIndex));
            OffHeapLongArray.set(_elementHash, hashIndex, currentVictimIndex);
            //free the lock
            if (!OffHeapLongArray.compareAndSwap(_elementHashLock, hashIndex, 1, 0)) {
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
    public KChunkIterator detachDirties() {
        return _dirtyState.getAndSet(new InternalDirtyStateList(this._threshold, this));
    }

    @Override
    public void declareDirty(KChunk dirtyChunk) {
        long world = dirtyChunk.world();
        long time = dirtyChunk.time();
        long id = dirtyChunk.id();
        int hashIndex = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != Constants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                long previousFlag;
                long nextFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS);
                    nextFlag = previousFlag | Constants.DIRTY_BIT;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS, previousFlag, nextFlag));

                boolean success = false;
                while (!success) {
                    InternalDirtyStateList previousState = this._dirtyState.get();
                    success = previousState.declareDirty(m);
                    if (!success) {
                        this._graph.save(null);
                    }
                }
                return;
            }
            m = OffHeapLongArray.get(_elementNext, m);
        }
        throw new RuntimeException("Try to declare a non existing object!");
    }

    @Override
    public void declareClean(KChunk cleanChunk) {
        long world = cleanChunk.world();
        long time = cleanChunk.time();
        long id = cleanChunk.id();
        int hashIndex = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        long m = OffHeapLongArray.get(_elementHash, hashIndex);
        while (m != Constants.OFFHEAP_NULL_PTR) {
            long foundChunkPtr = OffHeapLongArray.get(_elementValues, m);
            if (foundChunkPtr != Constants.OFFHEAP_NULL_PTR
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_WORLD) == world
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_TIME) == time
                    && OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_ID) == id
                    ) {

                long previousFlag;
                long nextFlag;
                do {
                    previousFlag = OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS);
                    nextFlag = previousFlag & ~Constants.DIRTY_BIT;
                }
                while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_FLAGS, previousFlag, nextFlag));

                if (OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS) == 0) {
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
        //TODO
    }

    @Override
    public final int size() {
        return this._elementCount.get();
    }

}



