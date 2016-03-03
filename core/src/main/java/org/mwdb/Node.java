package org.mwdb;

import org.mwdb.plugin.KResolver;

import java.util.concurrent.atomic.AtomicReference;

public class Node implements KNode {

    private final long _world;

    private final long _time;

    private final long _id;

    private final KResolver _resolver;

    private final AtomicReference<long[]> _previousResolveds;

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
        long[] previous;
        do {
            previous = this._previousResolveds.get();
        } while (!_previousResolveds.compareAndSet(previous, null));
        if (previous != null) {
            //this._graph.unmark(previous[Constants.PREVIOUS_RESOLVED_UNIVERSE_INDEX], previous[Constants.PREVIOUS_RESOLVED_TIME_INDEX], _id);//FREE OBJECT CHUNK
            //this._graph.unmark(previous[Constants.PREVIOUS_RESOLVED_UNIVERSE_INDEX], Constants.NULL_LONG, _id);//FREE TIME TREE
            //this._graph.unmark(Constants.NULL_LONG, Constants.NULL_LONG, _id); //FREE OBJECT UNIVERSE MAP
            //this._graph.unmark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG); //FREE GLOBAL UNIVERSE MAP
        }
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
