package org.mwdb.chunk.offheap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KTimeTreeChunk;
import org.mwdb.chunk.KTreeWalker;
import org.mwdb.utility.Base64;
import org.mwdb.utility.PrimitiveHelper;

public class OffHeapTimeTreeChunk implements KTimeTreeChunk, KOffHeapChunk {

    //constants definition
    private static final char BLACK_LEFT = '{';
    private static final char BLACK_RIGHT = '}';
    private static final char RED_LEFT = '[';
    private static final char RED_RIGHT = ']';
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

    private long addr;

    private long metaPtr;
    private long kPtr;
    private long colorsPtr;

    private final KChunkListener _listener;

    public OffHeapTimeTreeChunk(KChunkListener p_listener, long previousAddr, String initialPayload) {
        //listener
        this._listener = p_listener;
        //init
        if (previousAddr == Constants.OFFHEAP_NULL_PTR) {
            addr = OffHeapLongArray.allocate(14);
        }
        if (initialPayload != null) {
            load(initialPayload);
        } else {
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
            OffHeapLongArray.set(addr, INDEX_MAGIC, PrimitiveHelper.rand());

        }
    }


    @Override
    public void free() {
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_K));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_META));
        OffHeapLongArray.free(OffHeapLongArray.get(addr, INDEX_COLORS));
        OffHeapLongArray.free(addr);
    }


    @Override
    public long magic() {
        return OffHeapLongArray.get(addr, INDEX_MAGIC);
    }

    @Override
    public byte chunkType() {
        return Constants.TIME_TREE_CHUNK;
    }

    @Override
    public long addr() {
        return this.addr;
    }

    @Override
    public final long marks() {
        return OffHeapLongArray.get(addr, INDEX_MARKS);
    }

    @Override
    public long world() {
        return OffHeapLongArray.get(addr, INDEX_WORLD);
    }

    @Override
    public long time() {
        return OffHeapLongArray.get(addr, INDEX_TIME);
    }

    @Override
    public long id() {
        return OffHeapLongArray.get(addr, INDEX_ID);
    }

    @Override
    public long flags() {
        return OffHeapLongArray.get(addr, INDEX_FLAGS);
    }

    @Override
    public long size() {
        return OffHeapLongArray.get(addr, INDEX_SIZE);
    }

    @Override
    public final void range(long startKey, long endKey, KTreeWalker walker) {
        long indexEnd = internal_previousOrEqual_index(endKey);
        while (indexEnd != -1 && key(indexEnd) >= startKey) {
            walker.elem(key(indexEnd));
            indexEnd = previous(indexEnd);
        }
    }

    @Override
    public long previousOrEqual(long key) {
        long result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return Constants.NULL_LONG;
        }
    }

    @Override
    public final String save() {
        //OffHeap lock
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        ptrConsistency();

        long rootElem = OffHeapLongArray.get(addr, INDEX_ROOT_ELEM);
        if (rootElem == Constants.OFFHEAP_NULL_PTR) {
            //Free OffHeap lock
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
            return "0";
        }
        StringBuilder buffer = new StringBuilder();
        long treeSize = OffHeapLongArray.get(addr, INDEX_SIZE);
        Base64.encodeLongToBuffer(OffHeapLongArray.get(addr, INDEX_SIZE), buffer);
        buffer.append(",");
        Base64.encodeLongToBuffer(rootElem, buffer);

        for (long i = 0; i < treeSize; i++) {
            long parentIndex = OffHeapLongArray.get(metaPtr, (i * META_SIZE) + 2);
            if (parentIndex != -1 || i == rootElem) {
                boolean isOnLeft = false;
                if (parentIndex != -1) {
                    isOnLeft = OffHeapLongArray.get(metaPtr, parentIndex * META_SIZE) == i;
                }
                if (!color(i)) {
                    if (isOnLeft) {
                        buffer.append(BLACK_LEFT);
                    } else {
                        buffer.append(BLACK_RIGHT);
                    }
                } else {//red
                    if (isOnLeft) {
                        buffer.append(RED_LEFT);
                    } else {
                        buffer.append(RED_RIGHT);
                    }
                }
                Base64.encodeLongToBuffer(OffHeapLongArray.get(kPtr, i), buffer);
                buffer.append(',');
                if (parentIndex != -1) {
                    Base64.encodeLongToBuffer(parentIndex, buffer);
                }
            }
        }

        //Free OffHeap lock
        if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
        return buffer.toString();
    }

    private void ptrConsistency() {
        if (kPtr != OffHeapLongArray.get(addr, INDEX_K)) {
            kPtr = OffHeapLongArray.get(addr, INDEX_K);
            metaPtr = OffHeapLongArray.get(addr, INDEX_META);
            colorsPtr = OffHeapLongArray.get(addr, INDEX_COLORS);
        }
    }

    @Override
    public void insert(long p_key) {

        //OffHeap lock
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        ptrConsistency();

        long size = OffHeapLongArray.get(addr, INDEX_SIZE);
        if ((size + 1) > OffHeapLongArray.get(addr, INDEX_THRESHOLD)) {

            long newLength = (size == 0 ? 1 : size << 1);

            kPtr = OffHeapLongArray.reallocate(kPtr, size, newLength);
            metaPtr = OffHeapLongArray.reallocate(metaPtr, size, newLength * META_SIZE);
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
                    //Free OffHeap lock
                    if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                        throw new RuntimeException("CAS Error !!!");
                    }
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

        OffHeapLongArray.set(addr, INDEX_MAGIC, PrimitiveHelper.rand());

        //Free OffHeap lock
        if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    private long internal_previousOrEqual_index(long p_key) {

        //OffHeap lock
        //TODO move this lock above
        while (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 0, 1)) ;
        ptrConsistency();

        long p = OffHeapLongArray.get(addr, INDEX_ROOT_ELEM);
        if (p == -1) {
            //Free OffHeap lock
            if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
            return p;
        }

        while (p != -1) {
            if (p_key == key(p)) {
                //Free OffHeap lock
                if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                    throw new RuntimeException("CAS Error !!!");
                }
                return p;
            }
            if (p_key > key(p)) {
                if (right(p) != -1) {
                    p = right(p);
                } else {
                    //Free OffHeap lock
                    if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                        throw new RuntimeException("CAS Error !!!");
                    }
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
                    //Free OffHeap lock
                    if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
                        throw new RuntimeException("CAS Error !!!");
                    }
                    return parent;
                }
            }
        }

        //Free OffHeap lock
        if (!OffHeapLongArray.compareAndSwap(addr, INDEX_LOCK, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
        return -1;
    }

    private void internal_set_dirty() {
        if (_listener != null) {
            if ((OffHeapLongArray.get(addr, INDEX_FLAGS) & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
                _listener.declareDirty(this);
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

    private void load(String payload) {

        if (payload == null || payload.length() == 0) {

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
            OffHeapLongArray.set(addr, INDEX_MAGIC, PrimitiveHelper.rand());

            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }
        long newSize = 0;
        if (payload.charAt(cursor) == ',') {//className to parse
            newSize = Base64.decodeToIntWithBounds(payload, initPos, cursor);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }
        OffHeapLongArray.set(addr, INDEX_ROOT_ELEM, Base64.decodeToLongWithBounds(payload, initPos, cursor));


        long capacity = newSize * 2;
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
        OffHeapLongArray.set(addr, INDEX_SIZE, newSize);
        OffHeapLongArray.set(addr, INDEX_THRESHOLD, (long) (capacity * Constants.MAP_LOAD_FACTOR));
        OffHeapLongArray.set(addr, INDEX_MAGIC, PrimitiveHelper.rand());
        OffHeapLongArray.set(addr, INDEX_FLAGS, 0);


        int currentLoopIndex = 0;
        while (cursor < payload.length()) {
            while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                cursor++;
            }
            if (cursor < payload.length()) {
                char elem = payload.charAt(cursor);
                boolean isOnLeft = false;
                if (elem == BLACK_LEFT || elem == RED_LEFT) {
                    isOnLeft = true;
                }
                if (elem == BLACK_LEFT || elem == BLACK_RIGHT) {
                    setColor(currentLoopIndex, false);
                } else {
                    setColor(currentLoopIndex, true);
                }
                cursor++;
                int beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                    cursor++;
                }
                long loopKey = Base64.decodeToLongWithBounds(payload, beginChunk, cursor);
                setKey(currentLoopIndex, loopKey);
                cursor++;
                beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    long parentRaw = Base64.decodeToLongWithBounds(payload, beginChunk, cursor);
                    setParent(currentLoopIndex, parentRaw);
                    if (isOnLeft) {
                        setLeft(parentRaw, currentLoopIndex);
                    } else {
                        setRight(parentRaw, currentLoopIndex);
                    }
                }
                currentLoopIndex++;
            }
        }
    }

}
