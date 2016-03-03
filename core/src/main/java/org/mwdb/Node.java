package org.mwdb;

import org.mwdb.plugin.KResolver;

import java.util.concurrent.atomic.AtomicReference;

public class Node implements KNode {

    private final long _world;

    private final long _time;

    private final long _id;

    private final KResolver _resolver;

    public final AtomicReference<long[]> _previousResolveds;

    public Node(long p_world, long p_time, long p_id, KResolver p_resolver, long p_actualUniverse, long p_actualTime, long currentUniverseMagic, long currentTimeMagic) {
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._resolver = p_resolver;
        this._previousResolveds = new AtomicReference<long[]>();
        this._previousResolveds.set(new long[]{p_actualUniverse, p_actualTime, currentUniverseMagic, currentTimeMagic});
    }

    @Override
    public long world() {
        return this._world;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long id() {
        return this._id;
    }

    @Override
    public Object att(String attributeName) {
        return null;
    }

    @Override
    public void attSet(String attributeName, Object value) {

    }

    @Override
    public void attRemove(String attributeName, Object value) {

    }

    @Override
    public void ref(String relationName, KCallback<KNode[]> callback) {

    }

    @Override
    public void refValues(String relationName, long[] ids) {

    }

    @Override
    public void refAdd(String relationName, KNode relatedNode) {

    }

    @Override
    public void refRemove(String relationName, KNode relatedNode) {

    }

    @Override
    public KNode[] refSync(String relationName) {
        return new KNode[0];
    }

    @Override
    public void free() {
        this._resolver.freeNode(this);
    }

    @Override
    public long timeDephasing() {
        return 0;
    }

    @Override
    public void undephase() {

    }

    @Override
    public void timepoints(KCallback<long[]> callback) {

    }

    @Override
    public String toJSON() {
        return null;
    }
}
