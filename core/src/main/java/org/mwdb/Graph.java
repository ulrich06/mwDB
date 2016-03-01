package org.mwdb;

public class Graph implements KGraph {

    @Override
    public KNode createNode(long world, long time) {
        return null;
    }

    @Override
    public void lookup(long world, long time, long id, KCallback<KNode> callback) {

    }

    @Override
    public void lookupAllTimes(long world, long[] times, long id, KCallback<KNode[]> callback) {

    }

    @Override
    public void save(KCallback callback) {

    }

    @Override
    public void connect(KCallback callback) {

    }

    @Override
    public void disconnect(KCallback callback) {

    }
    
}
