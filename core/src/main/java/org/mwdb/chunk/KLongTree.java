package org.mwdb.chunk;

public interface KLongTree extends KTree {

    void insertKey(long key);

    long previousOrEqual(long key);

    long lookup(long key);

    void range(long startKey, long endKey, KTreeWalker walker);

    long magic();

}
