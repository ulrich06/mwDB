package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.chunk.KBuffer;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KTimeTreeChunk;
import org.mwdb.chunk.KTreeWalker;
import org.mwdb.utility.Base64;
import org.mwdb.utility.Unsafe;

public class HeapTimeTreeChunk implements KTimeTreeChunk, KHeapChunk {

    private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

    //constants definition
    private static final byte BLACK_LEFT = '{';
    private static final byte BLACK_RIGHT = '}';
    private static final byte RED_LEFT = '[';
    private static final byte RED_RIGHT = ']';
    private static final int META_SIZE = 3;

    private final long _world;
    private final long _time;
    private final long _id;

    private final KChunkListener _listener;

    private volatile int _threshold;
    private volatile int _root_index = -1;
    private volatile int _size = 0;

    private volatile int[] _back_meta;
    private volatile long[] _back_kv;
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

    public HeapTimeTreeChunk(final long p_world, final long p_time, final long p_obj, final KChunkListener p_listener, final KBuffer initialPayload) {
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
        load(initialPayload);
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
    public synchronized final void range(long startKey, long endKey, long maxElements, KTreeWalker walker) {
        //lock and load from main memory
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
    public synchronized final void save(KBuffer buffer) {
        //lock and load from main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;

        if (_root_index == -1) {
            buffer.write((byte) '0');
        }
        Base64.encodeLongToBuffer((long) _size, buffer);
        buffer.write(Constants.CHUNK_SUB_SEP);
        Base64.encodeLongToBuffer((long) _root_index, buffer);
        for (int i = 0; i < _back_meta.length / META_SIZE; i++) {
            int parentIndex = _back_meta[(i * META_SIZE) + 2];
            if (parentIndex != -1 || i == _root_index) {
                boolean isOnLeft = false;
                if (parentIndex != -1) {
                    isOnLeft = _back_meta[parentIndex * META_SIZE] == i;
                }
                if (!color(i)) {
                    if (isOnLeft) {
                        buffer.write(BLACK_LEFT);
                    } else {
                        buffer.write(BLACK_RIGHT);
                    }
                } else {//red
                    if (isOnLeft) {
                        buffer.write(RED_LEFT);
                    } else {
                        buffer.write(RED_RIGHT);
                    }
                }
                Base64.encodeLongToBuffer(_back_kv[i], buffer);
                buffer.write(Constants.CHUNK_SUB_SEP);
                if (parentIndex != -1) {
                    Base64.encodeIntToBuffer(parentIndex, buffer);
                }
            }
        }
        //free the lock
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    @Override
    public synchronized final long previousOrEqual(long key) {
        //lock and load from main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;

        int result = internal_previousOrEqual_index(key);
        long resultKey;
        if (result != -1) {
            resultKey = key(result);
        } else {
            resultKey = Constants.NULL_LONG;
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
        //lock and load from main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;
        internal_insert(p_key);
        //free the lock and write to main memory
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    @Override
    public final byte chunkType() {
        return Constants.TIME_TREE_CHUNK;
    }

    @Override
    public synchronized final void clearAt(long max) {
        //lock and load from main memory
        while (!unsafe.compareAndSwapInt(this, _lockOffset, 0, 1)) ;

        long[] previousValue = _back_kv;
        //reset the state
        _back_kv = new long[_back_kv.length];
        _back_meta = new int[_back_kv.length * META_SIZE];
        _back_colors = new boolean[_back_kv.length];
        _root_index = -1;
        int _previousSize = _size;
        _size = 0;

        for (int i = 0; i < _previousSize; i++) {
            if (previousValue[i] != Constants.NULL_LONG && previousValue[i] < max) {
                internal_insert(previousValue[i]);
            }
        }
        //dirty
        internal_set_dirty();

        //free the lock and write to main memory
        if (!unsafe.compareAndSwapInt(this, _lockOffset, 1, 0)) {
            throw new RuntimeException("CAS Error !!!");
        }
    }

    private void allocate(int capacity) {
        _back_meta = new int[capacity * META_SIZE];
        _back_kv = new long[capacity];
        _back_colors = new boolean[capacity];
        _threshold = (int) (capacity * Constants.MAP_LOAD_FACTOR);
    }

    private void reallocate(int newCapacity) {
        _threshold = (int) (newCapacity * Constants.MAP_LOAD_FACTOR);
        long[] new_back_kv = new long[newCapacity];
        if (_back_kv != null) {
            System.arraycopy(_back_kv, 0, new_back_kv, 0, _size);
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
        _back_kv = new_back_kv;
        _back_colors = new_back_colors;
    }

    private long key(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_kv[p_currentIndex];
    }

    private void setKey(int p_currentIndex, long p_paramIndex) {
        _back_kv[p_currentIndex] = p_paramIndex;
    }

    protected final long value(int p_currentIndex) {
        if (p_currentIndex == -1) {
            return -1;
        }
        return _back_kv[(p_currentIndex) + 1];
    }

    private void setValue(int p_currentIndex, long p_paramIndex) {
        _back_kv[(p_currentIndex) + 1] = p_paramIndex;
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
            return Constants.NULL_LONG;
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

    private void load(KBuffer buffer) {
        if (buffer == null || buffer.size() == 0) {
            return;
        }
        int initPos = 0;
        int cursor = 0;
        while (cursor < buffer.size() && buffer.read(cursor) != ',' && buffer.read(cursor) != BLACK_LEFT && buffer.read(cursor) != BLACK_RIGHT && buffer.read(cursor) != RED_LEFT && buffer.read(cursor) != RED_RIGHT) {
            cursor++;
        }
        if (buffer.read(cursor) == ',') {//className to parse
            _size = Base64.decodeToIntWithBounds(buffer, initPos, cursor);
            cursor++;
            initPos = cursor;
        }
        while (cursor < buffer.size() && buffer.read(cursor) != BLACK_LEFT && buffer.read(cursor) != BLACK_RIGHT && buffer.read(cursor) != RED_LEFT && buffer.read(cursor) != RED_RIGHT) {
            cursor++;
        }
        _root_index = Base64.decodeToIntWithBounds(buffer, initPos, cursor);
        allocate(_size);
        for (int i = 0; i < _size; i++) {
            int offsetI = i * META_SIZE;
            _back_meta[offsetI] = -1;
            _back_meta[offsetI + 1] = -1;
            _back_meta[offsetI + 2] = -1;
        }
        int currentLoopIndex = 0;
        while (cursor < buffer.size()) {
            while (cursor < buffer.size() && buffer.read(cursor) != BLACK_LEFT && buffer.read(cursor) != BLACK_RIGHT && buffer.read(cursor) != RED_LEFT && buffer.read(cursor) != RED_RIGHT) {
                cursor++;
            }
            if (cursor < buffer.size()) {
                byte elem = buffer.read(cursor);
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
                while (cursor < buffer.size() && buffer.read(cursor) != ',') {
                    cursor++;
                }
                long loopKey = Base64.decodeToLongWithBounds(buffer, beginChunk, cursor);
                setKey(currentLoopIndex, loopKey);
                cursor++;
                beginChunk = cursor;
                while (cursor < buffer.size() && buffer.read(cursor) != ',' && buffer.read(cursor) != BLACK_LEFT && buffer.read(cursor) != BLACK_RIGHT && buffer.read(cursor) != RED_LEFT && buffer.read(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    int parentRaw = Base64.decodeToIntWithBounds(buffer, beginChunk, cursor);
                    setParent(currentLoopIndex, parentRaw);
                    if (isOnLeft) {
                        setLeft(parentRaw, currentLoopIndex);
                    } else {
                        setRight(parentRaw, currentLoopIndex);
                    }
                }
                if (cursor < buffer.size() && buffer.read(cursor) == ',') {
                    cursor++;
                    beginChunk = cursor;
                    while (cursor < buffer.size() && buffer.read(cursor) != BLACK_LEFT && buffer.read(cursor) != BLACK_RIGHT && buffer.read(cursor) != RED_LEFT && buffer.read(cursor) != RED_RIGHT) {
                        cursor++;
                    }
                    if (cursor > beginChunk) {
                        long currentValue = Base64.decodeToLongWithBounds(buffer, beginChunk, cursor);
                        setValue(currentLoopIndex, currentValue);
                    }
                }
                currentLoopIndex++;
            }
        }
    }

    private void internal_insert(long p_key) {
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
                    return;
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
        internal_set_dirty();
    }

    private void internal_set_dirty() {
        long magicBefore;
        long magicAfter;
        do {
            magicBefore = _magic;
            magicAfter = magicBefore + 1;
        } while (!unsafe.compareAndSwapLong(this, _magicOffset, magicBefore, magicAfter));
        if (_listener != null) {
            if ((_flags & Constants.DIRTY_BIT) != Constants.DIRTY_BIT) {
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
                // Copy domainKey/value from predecessor and done delete it instead
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
