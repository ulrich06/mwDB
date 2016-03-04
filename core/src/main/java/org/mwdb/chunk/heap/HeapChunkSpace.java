
package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.KGraph;
import org.mwdb.chunk.*;
import org.mwdb.utility.PrimitiveHelper;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class HeapChunkSpace implements KChunkSpace, KChunkListener {

    /**
     * Global
     */
    private final int _maxEntries;

    private final int _threeshold;

    private final AtomicInteger _elementCount;

    private KStack _lru;

    private KGraph _graph;

    /**
     * HashMap variables
     */

    private final long[] elementK3a;
    private final long[] elementK3b;
    private final long[] elementK3c;

    private final int[] elementNext;

    private final int[] elementHash;

    private final AtomicIntegerArray elementHashLock;

    private final KChunk[] _values;

    public KChunk[] values() {
        return this._values;
    }

    private AtomicInteger _collisions;

    public int collisions() {
        return this._collisions.get();
    }

    @Override
    public void setGraph(KGraph p_graph) {
        this._graph = p_graph;
    }

    final class InternalDirtyStateList implements KChunkIterator {

        private final AtomicInteger _nextCounter;

        private final int[] _dirtyElements;

        private final int _max;

        private final AtomicInteger _iterationCounter;

        private HeapChunkSpace _parent;

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

    private final AtomicReference<InternalDirtyStateList> _dirtyState;

    private Random random;

    public HeapChunkSpace(int maxEntries, int autoSavePercent) {
        this._maxEntries = maxEntries;
        this._threeshold = maxEntries / 100 * autoSavePercent;

        this._lru = new FixedStack(maxEntries);
        this.random = new Random();
        this._collisions = new AtomicInteger(0);

        this._dirtyState = new AtomicReference<InternalDirtyStateList>();
        this._dirtyState.set(new InternalDirtyStateList(this._threeshold, this));

        //init std variables
        this.elementK3a = new long[maxEntries];
        this.elementK3b = new long[maxEntries];
        this.elementK3c = new long[maxEntries];

        this.elementNext = new int[maxEntries];
        this.elementHashLock = new AtomicIntegerArray(new int[maxEntries]);
        this.elementHash = new int[maxEntries];
        this._values = new KChunk[maxEntries];
        this._elementCount = new AtomicInteger(0);

        //init internal structures
        for (int i = 0; i < maxEntries; i++) {
            this.elementNext[i] = -1;
            this.elementHash[i] = -1;
            this.elementHashLock.set(i, -1);
        }
    }

    @Override
    public final KChunk getAndMark(long world, long time, long id) {
        if (this._elementCount.get() == 0) {
            return null;
        }
        int index = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        int m = this.elementHash[index];
        while (m != -1) {
            if (world == this.elementK3a[m] && time == this.elementK3b[m] && id == elementK3c[m]) {
                //GET VALUE
                //_lru.reenqueue(m);
                KChunk foundChunk = this._values[m];
                if (foundChunk.mark() == 1) {
                    //was at zero before, risky operation
                    //_lru.dequeue(m);
                    return foundChunk;
                    //TODO
                } else {
                    return foundChunk;
                }
            } else {
                m = this.elementNext[m];
            }
        }
        return null;
    }

    @Override
    public void unmark(long world, long time, long id) {
        if (this._elementCount.get() == 0) {
            return;
        }
        int index = PrimitiveHelper.tripleHash(world, time, id, this._maxEntries);
        int m = this.elementHash[index];
        while (m != -1) {
            if (world == this.elementK3a[m] && time == this.elementK3b[m] && id == elementK3c[m]) {
                //GET VALUE
                //_lru.reenqueue(m);
                KChunk foundChunk = this._values[m];
                if (foundChunk.unmark() == 0) {
                    //check if object is dirty
                    if ((foundChunk.flags() & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                        //declare available for recycling
                        this._lru.reenqueue(m);
                    }
                }
                return;
            } else {
                m = this.elementNext[m];
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
                int m = this.elementHash[index];
                while (m != -1) {
                    if (nodeWorld == this.elementK3a[m] && nodeTime == this.elementK3b[m] && nodeId == elementK3c[m]) {
                        this._lru.reenqueue(m);
                        return;
                    } else {
                        m = this.elementNext[m];
                    }
                }
            }
        }
    }

    @Override
    public KChunk create(long p_world, long p_time, long p_id, short p_type) {
        switch (p_type) {
            case Constants.STATE_CHUNK:
                return new HeapStateChunk(p_world, p_time, p_id, this);
            case Constants.LONG_LONG_MAP:
                return new HeapLongLongMapChunk(p_world, p_time, p_id, this);
            case Constants.LONG_TREE:
                return new ArrayLongTree(p_world, p_time, p_id, this);
            //case Constants.INDEX_STATE_CHUNK:
            //    return new HeapIndexStateChunk(p_world, p_time, p_id, this);
            default:
                return null;
        }
    }

    @Override
    public KChunk putAndMark(KChunk p_elem) {
        //first mark the object
        p_elem.mark();
        KChunk result;
        int entry;
        int index = PrimitiveHelper.tripleHash(p_elem.world(), p_elem.time(), p_elem.id(), this._maxEntries);
        entry = findNonNullKeyEntry(p_elem.world(), p_elem.time(), p_elem.id(), index);
        if (entry == -1) {
            //we look for nextIndex
            int nbTry = 0;
            int currentVictimIndex = this._lru.dequeueTail();
            while (this._values[currentVictimIndex] != null && (
                    this._values[currentVictimIndex].marks() > 0 /*&& nbTry < this._maxEntries*/
                            || (this._values[currentVictimIndex].flags() & Constants.DIRTY_BIT) == Constants.DIRTY_BIT)
                    ) {
                this._lru.enqueue(currentVictimIndex);
                currentVictimIndex = this._lru.dequeueTail();
                nbTry++;
                if (nbTry % (this._maxEntries / 10) == 0) {
                    System.gc();
                    System.err.println("GC " + nbTry);
                }
            }

            if (this._values[currentVictimIndex] != null) {
                KChunk victim = this._values[currentVictimIndex];
                long victimUniverse = victim.world();
                long victimTime = victim.time();
                long victimObj = victim.id();
                int indexVictim = PrimitiveHelper.tripleHash(victimUniverse, victimTime, victimObj, this._maxEntries);
                int previousMagic;
                do {
                    previousMagic = random.nextInt();
                } while (!this.elementHashLock.compareAndSet(indexVictim, -1, previousMagic));
                //we obtains the token, now remove the element
                int m = elementHash[indexVictim];
                int last = -1;
                while (m >= 0) {
                    if (victimUniverse == elementK3a[m] && victimTime == elementK3b[m] && victimObj == elementK3c[m]) {
                        break;
                    }
                    last = m;
                    m = elementNext[m];
                }
                //POP THE VALUE FROM THE NEXT LIST
                if (last == -1) {
                    int previousNext = elementNext[m];
                    elementHash[indexVictim] = previousNext;
                } else {
                    elementNext[last] = elementNext[m];
                }
                elementNext[m] = -1;//flag to dropped value

                //UNREF victim value object
                _values[currentVictimIndex] = null;

                //free the lock
                this.elementHashLock.compareAndSet(indexVictim, previousMagic, -1);
                this._elementCount.decrementAndGet();

                /*
                //TEST IF VICTIM IS DIRTY
                if ((victim.flags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT) {
                    //SAVE VICTIM
                    saveChunk(victim, metaModel, new KCallback<Throwable>() {
                        @Override
                        public void on(Throwable throwable) {
                            //free victim from memory
                            victim.free(metaModel);
                        }
                    });
                } else {
                    //FREE VICTIM FROM MEMORY
                    victim.free(metaModel);
                }*/

                //FREE VICTIM FROM MEMORY
                victim.free();

            }
            elementK3a[currentVictimIndex] = p_elem.world();
            elementK3b[currentVictimIndex] = p_elem.time();
            elementK3c[currentVictimIndex] = p_elem.id();
            _values[currentVictimIndex] = p_elem;

            int previousMagic;
            do {
                previousMagic = random.nextInt();
            } while (!this.elementHashLock.compareAndSet(index, -1, previousMagic));

            if (elementHash[index] != -1) {
                this._collisions.incrementAndGet();
            }

            elementNext[currentVictimIndex] = elementHash[index];
            elementHash[index] = currentVictimIndex;
            result = p_elem;
            //free the lock
            this.elementHashLock.compareAndSet(index, previousMagic, -1);
            this._elementCount.incrementAndGet();
            //reEnqueue
            this._lru.enqueue(currentVictimIndex);
        } else {
            result = _values[entry];
            this._lru.reenqueue(entry);
        }
        return result;
    }

    private int findNonNullKeyEntry(long universe, long time, long obj, int index) {
        int m = this.elementHash[index];
        while (m >= 0) {
            if (universe == this.elementK3a[m] && time == this.elementK3b[m] && obj == this.elementK3c[m]) {
                return m;
            }
            m = this.elementNext[m];
        }
        return -1;
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
        int entry = findNonNullKeyEntry(world, time, id, hashIndex);
        if (entry != -1) {
            boolean success = false;
            while (!success) {
                InternalDirtyStateList previousState = this._dirtyState.get();
                success = previousState.declareDirty(entry);
                if (!success) {
                    /*
                    _manager.saveDirtyList(_dirtyState.getAndSet(new InternalDirtyStateList(this._threeshold, this)), new KCallback<Throwable>() {
                        @Override
                        public void on(Throwable throwable) {
                            if (throwable != null) {
                                throwable.printStackTrace();
                            }
                        }
                    });
                    */
                }
            }
        } else {
            throw new RuntimeException("Try to declare a non existing object!");
        }
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
                    buffer.append(i + "#:" + this.elementK3a[i] + "," + this.elementK3b[i] + "," + this.elementK3c[i] + "=>" + loopChunk.chunkType() + "(count:" + loopChunk.marks() + ",flag:" + loopChunk.flags() + ")" + "==>" + loopChunk.save() + "\n");
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



