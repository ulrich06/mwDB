package org.mwg.core.chunk.heap;

import org.mwg.core.CoreConstants;
import org.mwg.struct.Buffer;
import org.mwg.core.chunk.ChunkListener;
import org.mwg.core.chunk.TimeTreeChunk;
import org.mwg.core.chunk.TreeWalker;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.Unsafe;

public class HeapTimeTreeChunk implements TimeTreeChunk, HeapChunk {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    //constants definition
    private static final int META_SIZE = 3;

    private final long _world;
    private final long _time;
    private final long _id;

    private final ChunkListener _listener;

    private volatile int _threshold;
    private volatile int _root_index = -1;
    private volatile int _size = 0;

    private volatile int[] _back_meta;
    private volatile long[] _back_k;
    private volatile boolean[] _back_colors;

    private volatile int _lock;
    private volatile long _flags;
    private volatile long _marks;
    private volatile long _magic;

    private static final long _lockOffset;
    private static final long _flagsOffset;
    private static final long _marksOffset;
    private static final long _magicOffset;

    static {
        try {
            _lockOffset = unsafe.objectFieldOffset(HeapTimeTreeChunk.class.getDeclaredField("_lock"));
            _flagsOffset = unsafe.objectFieldOffset(HeapTimeTreeChunk.class.getDeclaredField("_flags"));
            _marksOffset = unsafe.objectFieldOffset(HeapTimeTreeChunk.class.getDeclaredField("_marks"));
            _magicOffset = unsafe.objectFieldOffset(HeapTimeTreeChunk.class.getDeclaredField("_magic"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public HeapTimeTreeChunk(final long p_world, final long p_time, final long p_obj, final ChunkListener p_listener, final Buffer initialPayload) {
        //listener
        this._listener = p_listener;
        //identifier
        this._world = p_world;
        this._time = p_time;
        this._id = p_obj;
        //multi-thread management
        this._threshold = 0;
        this._flags = 0;
        this._marks = 0;
        this._magic = 0;
        this._lock = 0;
        try {
            load(initialPayload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public final long marks() {
        return this._marks;
    }

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
    public final long flags() {
        return _flags;
    }

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

    @Override
    public final long size() {
        return _size;
    }

    @Override
    public synchronized final void range(long startKey, long endKey, long maxElements, TreeWalker walker) {
        //lock and load fromVar main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;

        int nbElements = 0;
        int indexEnd = internal_previousOrEqual_index(endKey);
        while (indexEnd != -1 && key(indexEnd) >= startKey && nbElements < maxElements) {
            walker.elem(key(indexEnd));
            nbElements++;
            indexEnd = previous(indexEnd);
        }

        //free the lock
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    @Override
    public synchronized final void save(Buffer buffer) {
        //lock and load fromVar main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1));
        try {
            if (_root_index == -1) {
                return;
            }
            boolean isFirst = true;
            for (int i = 0; i < _size; i++) {
                if (!isFirst) {
                    buffer.write(CoreConstants.CHUNK_SUB_SEP);
                } else {
                    isFirst = false;
                }
                Base64.encodeLongToBuffer(this._back_k[i], buffer);
            }
        } finally {
            //free the lock
            if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
                throw new RuntimeException("CAS Error !!!");
            }
        }
    }


    private void load(final Buffer buffer) {
        if (buffer == null || buffer.size() == 0) {
            return;
        }
        _size = 0;
        long cursor = 0;
        long previous = 0;
        long payloadSize = buffer.size();
        while (cursor < payloadSize) {
            byte current = buffer.read(cursor);
            if (current == CoreConstants.CHUNK_SUB_SEP) {
                internal_insert(Base64.decodeToLongWithBounds(buffer, previous, cursor));
                previous = cursor + 1;
            }
            cursor++;
        }
        internal_insert(Base64.decodeToLongWithBounds(buffer, previous, cursor));
    }

    @Override
    public synchronized final long previousOrEqual(long key) {
        //lock and load fromVar main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;

        int result = internal_previousOrEqual_index(key);
        long resultKey;
        if (result != -1) {
            resultKey = key(result);
        } else {
            resultKey = CoreConstants.NULL_LONG;
        }
        //free the lock
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
        return resultKey;
    }

    @Override
    public final long magic() {
        return this._magic;
    }

    @Override
    public synchronized final void insert(long p_key) {

        boolean toSetDirty;
        //lock and load fromVar main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;
        toSetDirty = internal_insert(p_key);
        //free the lock and write to main memory
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
        if (toSetDirty) {
            internal_set_dirty();
        }

    }

    @Override
    public synchronized final void unsafe_insert(long p_key) {
        internal_insert(p_key);
    }

    @Override
    public final byte chunkType() {
        return CoreConstants.TIME_TREE_CHUNK;
    }

    @Override
    public synchronized final void clearAt(long max) {
        //lock and load fromVar main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;

        long[] previousValue = _back_k;
        //reset the state
        _back_k = new long[_back_k.length];
        _back_meta = new int[_back_k.length * META_SIZE];
        _back_colors = new boolean[_back_k.length];
        _root_index = -1;
        int _previousSize = _size;
        _size = 0;

        for (int i = 0; i < _previousSize; i++) {
            if (previousValue[i] != CoreConstants.NULL_LONG && previousValue[i] < max) {
                internal_insert(previousValue[i]);
            }
        }

        //free the lock and write to main memory
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
        //dirty
        internal_set_dirty();
    }

    private void allocate(int capacity) {
        _back_meta = new int[capacity * META_SIZE];
        _back_k = new long[capacity];
        _back_colors = new boolean[capacity];
        _threshold = (int) (capacity * CoreConstants.MAP_LOAD_FACTOR);
    }

    private void reallocate(int newCapacity) {
        _threshold = (int) (newCapacity * CoreConstants.MAP_LOAD_FACTOR);
        long[] new_back_kv = new long[newCapacity];
        if (_back_k != null) {
            System.arraycopy(_back_k, 0, new_back_kv, 0, _size);
        }
        boolean[] new_back_colors = new boolean[newCapacity];
        if (_back_colors != null) {
            System.arraycopy(_back_colors, 0, new_back_colors, 0, _size);
            for (int i = _size; i < newCapacity; i++) {
                new_back_colors[i] = false;
            }
        }
        int[] new_back_meta = new int[newCapacity * META_SIZE];
        if (_back_meta != null) {
            System.arraycopy(_back_meta, 0, new_back_meta, 0, _size * META_SIZE);
            for (int i = _size * META_SIZE; i < newCapacity * META_SIZE; i++) {
                new_back_meta[i] = -1;
            }
        }
        _back_meta = new_back_meta;
        _back_k = new_back_kv;
        _back_colors = new_back_colors;
    }

    private long key(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_k[p_currentIndex];
    }

    private void setKey(int p_currentIndex, long p_paramIndex) {
        _back_k[p_currentIndex] = p_paramIndex;
    }

    protected final long value(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_k[(p_currentIndex) + 1];
    }

    private void setValue(int p_currentIndex, long p_paramIndex) {
        _back_k[(p_currentIndex) + 1] = p_paramIndex;
    }

    private int left(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_meta[p_currentIndex * META_SIZE];
    }

    private void setLeft(int p_currentIndex, int p_paramIndex) {
        _back_meta[p_currentIndex * META_SIZE] = p_paramIndex;
    }

    private int right(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_meta[(p_currentIndex * META_SIZE) + 1];
    }

    private void setRight(int p_currentIndex, int p_paramIndex) {
        _back_meta[(p_currentIndex * META_SIZE) + 1] = p_paramIndex;
    }

    private int parent(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_meta[(p_currentIndex * META_SIZE) + 2];
    }

    private void setParent(int p_currentIndex, int p_paramIndex) {
        _back_meta[(p_currentIndex * META_SIZE) + 2] = p_paramIndex;
    }

    private boolean color(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return true;
        }
        return _back_colors[p_currentIndex];
    }

    private void setColor(int p_currentIndex, boolean p_paramIndex) {
        _back_colors[p_currentIndex] = p_paramIndex;
    }

    private int grandParent(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        if (parent(p_currentIndex) != -1) {
            return parent(parent(p_currentIndex));
        } else {
            return -1;
        }
    }

    private int sibling(int p_currentIndex) {
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

    private int uncle(int p_currentIndex) {
        if (parent(p_currentIndex) != -1) {
            return sibling(parent(p_currentIndex));
        } else {
            return -1;
        }
    }

    private int previous(int p_index) {
        int p = p_index;
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

    /*
    private int next(int p_index) {
        int p = p_index;
        if (right(p) != -1) {
            p = right(p);
            while (left(p) != -1) {
                p = left(p);
            }
            return p;
        } else {
            if (parent(p) != -1) {
                if (p == left(parent(p))) {
                    return parent(p);
                } else {
                    while (parent(p) != -1 && p == right(parent(p))) {
                        p = parent(p);
                    }
                    return parent(p);
                }

            } else {
                return -1;
            }
        }
    }

    private long lookup(long p_key) {
        int n = _root_index;
        if (n == -1) {
            return CoreConstants.NULL_LONG;
        }
        while (n != -1) {
            if (p_key == key(n)) {
                return key(n);
            } else {
                if (p_key < key(n)) {
                    n = left(n);
                } else {
                    n = right(n);
                }
            }
        }
        return n;
    }
    */


    private int internal_previousOrEqual_index(long p_key) {
        int p = _root_index;
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
                    int parent = parent(p);
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

    private void rotateLeft(int n) {
        int r = right(n);
        replaceNode(n, r);
        setRight(n, left(r));
        if (left(r) != -1) {
            setParent(left(r), n);
        }
        setLeft(r, n);
        setParent(n, r);
    }

    private void rotateRight(int n) {
        int l = left(n);
        replaceNode(n, l);
        setLeft(n, right(l));
        if (right(l) != -1) {
            setParent(right(l), n);
        }
        setRight(l, n);
        setParent(n, l);
    }

    private void replaceNode(int oldn, int newn) {
        if (parent(oldn) == -1) {
            _root_index = newn;
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

    private void insertCase1(int n) {
        if (parent(n) == -1) {
            setColor(n, true);
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(int n) {
        if (!color(parent(n))) {
            insertCase3(n);
        }
    }

    private void insertCase3(int n) {
        if (!color(uncle(n))) {
            setColor(parent(n), true);
            setColor(uncle(n), true);
            setColor(grandParent(n), false);
            insertCase1(grandParent(n));
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(int n_n) {
        int n = n_n;
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

    private void insertCase5(int n) {
        setColor(parent(n), true);
        setColor(grandParent(n), false);
        if (n == left(parent(n)) && parent(n) == left(grandParent(n))) {
            rotateRight(grandParent(n));
        } else {
            rotateLeft(grandParent(n));
        }
    }

    private boolean internal_insert(long p_key) {
        if ((_size + 1) > _threshold) {
            int length = (_size == 0 ? 1 : _size << 1);
            reallocate(length);
        }
        int newIndex = _size;
        if (newIndex == 0) {
            setKey(newIndex, p_key);
            setColor(newIndex, false);
            setLeft(newIndex, -1);
            setRight(newIndex, -1);
            setParent(newIndex, -1);
            _root_index = newIndex;
            _size = 1;
        } else {
            int n = _root_index;
            while (true) {
                if (p_key == key(n)) {
                    //nop _size
                    return false;
                } else if (p_key < key(n)) {
                    if (left(n) == -1) {
                        setKey(newIndex, p_key);
                        setColor(newIndex, false);
                        setLeft(newIndex, -1);
                        setRight(newIndex, -1);
                        setParent(newIndex, -1);
                        setLeft(n, newIndex);
                        _size++;
                        break;
                    } else {
                        n = left(n);
                    }
                } else {
                    if (right(n) == -1) {
                        setKey(newIndex, p_key);
                        setColor(newIndex, false);
                        setLeft(newIndex, -1);
                        setRight(newIndex, -1);
                        setParent(newIndex, -1);
                        setRight(n, newIndex);
                        _size++;
                        break;
                    } else {
                        n = right(n);
                    }
                }
            }
            setParent(newIndex, n);
        }
        insertCase1(newIndex);
        //internal_set_dirty();
        return true;
    }

    private void internal_set_dirty() {
        long magicBefore;
        long magicAfter;
        do {
            magicBefore = _magic;
            magicAfter = magicBefore + 1;
        } while (!unsafe.compareAndSwapLong(this, _magicOffset, magicBefore, magicAfter));
        if (_listener != null) {
            if ((_flags & CoreConstants.DIRTY_BIT) != CoreConstants.DIRTY_BIT) {
                _listener.declareDirty(this);
            }
        }
    }

     /*
    public void delete(long key) {
        TreeNode n = lookup(key);
        if (n == null) {
            return;
        } else {
            _size--;
            if (n.getLeft() != null && n.getRight() != null) {
                // Copy domainKey/value fromVar predecessor and done delete it instead
                TreeNode pred = n.getLeft();
                while (pred.getRight() != null) {
                    pred = pred.getRight();
                }
                n.key = pred.key;
                n = pred;
            }
            TreeNode child;
            if (n.getRight() == null) {
                child = n.getLeft();
            } else {
                child = n.getRight();
            }
            if (nodeColor(n) == true) {
                n.color = nodeColor(child);
                deleteCase1(n);
            }
            replaceNode(n, child);
        }
    }

    private void deleteCase1(TreeNode n) {
        if (n.getParent() == null) {
            return;
        } else {
            deleteCase2(n);
        }
    }

    private void deleteCase2(TreeNode n) {
        if (nodeColor(n.sibling()) == false) {
            n.getParent().color = false;
            n.sibling().color = true;
            if (n == n.getParent().getLeft()) {
                rotateLeft(n.getParent());
            } else {
                rotateRight(n.getParent());
            }
        }
        deleteCase3(n);
    }

    private void deleteCase3(TreeNode n) {
        if (nodeColor(n.getParent()) == true && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == true && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            deleteCase1(n.getParent());
        } else {
            deleteCase4(n);
        }
    }

    private void deleteCase4(TreeNode n) {
        if (nodeColor(n.getParent()) == false && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == true && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            n.getParent().color = true;
        } else {
            deleteCase5(n);
        }
    }

    private void deleteCase5(TreeNode n) {
        if (n == n.getParent().getLeft() && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getLeft()) == false && nodeColor(n.sibling().getRight()) == true) {
            n.sibling().color = false;
            n.sibling().getLeft().color = true;
            rotateRight(n.sibling());
        } else if (n == n.getParent().getRight() && nodeColor(n.sibling()) == true && nodeColor(n.sibling().getRight()) == false && nodeColor(n.sibling().getLeft()) == true) {
            n.sibling().color = false;
            n.sibling().getRight().color = true;
            rotateLeft(n.sibling());
        }
        deleteCase6(n);
    }

    private void deleteCase6(TreeNode n) {
        n.sibling().color = nodeColor(n.getParent());
        n.getParent().color = true;
        if (n == n.getParent().getLeft()) {
            n.sibling().getRight().color = true;
            rotateLeft(n.getParent());
        } else {
            n.sibling().getLeft().color = true;
            rotateRight(n.getParent());
        }
    }*/

}
