package org.mwdb;

import org.mwdb.plugin.KResolver;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractNode implements KNode {

    private final long _world;

    private final long _time;

    private final long _id;

    private final KGraph _graph;

    protected final KResolver _resolver;

    public final AtomicReference<long[]> _previousResolveds;

    public AbstractNode(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution) {
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._graph = p_graph;
        this._resolver = p_graph.resolver();
        this._previousResolveds = new AtomicReference<long[]>();
        this._previousResolveds.set(currentResolution);
    }

    @Override
    public KGraph graph() {
        return _graph;
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
        KResolver.KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.get(this._resolver.stringToLongKey(attributeName));
        }
        return null;
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        KResolver.KNodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            preciseState.set(this._resolver.stringToLongKey(attributeName), attributeType, attributeValue);
        } else {
            throw new RuntimeException(KConstants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public Object attMap(String attributeName, byte attributeType) {
        KResolver.KNodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            return preciseState.getOrCreate(this._resolver.stringToLongKey(attributeName), attributeType);
        } else {
            throw new RuntimeException(KConstants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public byte attType(String attributeName) {
        KResolver.KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.getType(this._resolver.stringToLongKey(attributeName));
        }
        return -1;
    }

    @Override
    public void attRemove(String attributeName) {
        attSet(attributeName, KType.INT, null);
    }

    @Override
    public <A extends KNode> void rel(String relationName, KCallback<A[]> callback) {
        if (callback == null) {
            return;
        }
        final KResolver.KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            final long[] flatRefs = (long[]) resolved.get(this._resolver.stringToLongKey(relationName));
            if (flatRefs == null || flatRefs.length == 0) {
                callback.on((A[]) new KNode[0]);
            } else {
                final A[] result = (A[]) new KNode[flatRefs.length];
                final KDeferCounter counter = _graph.counter(flatRefs.length);
                for (int i = 0; i < flatRefs.length; i++) {
                    final int fi = i;
                    this._resolver.lookup(_world, _time, flatRefs[i], new KCallback<KNode>() {
                        @Override
                        public void on(KNode kNode) {
                            result[fi] = (A) kNode;
                            counter.count();
                        }
                    });
                }
                counter.then(new KCallback() {
                    @Override
                    public void on(Object o) {
                        callback.on(result);
                    }
                });
            }
        }
    }

    @Override
    public long[] relValues(String relationName) {
        KResolver.KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return (long[]) resolved.get(this._resolver.stringToLongKey(relationName));
        } else {
            throw new RuntimeException(KConstants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void relAdd(String relationName, KNode relatedNode) {
        KResolver.KNodeState preciseState = this._resolver.resolveState(this, false);
        long relationKey = this._resolver.stringToLongKey(relationName);
        if (preciseState != null) {
            long[] previous = (long[]) preciseState.get(relationKey);
            if (previous == null) {
                previous = new long[1];
                previous[0] = relatedNode.id();
            } else {
                long[] incArray = new long[previous.length + 1];
                System.arraycopy(previous, 0, incArray, 0, previous.length);
                incArray[previous.length] = relatedNode.id();
                previous = incArray;
            }
            preciseState.set(relationKey, KType.LONG_ARRAY, previous);
        } else {
            throw new RuntimeException(KConstants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void relRemove(String relationName, KNode relatedNode) {
        KResolver.KNodeState preciseState = this._resolver.resolveState(this, false);
        long relationKey = this._resolver.stringToLongKey(relationName);
        if (preciseState != null) {
            long[] previous = (long[]) preciseState.get(relationKey);
            if (previous != null) {
                int indexToRemove = -1;
                for (int i = 0; i < previous.length; i++) {
                    if (previous[i] == relatedNode.id()) {
                        indexToRemove = i;
                        break;
                    }
                }
                if (indexToRemove != -1) {
                    if ((previous.length - 1) == 0) {
                        preciseState.set(relationKey, KType.LONG_ARRAY, null);
                    } else {
                        long[] newArray = new long[previous.length - 1];
                        System.arraycopy(previous, 0, newArray, 0, indexToRemove);
                        System.arraycopy(previous, indexToRemove + 1, newArray, indexToRemove, previous.length - indexToRemove - 1);
                        preciseState.set(relationKey, KType.LONG_ARRAY, newArray);
                    }
                }
            }
        } else {
            throw new RuntimeException(KConstants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void free() {
        this._resolver.freeNode(this);
    }

    @Override
    public long timeDephasing() {
        KResolver.KNodeState state = this._resolver.resolveState(this, true);
        if (state != null) {
            return (this._time - state.time());
        } else {
            throw new RuntimeException(KConstants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void forcePhase() {
        this._resolver.resolveState(this, false);
    }

    @Override
    public void timepoints(long beginningOfSearch, long endOfSearch, KCallback<long[]> callback) {
        this._resolver.resolveTimepoints(this, beginningOfSearch, endOfSearch, callback);
    }

    /*
    @Override
    <A extends KNode> public void jump(long targetTime, KCallback<A> timedNode) {
        _resolver.lookup(_world, targetTime, _id, timedNode);
    }*/

    @Override
    public <A extends KNode> void jump(long targetTime, KCallback<A> callback) {
        _resolver.lookup(_world, targetTime, _id, callback);
    }

    @Override
    public <A extends KNode> void find(String indexName, String query, KCallback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public <A extends KNode> void find(String indexName, long world, long time, String query, KCallback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public <A extends KNode> void all(String indexName, long world, long time, KCallback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public <A extends KNode> void all(String indexName, KCallback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

}
