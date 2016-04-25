package org.mwg.plugin;

import org.mwg.*;
import org.mwg.struct.Map;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Base implementation to develop NodeFactory plugins without overriding every methods
 */
public abstract class AbstractNode implements Node {

    private final long _world;

    private final long _time;

    private final long _id;

    private final Graph _graph;

    protected final Resolver _resolver;

    public final AtomicReference<long[]> _previousResolveds;

    public AbstractNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        this._world = p_world;
        this._time = p_time;
        this._id = p_id;
        this._graph = p_graph;
        this._resolver = p_graph.resolver();
        this._previousResolveds = new AtomicReference<long[]>();
        this._previousResolveds.set(currentResolution);
    }

    protected NodeState unphasedState() {
        return this._resolver.resolveState(this, true);
    }

    protected NodeState phasedState() {
        return this._resolver.resolveState(this, false);
    }

    protected NodeState newState(long time) {
        return this._resolver.newState(this, _world, time);
    }

    @Override
    public Graph graph() {
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
    public Object get(String propertyName) {
        NodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.get(this._resolver.stringToLongKey(propertyName));
        }
        return null;
    }

    @Override
    public void set(String propertyName, Object propertyValue) {
        if (propertyValue instanceof String) {
            setProperty(propertyName, Type.STRING, propertyValue);
        } else if (propertyValue instanceof Double) {
            setProperty(propertyName, Type.DOUBLE, propertyValue);
        } else if (propertyValue instanceof Long) {
            setProperty(propertyName, Type.LONG, propertyValue);
        } else if (propertyValue instanceof Float) {
            setProperty(propertyName, Type.DOUBLE, (double) ((float) propertyValue));
        } else if (propertyValue instanceof Integer) {
            setProperty(propertyName, Type.INT, propertyValue);
        } else if (propertyValue instanceof Boolean) {
            setProperty(propertyName, Type.BOOL, propertyValue);
        } else if (propertyValue instanceof int[]) {
            setProperty(propertyName, Type.INT_ARRAY, propertyValue);
        } else if (propertyValue instanceof double[]) {
            setProperty(propertyName, Type.DOUBLE_ARRAY, propertyValue);
        } else if (propertyValue instanceof long[]) {
            setProperty(propertyName, Type.LONG_ARRAY, propertyValue);
        } else {
            throw new RuntimeException("Invalid property type: " + propertyValue + ", please use a Type listed in org.mwg.Type");
        }
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        NodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            preciseState.set(this._resolver.stringToLongKey(propertyName), propertyType, propertyValue);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public Map map(String propertyName, byte propertyType) {
        NodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            return (Map) preciseState.getOrCreate(this._resolver.stringToLongKey(propertyName), propertyType);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public byte type(String propertyName) {
        NodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.getType(this._resolver.stringToLongKey(propertyName));
        }
        return -1;
    }

    @Override
    public void remove(String attributeName) {
        setProperty(attributeName, Type.INT, null);
    }

    @Override
    public <A extends Node> void rel(String relationName, Callback<A[]> callback) {
        if (callback == null) {
            return;
        }
        final NodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            final long[] flatRefs = (long[]) resolved.get(this._resolver.stringToLongKey(relationName));
            if (flatRefs == null || flatRefs.length == 0) {
                callback.on((A[]) new Node[0]);
            } else {
                final A[] result = (A[]) new Node[flatRefs.length];
                final DeferCounter counter = _graph.counter(flatRefs.length);
                final int[] resultIndex = new int[1];

                for (int i = 0; i < flatRefs.length; i++) {
                    this._resolver.lookup(_world, _time, flatRefs[i], new Callback<Node>() {
                        @Override
                        public void on(Node kNode) {
                            if (kNode != null) {
                                result[resultIndex[0]] = (A) kNode;
                                resultIndex[0]++;
                            }
                            counter.count();
                        }
                    });
                }
                counter.then(new Callback() {
                    @Override
                    public void on(Object o) {
                        if (resultIndex[0] == result.length) {
                            callback.on(result);
                        } else {
                            A[] toSend = (A[]) new Node[resultIndex[0]];
                            System.arraycopy(result, 0, toSend, 0, toSend.length);
                            callback.on(toSend);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void add(String relationName, Node relatedNode) {
        NodeState preciseState = this._resolver.resolveState(this, false);
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
            preciseState.set(relationKey, Type.LONG_ARRAY, previous);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void remove(String relationName, Node relatedNode) {
        NodeState preciseState = this._resolver.resolveState(this, false);
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
                        preciseState.set(relationKey, Type.LONG_ARRAY, null);
                    } else {
                        long[] newArray = new long[previous.length - 1];
                        System.arraycopy(previous, 0, newArray, 0, indexToRemove);
                        System.arraycopy(previous, indexToRemove + 1, newArray, indexToRemove, previous.length - indexToRemove - 1);
                        preciseState.set(relationKey, Type.LONG_ARRAY, newArray);
                    }
                }
            }
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void free() {
        this._resolver.freeNode(this);
    }

    @Override
    public long timeDephasing() {
        NodeState state = this._resolver.resolveState(this, true);
        if (state != null) {
            return (this._time - state.time());
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void forcePhase() {
        this._resolver.resolveState(this, false);
    }

    @Override
    public void timepoints(long beginningOfSearch, long endOfSearch, Callback<long[]> callback) {
        this._resolver.resolveTimepoints(this, beginningOfSearch, endOfSearch, callback);
    }

    @Override
    public <A extends Node> void jump(long targetTime, Callback<A> callback) {
        _resolver.lookup(_world, targetTime, _id, callback);
    }

    @Override
    public <A extends Node> void find(String indexName, String query, Callback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public <A extends Node> void findAt(String indexName, long world, long time, String query, Callback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public <A extends Node> void all(String indexName, Callback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }


    @Override
    public <A extends Node> void allAt(String indexName, long world, long time, Callback<A[]> callback) {
        throw new RuntimeException("Not Implemented");
    }

}
