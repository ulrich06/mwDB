package org.mwg.plugin;

import org.mwg.*;
import org.mwg.struct.LongLongArrayMap;
import org.mwg.struct.LongLongArrayMapCallBack;
import org.mwg.struct.Map;

import java.util.concurrent.atomic.AtomicInteger;
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

    /**
     * This method should be overridden to init the object
     */
    public void init() {
        //noop
    }

    @Override
    public String nodeTypeName() {
        return this._resolver.typeName(this);
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
            return resolved.get(this._resolver.stringToHash(propertyName, false));
        }
        return null;
    }

    /**
     * @native ts
     * if (typeof propertyValue === 'string' || propertyValue instanceof String) {
     * this.setProperty(propertyName, org.mwg.Type.STRING, propertyValue);
     * } else if(typeof propertyValue === 'number' || propertyValue instanceof Number) {
     * if(propertyValue % 1 != 0) {
     * this.setProperty(propertyName, org.mwg.Type.DOUBLE, propertyValue);
     * } else {
     * this.setProperty(propertyName, org.mwg.Type.LONG, propertyValue);
     * }
     * } else if(typeof propertyValue === 'boolean' || propertyValue instanceof Boolean) {
     * this.setProperty(propertyName, org.mwg.Type.BOOL, propertyValue);
     * } else if (propertyValue instanceof Int32Array) {
     * this.setProperty(propertyName, org.mwg.Type.LONG_ARRAY, propertyValue);
     * } else if (propertyValue instanceof Float64Array) {
     * this.setProperty(propertyName, org.mwg.Type.DOUBLE_ARRAY, propertyValue);
     * } else {
     * throw new Error("Invalid property type: " + propertyValue + ", please use a Type listed in org.mwg.Type");
     * }
     */
    @Override
    public final void set(String propertyName, Object propertyValue) {
        if (propertyValue instanceof String) {
            setProperty(propertyName, Type.STRING, propertyValue);
        } else if (propertyValue instanceof Double) {
            setProperty(propertyName, Type.DOUBLE, propertyValue);
        } else if (propertyValue instanceof Long) {
            setProperty(propertyName, Type.LONG, propertyValue);
        } else if (propertyValue instanceof Float) {
            setProperty(propertyName, Type.DOUBLE, (double) ((Float) propertyValue));
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
            preciseState.set(this._resolver.stringToHash(propertyName, true), propertyType, propertyValue);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public Map getOrCreateMap(String propertyName, byte propertyType) {
        NodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            return (Map) preciseState.getOrCreate(this._resolver.stringToHash(propertyName, true), propertyType);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public byte type(String propertyName) {
        NodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.getType(this._resolver.stringToHash(propertyName, false));
        }
        return -1;
    }

    @Override
    public void removeProperty(String attributeName) {
        setProperty(attributeName, Type.INT, null);
    }

    @Override
    public void rel(String relationName, final Callback<Node[]> callback) {
        if (callback == null) {
            return;
        }
        final NodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            final long[] flatRefs = (long[]) resolved.get(this._resolver.stringToHash(relationName, false));
            if (flatRefs == null || flatRefs.length == 0) {
                callback.on(new Node[0]);
            } else {
                final Node[] result = new Node[flatRefs.length];
                final DeferCounter counter = _graph.newCounter(flatRefs.length);
                final int[] resultIndex = new int[1];
                for (int i = 0; i < flatRefs.length; i++) {
                    this._resolver.lookup(_world, _time, flatRefs[i], new Callback<Node>() {
                        @Override
                        public void on(Node kNode) {
                            if (kNode != null) {
                                result[resultIndex[0]] = kNode;
                                resultIndex[0]++;
                            }
                            counter.count();
                        }
                    });
                }
                counter.then(new Job() {
                    @Override
                    public void run() {
                        if (resultIndex[0] == result.length) {
                            callback.on(result);
                        } else {
                            Node[] toSend = new Node[resultIndex[0]];
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
        long relationKey = this._resolver.stringToHash(relationName, true);
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
            preciseState.set(relationKey, Type.RELATION, previous);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void remove(String relationName, Node relatedNode) {
        NodeState preciseState = this._resolver.resolveState(this, false);
        long relationKey = this._resolver.stringToHash(relationName, false);
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
                        preciseState.set(relationKey, Type.RELATION, null);
                    } else {
                        long[] newArray = new long[previous.length - 1];
                        System.arraycopy(previous, 0, newArray, 0, indexToRemove);
                        System.arraycopy(previous, indexToRemove + 1, newArray, indexToRemove, previous.length - indexToRemove - 1);
                        preciseState.set(relationKey, Type.RELATION, newArray);
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
    public void rephase() {
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
    public void findByQuery(final Query query, final Callback<Node[]> callback) {
        NodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        String indexName = query.indexName();
        if (indexName == null) {
            throw new RuntimeException("Please specify indexName in query before first use!");
        }
        long queryWorld = query.world();
        if (queryWorld == Constants.NULL_LONG) {
            queryWorld = world();
        }
        long queryTime = query.time();
        if (queryTime == Constants.NULL_LONG) {
            queryTime = time();
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.get(this._resolver.stringToHash(indexName, false));
        if (indexMap != null) {
            final AbstractNode selfPointer = this;
            final long[] foundId = indexMap.get(query.hash());
            if (foundId == null) {
                callback.on(new org.mwg.plugin.AbstractNode[0]);
                return;
            }
            final org.mwg.Node[] resolved = new org.mwg.plugin.AbstractNode[foundId.length];
            final DeferCounter waiter = _graph.newCounter(foundId.length);
            //TODO replace by a par lookup
            final AtomicInteger nextResolvedTabIndex = new AtomicInteger(0);
            for (int i = 0; i < foundId.length; i++) {
                selfPointer._resolver.lookup(queryWorld, queryTime, foundId[i], new Callback<org.mwg.Node>() {
                    @Override
                    public void on(org.mwg.Node resolvedNode) {
                        if (resolvedNode != null) {
                            resolved[nextResolvedTabIndex.getAndIncrement()] = resolvedNode;
                        }
                        waiter.count();
                    }
                });
            }
            waiter.then(new Job() {
                @Override
                public void run() {
                    //select
                    Node[] resultSet = new org.mwg.plugin.AbstractNode[nextResolvedTabIndex.get()];
                    int resultSetIndex = 0;
                    for (int i = 0; i < resultSet.length; i++) {
                        org.mwg.Node resolvedNode = resolved[i];
                        NodeState resolvedState = selfPointer._resolver.resolveState(resolvedNode, true);
                        boolean exact = true;
                        for (int j = 0; j < query.attributes().length; j++) {
                            Object obj = resolvedState.get(query.attributes()[j]);
                            if (query.values()[j] == null) {
                                if (obj != null) {
                                    exact = false;
                                    break;
                                }
                            } else {
                                if (obj == null) {
                                    exact = false;
                                    break;
                                } else {
                                    if (obj instanceof long[]) {
                                        if (query.values()[j] instanceof long[]) {
                                            if (!Constants.longArrayEquals((long[]) query.values()[j], (long[]) obj)) {
                                                exact = false;
                                                break;
                                            }
                                        } else {
                                            exact = false;
                                            break;
                                        }
                                    } else {
                                        if (!Constants.equals(query.values()[j].toString(), obj.toString())) {
                                            exact = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (exact) {
                            resultSet[resultSetIndex] = resolvedNode;
                            resultSetIndex++;
                        }
                    }
                    if (resultSet.length == resultSetIndex) {
                        callback.on(resultSet);
                    } else {
                        Node[] trimmedResultSet = new org.mwg.plugin.AbstractNode[resultSetIndex];
                        System.arraycopy(resultSet, 0, trimmedResultSet, 0, resultSetIndex);
                        callback.on(trimmedResultSet);
                    }
                }
            });
        } else {
            callback.on(new org.mwg.plugin.AbstractNode[0]);
        }
    }

    @Override
    public void find(String indexName, String query, Callback<Node[]> callback) {
        Query queryObj = _graph.newQuery();
        queryObj.setWorld(world());
        queryObj.setTime(time());
        queryObj.setIndexName(indexName);
        queryObj.parse(query);
        findByQuery(queryObj, callback);
    }

    @Override
    public void findAll(final String indexName, final Callback<Node[]> callback) {
        NodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.get(this._resolver.stringToHash(indexName, false));
        if (indexMap != null) {
            final AbstractNode selfPointer = this;
            int mapSize = (int) indexMap.size();
            final Node[] resolved = new org.mwg.plugin.AbstractNode[mapSize];
            final DeferCounter waiter = _graph.newCounter(mapSize);
            //TODO replace by a parralel lookup
            final AtomicInteger loopInteger = new AtomicInteger(0);
            indexMap.each(new LongLongArrayMapCallBack() {
                @Override
                public void on(final long hash, final long nodeId) {
                    selfPointer._resolver.lookup(world(), time(), nodeId, new Callback<org.mwg.Node>() {
                        @Override
                        public void on(org.mwg.Node resolvedNode) {
                            resolved[loopInteger.getAndIncrement()] = resolvedNode;
                            waiter.count();
                        }
                    });
                }
            });
            waiter.then(new Job() {
                @Override
                public void run() {
                    if (loopInteger.get() == resolved.length) {
                        callback.on(resolved);
                    } else {
                        Node[] toSend = new org.mwg.plugin.AbstractNode[loopInteger.get()];
                        System.arraycopy(resolved, 0, toSend, 0, toSend.length);
                        callback.on(toSend);
                    }
                }
            });
        } else {
            callback.on(new org.mwg.plugin.AbstractNode[0]);
        }
    }

    @Override
    public void index(String indexName, org.mwg.Node nodeToIndex, String flatKeyAttributes, Callback<Boolean> callback) {
        final String[] keyAttributes = flatKeyAttributes.split(Constants.QUERY_SEP + "");
        NodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.getOrCreate(this._resolver.stringToHash(indexName, true), Type.LONG_TO_LONG_ARRAY_MAP);
        Query flatQuery = _graph.newQuery();
        NodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
        for (int i = 0; i < keyAttributes.length; i++) {
            String attKey = keyAttributes[i];
            Object attValue = toIndexNodeState.getFromKey(attKey);
            if (attValue != null) {
                flatQuery.add(keyAttributes[i], attValue);
            } else {
                flatQuery.add(keyAttributes[i], null);
            }
        }
        //TODO AUTOMATIC UPDATE
        indexMap.put(flatQuery.hash(), nodeToIndex.id());
        if (Constants.isDefined(callback)) {
            callback.on(true);
        }
    }

    @Override
    public void unindex(String indexName, org.mwg.Node nodeToIndex, String flatKeyAttributes, Callback<Boolean> callback) {
        final String[] keyAttributes = flatKeyAttributes.split(Constants.QUERY_SEP + "");
        NodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.get(this._resolver.stringToHash(indexName, false));
        if (indexMap != null) {
            Query flatQuery = _graph.newQuery();
            NodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
            for (int i = 0; i < keyAttributes.length; i++) {
                String attKey = keyAttributes[i];
                Object attValue = toIndexNodeState.getFromKey(attKey);
                if (attValue != null) {
                    flatQuery.add(attKey, attValue.toString());
                } else {
                    flatQuery.add(attKey, null);
                }
            }
            //TODO AUTOMATIC UPDATE
            indexMap.remove(flatQuery.hash(), nodeToIndex.id());
        }
        if (Constants.isDefined(callback)) {
            callback.on(true);
        }
    }

    /**
     * @native ts
     * return isNaN(toTest);
     */
    private boolean isNaN(double toTest) {
        return Double.NaN == toTest;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{\"world\":");
        builder.append(world());
        builder.append(",\"time\":");
        builder.append(time());
        builder.append(",\"id\":");
        builder.append(id());
        NodeState state = this._resolver.resolveState(this, true);
        if (state != null) {
            state.each(new NodeStateCallback() {
                @Override
                public void on(long attributeKey, int elemType, Object elem) {
                    if (elem != null) {
                        switch (elemType) {
                            /** Primitive types */
                            case Type.BOOL: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                if ((Boolean) elem) {
                                    builder.append("0");
                                } else {
                                    builder.append("1");
                                }
                                break;
                            }
                            case Type.STRING: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                builder.append("\"");
                                builder.append(elem);
                                builder.append("\"");
                                break;
                            }
                            case Type.LONG: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                builder.append(elem);
                                break;
                            }
                            case Type.INT: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                builder.append(elem);
                                break;
                            }
                            case Type.DOUBLE: {
                                if (!isNaN((Double) elem)) {
                                    builder.append(",\"");
                                    builder.append(_resolver.hashToString(attributeKey));
                                    builder.append("\":");
                                    builder.append(elem);
                                }
                                break;
                            }
                            /** Array types */
                            case Type.DOUBLE_ARRAY: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                builder.append("[");
                                double[] castedArr = (double[]) elem;
                                for (int j = 0; j < castedArr.length; j++) {
                                    if (j != 0) {
                                        builder.append(",");
                                    }
                                    builder.append(castedArr[j]);
                                }
                                builder.append("]");
                                break;
                            }
                            case Type.RELATION:
                            case Type.LONG_ARRAY: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                builder.append("[");
                                long[] castedArr2 = (long[]) elem;
                                for (int j = 0; j < castedArr2.length; j++) {
                                    if (j != 0) {
                                        builder.append(",");
                                    }
                                    builder.append(castedArr2[j]);
                                }
                                builder.append("]");
                                break;
                            }
                            case Type.INT_ARRAY: {
                                builder.append(",\"");
                                builder.append(_resolver.hashToString(attributeKey));
                                builder.append("\":");
                                builder.append("[");
                                int[] castedArr3 = (int[]) elem;
                                for (int j = 0; j < castedArr3.length; j++) {
                                    if (j != 0) {
                                        builder.append(",");
                                    }
                                    builder.append(castedArr3[j]);
                                }
                                builder.append("]");
                                break;
                            }
                        }
                    }
                }
            });
            builder.append("}");
        }
        return builder.toString();
    }

    @Override
    public Node clone() {
        long[] initPreviouslyResolved = _previousResolveds.get();
        if (initPreviouslyResolved == null) {
            throw new RuntimeException("This Node has been tagged destroyed, please don't use it anymore! node id: " + id());
        }

        long typeCode = _resolver.markNodeAndGetType(this);
        NodeFactory resolvedFactory = _graph.factoryByCode(typeCode);
        return resolvedFactory.create(world(), time(), id(), _graph, initPreviouslyResolved);
    }
}
