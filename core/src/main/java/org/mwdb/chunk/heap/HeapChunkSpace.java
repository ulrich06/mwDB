
package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.KCallback;
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

/*
    final class InternalDirtyState implements KChunkIterator {

        private AtomicInteger dirtyHead = new AtomicInteger(-1);

        private AtomicInteger dirtySize = new AtomicInteger(0);

        private int[] dirtyNext;

        private PressHeapChunkSpace _parent;

        public InternalDirtyState(int p_maxEntries, PressHeapChunkSpace p_parent) {
            dirtyNext = new int[p_maxEntries];
            this._parent = p_parent;
        }

        public void declareDirty(int index) {
            int previous;
            boolean diff = false;
            do {
                previous = dirtyHead.get();
                if (previous != index) {
                    diff = true;
                }
            } while (!dirtyHead.compareAndSet(previous, index));
            if (diff) {
                this.dirtyNext[index] = previous;
                dirtySize.incrementAndGet();
            }
        }

        @Override
        public boolean hasNext() {
            return this.dirtyHead.get() != -1;
        }

        @Override
        public KChunk next() {
            int unpop;
            int unpopNext;
            do {
                unpop = this.dirtyHead.get();
                unpopNext = this.dirtyNext[unpop];
            } while (unpop != -1 && !this.dirtyHead.compareAndSet(unpop, unpopNext));
            if (unpop == -1) {
                return null;
            } else {
                return this._parent.values()[unpop];
            }
        }

        @Override
        public int size() {
            return this.dirtySize.get();
        }
    }
*/

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

    /*
    @Override
    public void setManager(KDataManager dataManager) {
        this._manager = (KInternalDataManager) dataManager;
    }
    */

    @Override
    public final KChunk get(long universe, long time, long obj) {
        if (this._elementCount.get() == 0) {
            return null;
        }
        int index = (PrimitiveHelper.tripleHash(universe, time, obj) & 0x7FFFFFFF) % this._maxEntries;
        int m = this.elementHash[index];
        while (m != -1) {
            if (universe == this.elementK3a[m] && time == this.elementK3b[m] && obj == elementK3c[m]) {
                //GET VALUE
                _lru.reenqueue(m);
                return this._values[m];
            } else {
                m = this.elementNext[m];
            }
        }
        return null;
    }

    @Override
    public KChunk create(long p_world, long p_time, long p_id, short p_type) {
        switch (p_type) {
            case Constants.OBJECT_CHUNK:
                return new HeapObjectChunk(p_world, p_time, p_id, this);
            case Constants.LONG_LONG_MAP:
                return new ArrayLongLongMap(p_world, p_time, p_id, this);
            case Constants.LONG_TREE:
                return new ArrayLongTree(p_world, p_time, p_id, this);
            case Constants.OBJECT_CHUNK_INDEX:
                //return new HeapObjectIndexChunk(p_universe, p_time, p_obj, this);
            default:
                return null;
        }
    }

    /*
    @Override
    public KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj) {
        KObjectChunk cloned = (KObjectChunk) internal_put(newUniverse, newTime, newObj, previousElement.clone(newUniverse, newTime, newObj, metaModel), metaModel);
        cloned.setFlags(KChunkFlags.DIRTY_BIT, 0);
        declareDirty(cloned);
        return cloned;
    }*/

    /*
    private KChunk internal_createElement(long p_universe, long p_time, long p_obj, short type) {

    }*/

    @Override
    public KChunk put(long p_world, long p_time, long p_id, KChunk p_elem) {
        KChunk result;
        int entry;
        int index;
        int hash = PrimitiveHelper.tripleHash(p_world, p_time, p_id);//  (int) (universe ^ time ^ p_obj);
        index = (hash & 0x7FFFFFFF) % this._maxEntries;
        entry = findNonNullKeyEntry(p_world, p_time, p_id, index);
        if (entry == -1) {
            //we look for nextIndex
            int nbTry = 0;
            int currentVictimIndex = this._lru.dequeue();
            while (this._values[currentVictimIndex] != null && (
                    this._values[currentVictimIndex].marks() > 0 /*&& nbTry < this._maxEntries*/
                            || (this._values[currentVictimIndex].getFlags() & Constants.DIRTY_BIT) == Constants.DIRTY_BIT)
                    ) {
                this._lru.enqueue(currentVictimIndex);
                currentVictimIndex = this._lru.dequeue();
                nbTry++;
                if (nbTry % (this._maxEntries / 10) == 0) {
                    System.gc();
                    System.err.println("GC " + nbTry);
                }
            }

            /*
            if (nbTry == this._maxEntries) {
                throw new RuntimeException("Press Cache is Full, too many object are reserved!");
            }*/

            if (this._values[currentVictimIndex] != null) {
                KChunk victim = this._values[currentVictimIndex];
                long victimUniverse = victim.world();
                long victimTime = victim.time();
                long victimObj = victim.id();
                int hashVictim = PrimitiveHelper.tripleHash(victimUniverse, victimTime, victimObj);
                int indexVictim = (hashVictim & 0x7FFFFFFF) % this._maxEntries;
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
                if ((victim.getFlags() & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT) {
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
            elementK3a[currentVictimIndex] = p_world;
            elementK3b[currentVictimIndex] = p_time;
            elementK3c[currentVictimIndex] = p_id;
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
    public void remove(long universe, long time, long obj) {
        //NOOP, external remove is not allowed in press mode
    }

    @Override
    public void declareDirty(KChunk dirtyChunk) {
        long world = dirtyChunk.world();
        long time = dirtyChunk.time();
        long id = dirtyChunk.id();
        int hash = PrimitiveHelper.tripleHash(world, time, id);
        int index = (hash & 0x7FFFFFFF) % this._maxEntries;
        int entry = findNonNullKeyEntry(world, time, id, index);
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

    /*
    private void saveChunk(KChunk chunk, KMetaModel p_metaModel, KCallback<Throwable> result) {
        if (this._manager != null) {
            KContentDeliveryDriver cdn = this._manager.cdn();
            if (cdn != null) {
                long[] key = new long[3];
                key[0] = chunk.universe();
                key[1] = chunk.time();
                key[2] = chunk.obj();
                String[] payload = new String[1];
                payload[0] = chunk.serialize(p_metaModel);
                cdn.put(key, payload, new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        chunk.setFlags(0, KChunkFlags.DIRTY_BIT);
                        result.on(throwable);
                    }
                }, -1);
            }
        }
    }*/

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        try {
            for (int i = 0; i < this._values.length; i++) {
                KChunk loopChunk = this._values[i];
                if (loopChunk != null) {
                    buffer.append(i + "#:" + this.elementK3a[i] + "," + this.elementK3b[i] + "," + this.elementK3c[i] + "=>" + loopChunk.chunkType() + "(count:" + loopChunk.marks() + ",flag:" + loopChunk.getFlags() + ")" + "==>" + loopChunk.serialize() + "\n");
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



