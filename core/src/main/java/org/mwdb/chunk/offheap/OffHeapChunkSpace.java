
package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.KGraph;
import org.mwdb.chunk.*;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
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

        private final AtomicInteger _nextCounter;
        private final int[] _dirtyElements;
        private final int _max;
        private final AtomicInteger _iterationCounter;
        private final OffHeapChunkSpace _parent;

        public InternalDirtyStateList(int maxSize, OffHeapChunkSpace p_parent) {

            this._dirtyElements = new int[maxSize];

            this._nextCounter = new AtomicInteger(0);
            this._iterationCounter = new AtomicInteger(0);

            this._max = maxSize;
            this._parent = p_parent;
        }

        @Override
        public boolean hasNext() {
            return this._iterationCounter.get() < this._nextCounter.get();
        }

        @Override
        public KChunk next() {
            int previous;
            int next;
            do {
                previous = this._iterationCounter.get();
                if (this._nextCounter.get() == previous) {
                    return null;
                }
                next = previous + 1;
            } while (!this._iterationCounter.compareAndSet(previous, next));
            return this._parent._elementValues[this._dirtyElements[previous]];
        }

        public boolean declareDirty(int dirtyIndex) {
            int previousDirty;
            int nextDirty;
            do {
                previousDirty = this._nextCounter.get();
                if (previousDirty == this._max) {
                    return false;
                }
                nextDirty = previousDirty + 1;
            } while (!this._nextCounter.compareAndSet(previousDirty, nextDirty));
            //ok we have the token previous
            this._dirtyElements[previousDirty] = dirtyIndex;
            return true;
        }

        @Override
        public int size() {
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
                        return internal_create(foundChunkPtr, m);
                    } else {
                        if (OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS) > 1) {
                            //ok fine we are several on the same object...
                            return internal_create(foundChunkPtr, m);
                        } else {
                            //better return null the object will be recycled by somebody else...
                            return null;
                        }
                    }
                } else {
                    return internal_create(foundChunkPtr, m);
                }
            } else {
                m = OffHeapLongArray.get(_elementNext, m);
            }
        }
        return null;
    }

    private KChunk internal_create(long addr, long chunkIndex) {
        byte chunkType = (byte) OffHeapLongArray.get(addr, Constants.OFFHEAP_CHUNK_INDEX_TYPE);
        switch (chunkType) {
            case Constants.STATE_CHUNK:
                //TODO
                break;
            case Constants.TIME_TREE_CHUNK:
                //TODO
                break;
            case Constants.WORLD_ORDER_CHUNK:
                //TODO
                break;
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
    public KChunk create(long p_world, long p_time, long p_id, byte p_type) {
        //TODO
        /*
        switch (p_type) {
            case Constants.STATE_CHUNK:
                return new HeapStateChunk(p_world, p_time, p_id, this);
            case Constants.WORLD_ORDER_CHUNK:
                return new HeapWorldOrderChunk(p_world, p_time, p_id, this);
            case Constants.TIME_TREE_CHUNK:
                return new HeapTimeTreeChunk(p_world, p_time, p_id, this);
        }
        */
        return null;
    }

    @Override
    public KChunk putAndMark(KChunk p_elem) {
        //first mark the object

        long world = p_elem.world();
        long time = p_elem.time();
        long id = p_elem.id();

        long previousFlag;
        long newFlag;
        do {
            previousFlag = OffHeapLongArray.get(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS);
            newFlag = previousFlag + 1;
        }
        while (!OffHeapLongArray.compareAndSwap(foundChunkPtr, Constants.OFFHEAP_CHUNK_INDEX_MARKS, previousFlag, newFlag));

        if (newFlag != 1) {
            throw new RuntimeException("Warning, trying to put an unsafe object " + p_elem);
        }

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
            int currentVictimIndex = (int) this._lru.dequeueTail();
            if (currentVictimIndex == -1) {
                //TODO cache is full :(
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                currentVictimIndex = (int) this._lru.dequeueTail();
                if (currentVictimIndex == -1) {
                    throw new RuntimeException("mwDB crashed, cache is full, please avoid to much retention of nodes or augment cache capacity!");
                }
            }
            if (this._elementValues[currentVictimIndex] != null) {
                KChunk victim = this._elementValues[currentVictimIndex];
                long victimWorld = victim.world();
                long victimTime = victim.time();
                long victimObj = victim.id();
                int indexVictim = PrimitiveHelper.tripleHash(victimWorld, victimTime, victimObj, this._maxEntries);

                //negociate a lock on the indexVictim hash
                while (!this._elementHashLock.compareAndSet(indexVictim, -1, 1)) ;
                //we obtains the token, now remove the element
                m = _elementHash[indexVictim];
                int last = -1;
                while (m >= 0) {
                    KChunk currentM = this._elementValues[m];
                    if (currentM != null && victimWorld == currentM.world() && victimTime == currentM.time() && victimObj == currentM.id()) {
                        break;
                    }
                    last = m;
                    m = _elementNext[m];
                }
                //POP THE VALUE FROM THE NEXT LIST
                if (last == -1) {
                    int previousNext = _elementNext[m];
                    _elementHash[indexVictim] = previousNext;
                } else {
                    _elementNext[last] = _elementNext[m];
                }
                _elementNext[m] = -1;//flag to dropped value
                //UNREF victim value object
                _elementValues[currentVictimIndex] = null;

                //free the lock
                this._elementHashLock.set(indexVictim, -1);
                this._elementCount.decrementAndGet();
                //FREE VICTIM FROM MEMORY
                victim.free();
            }
            _elementValues[currentVictimIndex] = p_elem;
            //negociate the lock to write on hashIndex
            while (!this._elementHashLock.compareAndSet(hashIndex, -1, 1)) ;
            _elementNext[currentVictimIndex] = _elementHash[hashIndex];
            _elementHash[hashIndex] = currentVictimIndex;
            //free the lock
            this._elementHashLock.set(hashIndex, -1);
            this._elementCount.incrementAndGet();
            return p_elem;
        } else {
            return _elementValues[entry];
        }
    }

    @Override
    public KChunkIterator detachDirties() {
        return _dirtyState.getAndSet(new InternalDirtyStateList(this._threeshold, this));
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
                //TODO hide this method
                dirtyChunk.setFlags(Constants.DIRTY_BIT, 0);
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

                //TODO hide the flags and marks methods
                cleanChunk.setFlags(0, Constants.DIRTY_BIT);
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



