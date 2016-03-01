package org.mwdb.chunk.heap;

import org.mwdb.Constants;
import org.mwdb.chunk.KChunkListener;
import org.mwdb.chunk.KLongTree;

public class ArrayLongTree extends AbstractArrayTree implements KLongTree {

    public ArrayLongTree(long p_world, long p_time, long p_id, KChunkListener p_listener) {
        super(p_world, p_time, p_id, p_listener);
    }

    public long previousOrEqual(long key) {
        int result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return Constants.NULL_LONG;
        }
    }

    @Override
    public long magic() {
        return this._magic;
    }

    public void insertKey(long p_key) {
        internal_insert(p_key, p_key);
    }

    @Override
    public short chunkType() {
        return Constants.LONG_TREE;
    }

}
