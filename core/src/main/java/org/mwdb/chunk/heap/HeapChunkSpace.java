
package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.KGraph;
import org.mwdb.chunk.*;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class HeapChunkSpace implements KChunkSpace, KChunkListener {

    /**
     * Global variables
     */
    private final int _maxEntries;
    private final int _threeshold;
    private final AtomicInteger _elementCount;
    private final KStack _lru;
    private KGraph _graph;

    /**
     * HashMap variables
     */
    private final int[] _elementNext;
    private final int[] _elementHash;
    private final KChunk[] _values;
    private final AtomicIntegerArray _elementHashLock;
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
        private final HeapChunkSpace _parent;

        public InternalDirtyStateList(int maxSize, HeapChunkSpace p_parent) {

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
            return this._parent._values[this._dirtyElements[previous]];
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

    public HeapChunkSpace(int maxEntries, int autoSavePercent) {
        this._maxEntries = maxEntries;
        this._threeshold = maxEntries / 100 * autoSavePercent;
        this._lru = new FixedStack(maxEntries);
        this._dirtyState = new AtomicReference<InternalDirtyStateList>();
        this._dirtyState.set(new InternalDirtyStateList(this._threeshold, this));

        //init std variables
        this._elementNext = new int[maxEntries];
        this._elementHashLock = new AtomicIntegerArray(new int[maxEntries]);
        this._elementHash = new int[maxEntries];
        this._values = new KChunk[maxEntries];
        this._elementCount = new AtomicInteger(0);

        //init internal structures
        for (int i = 0; i < maxEntries; i++) {
            this._elementNext[i] = -1;
            this._elementHash[i] = -1;
            this._elementHashLock.set(i, -1);
        }
    }

    @Override
    public final KChunk getAndMark(long world, long time, long id) {
        int index = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        int m = this._elementHash[index];
        while (m != -1) {
            KChunk foundChunk = this._values[m];
            if (foundChunk != null && world == foundChunk.world() && time == foundChunk.time() && id == foundChunk.id()) {
                //GET VALUE
                if (foundChunk.mark() == 1) {
                    //was at zero before, risky operation, check with LRU
                    if (this._lru.dequeue(m)) {
                        return foundChunk;
                    } else {
                        if (foundChunk.marks() > 1) {
                            //ok fine we are several on the same object...
                        } else {
                            //better return null the object will be recycled by somebody else...
                            return null;
                        }
                    }
                } else {
                    return foundChunk;
                }
            } else {
                m = this._elementNext[m];
            }
        }
        return null;
    }

    @Override
    public void unmark(long world, long time, long id) {
        int index = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        int m = this._elementHash[index];
        while (m != -1) {
            KChunk foundChunk = this._values[m];
            if (foundChunk != null && world == foundChunk.world() && time == foundChunk.time() && id == foundChunk.id()) {
                if (foundChunk.unmark() == 0) {
                    //check if object is dirty
                    if ((foundChunk.flags() & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                        //declare available for recycling
                        this._lru.enqueue(m);
                    }
                }
                return;
            } else {
                m = this._elementNext[m];
            }
        }
    }

    @Override
    public void unmarkChunk(KChunk chunk) {
        int marks = chunk.unmark();
        if (marks == 0) {
            if ((chunk.flags() & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                long nodeWorld = chunk.world();
                long nodeTime = chunk.time();
                long nodeId = chunk.id();
                int index = PrimitiveHelper.tripleHash(nodeWorld, nodeTime, nodeId, this._maxEntries);
                int m = this._elementHash[index];
                while (m != -1) {
                    KChunk foundChunk = this._values[m];
                    if (foundChunk != null && nodeWorld == foundChunk.world() && nodeTime == foundChunk.time() && nodeId == foundChunk.id()) {
                        this._lru.enqueue(m);
                        return;
                    } else {
                        m = this._elementNext[m];
                    }
                }
            }
        }
    }

    @Override
    public KChunk create(long p_world, long p_time, long p_id, byte p_type) {
        switch (p_type) {
            case Constants.STATE_CHUNK:
                return new HeapStateChunk(p_world, p_time, p_id, this);
            case Constants.WORLD_ORDER_CHUNK:
                return new HeapWorldOrderChunk(p_world, p_time, p_id, this);
            case Constants.TIME_TREE_CHUNK:
                return new HeapTimeTreeChunk(p_world, p_time, p_id, this);
        }
        return null;
    }

    @Override
    public KChunk putAndMark(KChunk p_elem) {
        //first mark the object
        if (p_elem.mark() != 1) {
            throw new RuntimeException("Warning, trying to put an unsafe object " + p_elem);
        }
        int entry = -1;
        int hashIndex = PrimitiveHelper.tripleHash(p_elem.world(), p_elem.time(), p_elem.id(), this._maxEntries);
        int m = this._elementHash[hashIndex];
        while (m >= 0) {
            KChunk currentM = this._values[m];
            if (currentM != null && p_elem.world() == currentM.world() && p_elem.time() == currentM.time() && p_elem.id() == currentM.id()) {
                entry = m;
                break;
            }
            m = this._elementNext[m];
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
            if (this._values[currentVictimIndex] != null) {
                KChunk victim = this._values[currentVictimIndex];
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
                    KChunk currentM = this._values[m];
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
                _values[currentVictimIndex] = null;

                //free the lock
                this._elementHashLock.set(indexVictim, -1);
                this._elementCount.decrementAndGet();
                //FREE VICTIM FROM MEMORY
                victim.free();
            }
            _values[currentVictimIndex] = p_elem;
            //negociate the lock to write on hashIndex
            while (!this._elementHashLock.compareAndSet(hashIndex, -1, 1)) ;
            _elementNext[currentVictimIndex] = _elementHash[hashIndex];
            _elementHash[hashIndex] = currentVictimIndex;
            //free the lock
            this._elementHashLock.set(hashIndex, -1);
            this._elementCount.incrementAndGet();
            return p_elem;
        } else {
            return _values[entry];
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
        int m = this._elementHash[hashIndex];
        while (m >= 0) {
            KChunk currentM = this._values[m];
            if (currentM != null && world == currentM.world() && time == currentM.time() && id == currentM.id()) {
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
            m = this._elementNext[m];
        }
        throw new RuntimeException("Try to declare a non existing object!");
    }

    @Override
    public void declareClean(KChunk cleanChunk) {
        long world = cleanChunk.world();
        long time = cleanChunk.time();
        long id = cleanChunk.id();
        int hashIndex = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        int m = this._elementHash[hashIndex];
        while (m >= 0) {
            KChunk currentM = this._values[m];
            if (currentM != null && world == currentM.world() && time == currentM.time() && id == currentM.id()) {
                cleanChunk.setFlags(0, Constants.DIRTY_BIT);
                if (currentM.marks() == 0) {
                    this._lru.enqueue(m);
                }
                return;
            }
            m = this._elementNext[m];
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
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        try {
            for (int i = 0; i < this._values.length; i++) {
                KChunk loopChunk = this._values[i];
                if (loopChunk != null) {
                    buffer.append(i);
                    buffer.append("#:");
                    buffer.append(loopChunk.world());
                    buffer.append(",");
                    buffer.append(loopChunk.time());
                    buffer.append(",");
                    buffer.append(loopChunk.id());
                    buffer.append("=>");
                    buffer.append(loopChunk.chunkType());
                    buffer.append(",");
                    buffer.append(loopChunk.save());
                    buffer.append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    @Override
    public final int size() {
        return this._elementCount.get();
    }

}



