package org.mwg.core.chunk.offheap;

import org.mwg.struct.Buffer;
import org.mwg.core.Constants;
import org.mwg.core.chunk.KChunkListener;
import org.mwg.core.chunk.KTimeTreeChunk;
import org.mwg.core.chunk.KTreeWalker;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.PrimitiveHelper;

/**
 * @ignore ts
 */
public class OffHeapTimeTreeChunk implements KTimeTreeChunk, KOffHeapChunk {

    //constants definition
    private static final byte BLACK_LEFT = '{';
    private static final byte BLACK_RIGHT = '}';
    private static final byte RED_LEFT = '[';
    private static final byte RED_RIGHT = ']';
    private static final int META_SIZE = 3;

    /**
     * Global KChunk indexes
     */
    private static final int INDEX_WORLD = Constants.OFFHEAP_CHUNK_INDEX_WORLD;
    private static final int INDEX_TIME = Constants.OFFHEAP_CHUNK_INDEX_TIME;
    private static final int INDEX_ID = Constants.OFFHEAP_CHUNK_INDEX_ID;
    private static final int INDEX_TYPE = Constants.OFFHEAP_CHUNK_INDEX_TYPE;
    private static final int INDEX_FLAGS = Constants.OFFHEAP_CHUNK_INDEX_FLAGS;
    private static final int INDEX_MARKS = Constants.OFFHEAP_CHUNK_INDEX_MARKS;

    /**
     * Local indexes
     */
    private static final int INDEX_META = 6;
    private static final int INDEX_K = 7;
    private static final int INDEX_COLORS = 8;
    private static final int INDEX_ROOT_ELEM = 9;
    private static final int INDEX_THRESHOLD = 10;
    private static final int INDEX_MAGIC = 11;
    private static final int INDEX_LOCK = 12;
    private static final int INDEX_SIZE = 13;

    private final long addr;

    private long metaPtr;
    private long kPtr;
    private long colorsPtr;

    private final KChunkListener _listener;

    public OffHeapTimeTreeChunk(KChunkListener p_listener, long previousAddr, Buffer initialPayload) {
        //listener
        this._listener = p_listener;
        //init
        if (previousAddr != Constants.OFFHEAP_NULL_PTR) {
            addr = previousAddr;
        } else if (initialPayload != null) {
            addr = OffHeapLongArray.allocate(14);
            load(initialPayload);
        } else {
            addr = OffHeapLongArray.allocate(14);
            long capacity = Constants.MAP_INITIAL_CAPACITY;
            //init k array
            kPtr = OffHeapLongArray.allocate(capacity);
            OffHeapLongArray.set(addr, INDEX_K, kPtr);
            //init meta array
            metaPtr = OffHeapLongArray.allocate(capacity * META_SIZE);
            OffHeapLongArray.set(addr, INDEX_META, metaPtr);
            //init colors array
            colorsPtr = OffHeapByteArray.allocate(capacity);
            OffHeapLongArray.set(addr, INDEX_COLORS, colorsPtr);

            OffHeapLongArray.set(addr, INDEX_LOCK, 0);
            OffHeapLongArray.set(addr, INDEX_SIZE, 0);
            OffHeapLongArray.set(addr, INDEX_FLAGS, 0);
            OffHeapLongArray.set(addr, INDEX_ROOT_ELEM, -1);
            OffHeapLongArray.set(addr, INDEX_THRESHOLD, (long) (capacity * Constants.MAP_LOAD_FACTOR));
            OffHeapLongArray.set(addr, INDEX_MAGIC, 0);
        }

    }

    @Override
    public final void clearAt(long max) {
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        try {
            ptrConsistency();
            long previousKeys = OffHeapLongArray.get(addr, INDEX_K);
            long previousMetas = OffHeapLongArray.get(addr, INDEX_META);
            long previousColors = OffHeapLongArray.get(addr, INDEX_COLORS);
            long previousSize = OffHeapLongArray.get(addr, INDEX_SIZE);

            //reset
            long capacity = Constants.MAP_INITIAL_CAPACITY;
            //init k array
            kPtr = OffHeapLongArray.allocate(capacity);
            OffHeapLongArray.set(addr, INDEX_K, kPtr);
            //init meta array
            metaPtr = OffHeapLongArray.allocate(capacity * META_SIZE);
            OffHeapLongArray.set(addr, INDEX_META, metaPtr);
            //init colors array
            colorsPtr = OffHeapByteArray.allocate(capacity);
            OffHeapLongArray.set(addr, INDEX_COLORS, colorsPtr);

            OffHeapLongArray.set(addr, INDEX_SIZE, 0);
            OffHeapLongArray.set(addr, INDEX_ROOT_ELEM, -1);
            OffHeapLongArray.set(addr, INDEX_THRESHOLD, (long) (capacity * Constants.MAP_LOAD_FACTOR));
            OffHeapLongArray.set(addr, INDEX_MAGIC, PrimitiveHelper.rand());

            for (long i = 0; i < previousSize; i++) {
                long currentVal = OffHeapLongArray.get(previousKeys, i);
                if (currentVal < max) {
                    internal_insert(OffHeapLongArray.get(previousKeys, i));
                }
            }

            OffHeapLongArray.free(previousKeys);
            OffHeapLongArray.free(previousMetas);
            OffHeapByteArray.free(previousColors);
        } finally {
            //Free OffHeap lock
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    public static void free(long addr) {
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_K));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_META));
        OffHeapByteArray.free(OffHeapLongArray.get(addr, INDEX_COLORS));
        OffHeapLongArray.free(addr);
    }

    @Override
    public final long magic() {
        return OffHeapLongArray.get(addr, INDEX_MAGIC);
    }

    @Override
    public final byte chunkType() {
        return Constants.TIME_TREE_CHUNK;
    }

    @Override
    public final long addr() {
        return this.addr;
    }

    @Override
    public final long marks() {
        return OffHeapLongArray.get(addr, INDEX_MARKS);
    }

    @Override
    public final long world() {
        return OffHeapLongArray.get(addr, INDEX_WORLD);
    }

    @Override
    public final long time() {
        return OffHeapLongArray.get(addr, INDEX_TIME);
    }

    @Override
    public final long id() {
        return OffHeapLongArray.get(addr, INDEX_ID);
    }

    @Override
    public final long flags() {
        return OffHeapLongArray.get(addr, INDEX_FLAGS);
    }

    @Override
    public final long size() {
        return OffHeapLongArray.get(addr, INDEX_SIZE);
    }

    @Override
    public final void range(long startKey, long endKey, long maxElements, KTreeWalker walker) {
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        try {
            ptrConsistency();

            long nbElements = 0;
            long indexEnd = internal_previousOrEqual_index(endKey);
            while (indexEnd != -1 && key(indexEnd) >= startKey && nbElements < maxElements) {
                walker.elem(key(indexEnd));
                nbElements++;
                indexEnd = previous(indexEnd);
            }

        } finally {
            //Free OffHeap lock
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    @Override
    public final long previousOrEqual(long key) {
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        try {
            ptrConsistency();
            long result = internal_previousOrEqual_index(key);
            long resultKey;
            if (result != -1) {
                resultKey = key(result);
            } else {
                resultKey = Constants.NULL_LONG;
            }
            return resultKey;
        } finally {
            //Free OffHeap lock
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    @Override
    public synchronized final void save(Buffer buffer) {
        //lock and load fromVar main memory
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        try {
            ptrConsistency();

            if (OffHeapLongArray.get(addr, INDEX_ROOT_ELEM) == Constants.OFFHEAP_NULL_PTR) {
                return;
            }

            long treeSize = OffHeapLongArray.get(addr, INDEX_SIZE);
            boolean isFirst = true;
            for (int i = 0; i < treeSize; i++) {
                if (!isFirst) {
                    buffer.write(Constants.CHUNK_SUB_SEP);
                } else {
                    isFirst = false;
                }
                Base64.encodeLongToBuffer(OffHeapLongArray.get(kPtr, i), buffer);
            }

        } finally {
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    private void ptrConsistency() {
        if (kPtr != OffHeapLongArray.get(addr, INDEX_K)) {
            kPtr = OffHeapLongArray.get(addr, INDEX_K);
            metaPtr = OffHeapLongArray.get(addr, INDEX_META);
            colorsPtr = OffHeapLongArray.get(addr, INDEX_COLORS);
        }
    }

    @Override
    public final void insert(long p_key) {
        //OffHeap lock
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        try {
            ptrConsistency();

            internal_insert(p_key);
        } finally {
            //Free OffHeap lock
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }

    private void internal_insert(long p_key) {

        long size = OffHeapLongArray.get(addr, INDEX_SIZE);
        if ((size + 1) > OffHeapLongArray.get(addr, INDEX_THRESHOLD)) {

            long newLength = (size == 0 ? 1 : size << 1);

            kPtr = OffHeapLongArray.reallocate(kPtr, size, newLength);
            metaPtr = OffHeapLongArray.reallocate(metaPtr, size * META_SIZE, newLength * META_SIZE);
            colorsPtr = OffHeapByteArray.reallocate(colorsPtr, size, newLength);

            OffHeapLongArray.set(addr, INDEX_K, kPtr);
            OffHeapLongArray.set(addr, INDEX_META, metaPtr);
            OffHeapLongArray.set(addr, INDEX_COLORS, colorsPtr);
            OffHeapLongArray.set(addr, INDEX_THRESHOLD, (long) (newLength * Constants.MAP_LOAD_FACTOR));

        }
        if (size == 0) {
            setKey(size, p_key);
            setColor(size, false);
            setLeft(size, -1);
            setRight(size, -1);
            setParent(size, -1);
            OffHeapLongArray.set(addr, INDEX_ROOT_ELEM, size);
            OffHeapLongArray.set(addr, INDEX_SIZE, 1);
        } else {
            long n = OffHeapLongArray.get(addr, INDEX_ROOT_ELEM);
            while (true) {
                if (p_key == key(n)) {
                    return;
                } else if (p_key < key(n)) {
                    if (left(n) == -1) {
                        setKey(size, p_key);
                        setColor(size, false);
                        setLeft(size, -1);
                        setRight(size, -1);
                        setParent(size, -1);
                        setLeft(n, size);
                        OffHeapLongArray.set(addr, INDEX_SIZE, size + 1);
                        break;
                    } else {
                        n = left(n);
                    }
                } else {
                    if (right(n) == -1) {
                        setKey(size, p_key);
                        setColor(size, false);
                        setLeft(size, -1);
                        setRight(size, -1);
                        setParent(size, -1);
                        setRight(n, size);
                        OffHeapLongArray.set(addr, INDEX_SIZE, size + 1);
                        break;
                    } else {
                        n = right(n);
                    }
                }
            }
            setParent(size, n);
        }
        insertCase1(size);
        internal_set_dirty();
    }

    private long internal_previousOrEqual_index(long p_key) {

        long p = OffHeapLongArray.get(addr, INDEX_ROOT_ELEM);
        if (p == -1) {
            return p;
        }

        while (p != -1) {
            if (p_key == key(p)) {
                return p;
            }
            if (p_key > key(p)) {
                if (right(p) != -1) {
                    p = right(p);
                } else {
                    return p;
                }
            } else {
                if (left(p) != -1) {
                    p = left(p);
                } else {
                    long parent = parent(p);
                    long ch = p;
                    while (parent != -1 && ch == left(parent)) {
                        ch = parent;
                        parent = parent(parent);
                    }
                    return parent;
                }
            }
        }

        return -1;
    }

    private void internal_set_dirty() {
        if (!inLoad) {
            long previousMagic;
            long nextMagic;
            do {
                previousMagic = OffHeapLongArray.get(addr, INDEX_MAGIC);
                nextMagic = previousMagic + 1;
            } while (!OffHeapLongArray.compareAndSwap(addr, INDEX_MAGIC, previousMagic, nextMagic));


            if (_listener != null) {
                if ((OffHeapLongArray.get(addr, INDEX_FLAGS) & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                    _listener.declareDirty(this);
                }
            }
        }
    }

    private long key(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return OffHeapLongArray.get(kPtr, p_currentIndex);
    }

    private void setKey(long p_currentIndex, long p_paramIndex) {
        OffHeapLongArray.set(kPtr, p_currentIndex, p_paramIndex);
    }

    private long left(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return OffHeapLongArray.get(metaPtr, p_currentIndex * META_SIZE);
    }

    private void setLeft(long p_currentIndex, long p_paramIndex) {
        OffHeapLongArray.set(metaPtr, p_currentIndex * META_SIZE, p_paramIndex);
    }

    private long right(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return OffHeapLongArray.get(metaPtr, (p_currentIndex * META_SIZE) + 1);
    }

    private void setRight(long p_currentIndex, long p_paramIndex) {
        OffHeapLongArray.set(metaPtr, (p_currentIndex * META_SIZE) + 1, p_paramIndex);
    }

    private long parent(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return OffHeapLongArray.get(metaPtr, (p_currentIndex * META_SIZE) + 2);
    }

    private void setParent(long p_currentIndex, long p_paramIndex) {
        OffHeapLongArray.set(metaPtr, (p_currentIndex * META_SIZE) + 2, p_paramIndex);
    }

    private boolean color(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return true;
        }
        return OffHeapByteArray.get(colorsPtr, p_currentIndex) == 1;
    }

    private void setColor(long p_currentIndex, boolean p_paramIndex) {
        if (p_paramIndex) {
            OffHeapByteArray.set(colorsPtr, p_currentIndex, (byte) 1);
        } else {
            OffHeapByteArray.set(colorsPtr, p_currentIndex, (byte) 0);
        }
    }

    private long grandParent(long p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        if (parent(p_currentIndex) != -1) {
            return parent(parent(p_currentIndex));
        } else {
            return -1;
        }
    }

    private long sibling(long p_currentIndex) {
        if (parent(p_currentIndex) == -1) {
            return -1;
        } else {
            if (p_currentIndex == left(parent(p_currentIndex))) {
                return right(parent(p_currentIndex));
            } else {
                return left(parent(p_currentIndex));
            }
        }
    }

    private long uncle(long p_currentIndex) {
        if (parent(p_currentIndex) != -1) {
            return sibling(parent(p_currentIndex));
        } else {
            return -1;
        }
    }

    private long previous(long p_index) {
        long p = p_index;
        if (left(p) != -1) {
            p = left(p);
            while (right(p) != -1) {
                p = right(p);
            }
            return p;
        } else {
            if (parent(p) != -1) {
                if (p == right(parent(p))) {
                    return parent(p);
                } else {
                    while (parent(p) != -1 && p == left(parent(p))) {
                        p = parent(p);
                    }
                    return parent(p);
                }
            } else {
                return -1;
            }
        }
    }

    private void rotateLeft(long n) {
        long r = right(n);
        replaceNode(n, r);
        setRight(n, left(r));
        if (left(r) != -1) {
            setParent(left(r), n);
        }
        setLeft(r, n);
        setParent(n, r);
    }

    private void rotateRight(long n) {
        long l = left(n);
        replaceNode(n, l);
        setLeft(n, right(l));
        if (right(l) != -1) {
            setParent(right(l), n);
        }
        setRight(l, n);
        setParent(n, l);
    }

    private void replaceNode(long oldn, long newn) {
        if (parent(oldn) == -1) {
            OffHeapLongArray.set(addr, INDEX_ROOT_ELEM, newn);
        } else {
            if (oldn == left(parent(oldn))) {
                setLeft(parent(oldn), newn);
            } else {
                setRight(parent(oldn), newn);
            }
        }
        if (newn != -1) {
            setParent(newn, parent(oldn));
        }
    }

    private void insertCase1(long n) {
        if (parent(n) == -1) {
            setColor(n, true);
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(long n) {
        if (!color(parent(n))) {
            insertCase3(n);
        }
    }

    private void insertCase3(long n) {
        if (!color(uncle(n))) {
            setColor(parent(n), true);
            setColor(uncle(n), true);
            setColor(grandParent(n), false);
            insertCase1(grandParent(n));
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(long n_n) {
        long n = n_n;
        if (n == right(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateLeft(parent(n));
            n = left(n);
        } else {
            if (n == left(parent(n)) && parent(n) == right(grandParent(n))) {
                rotateRight(parent(n));
                n = right(n);
            }
        }
        insertCase5(n);
    }

    private void insertCase5(long n) {
        setColor(parent(n), true);
        setColor(grandParent(n), false);
        if (n == left(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateRight(grandParent(n));
        } else {
            rotateLeft(grandParent(n));
        }
    }

    private boolean inLoad = false;

    private void load(final Buffer buffer) {

        long capacity = Constants.MAP_INITIAL_CAPACITY;

        //init k array
        kPtr = OffHeapLongArray.allocate(Constants.MAP_INITIAL_CAPACITY);
        OffHeapLongArray.set(addr, INDEX_K, kPtr);
        //init meta array
        metaPtr = OffHeapLongArray.allocate(Constants.MAP_INITIAL_CAPACITY * META_SIZE);
        OffHeapLongArray.set(addr, INDEX_META, metaPtr);
        //init colors array
        colorsPtr = OffHeapByteArray.allocate(Constants.MAP_INITIAL_CAPACITY);
        OffHeapLongArray.set(addr, INDEX_COLORS, colorsPtr);

        OffHeapLongArray.set(addr, INDEX_LOCK, 0);
        OffHeapLongArray.set(addr, INDEX_FLAGS, 0);
        OffHeapLongArray.set(addr, INDEX_SIZE, 0);
        OffHeapLongArray.set(addr, INDEX_ROOT_ELEM, -1);
        OffHeapLongArray.set(addr, INDEX_THRESHOLD, (long) (capacity * Constants.MAP_LOAD_FACTOR));
        OffHeapLongArray.set(addr, INDEX_MAGIC, 0);

        inLoad = true;

        long cursor = 0;
        long previous = 0;
        long payloadSize = buffer.size();
        while (cursor < payloadSize) {
            byte current = buffer.read(cursor);
            if (current == Constants.CHUNK_SUB_SEP) {
                internal_insert(Base64.decodeToLongWithBounds(buffer, previous, cursor));
                previous = cursor + 1;
            }
            cursor++;
        }
        internal_insert(Base64.decodeToLongWithBounds(buffer, previous, cursor));
        inLoad = false;
    }

}
