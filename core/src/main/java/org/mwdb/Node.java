package org.mwdb;

import org.mwdb.chunk.*;
import org.mwdb.plugin.KResolver.KNodeState;
import org.mwdb.plugin.KResolver;
import org.mwdb.utility.DeferCounter;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Query;

import java.util.concurrent.atomic.AtomicInteger;
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
        KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.get(this._resolver.key(attributeName));
        }
        return null;
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        KNodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            preciseState.set(this._resolver.key(attributeName), attributeType, attributeValue);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public Object attMap(String attributeName, byte attributeType) {
        KNodeState preciseState = this._resolver.resolveState(this, false);
        if (preciseState != null) {
            return preciseState.getOrCreate(this._resolver.key(attributeName), attributeType);
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public int attType(String attributeName) {
        KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return resolved.getType(this._resolver.key(attributeName));
        }
        return -1;
    }

    @Override
    public void attRemove(String attributeName) {
        attSet(attributeName, KType.INT, null);
    }


    @Override
    public void rel(String relationName, KCallback<KNode[]> callback) {
        if (!PrimitiveHelper.isDefined(callback)) {
            return;
        }
        final KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            final long[] flatRefs = (long[]) resolved.get(this._resolver.key(relationName));
            if (flatRefs == null || flatRefs.length == 0) {
                callback.on(new KNode[0]);
            } else {
                final KNode[] result = new KNode[flatRefs.length];
                final DeferCounter counter = new DeferCounter(flatRefs.length);
                for (int i = 0; i < flatRefs.length; i++) {
                    final int fi = i;
                    this._resolver.lookup(_world, _time, flatRefs[i], new KCallback<KNode>() {
                        @Override
                        public void on(KNode kNode) {
                            result[fi] = kNode;
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
        KNodeState resolved = this._resolver.resolveState(this, true);
        if (resolved != null) {
            return (long[]) resolved.get(this._resolver.key(relationName));
        } else {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void relAdd(String relationName, KNode relatedNode) {
        KNodeState preciseState = this._resolver.resolveState(this, false);
        long relationKey = this._resolver.key(relationName);
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
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void relRemove(String relationName, KNode relatedNode) {
        KNodeState preciseState = this._resolver.resolveState(this, false);
        long relationKey = this._resolver.key(relationName);
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
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
    }

    @Override
    public void free() {
        this._resolver.freeNode(this);
    }

    @Override
    public long timeDephasing() {
        KStateChunk state = (KStateChunk) this._resolver.resolveState(this, true);
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
    public void timepoints(long beginningOfSearch, long endOfSearch, KCallback<long[]> callback) {
        this._resolver.resolveTimepoints(this, beginningOfSearch, endOfSearch, callback);
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.getOrCreate(this._resolver.key(indexName), KType.LONG_LONG_ARRAY_MAP);
        Query flatQuery = new Query();
        KResolver.KNodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
        for (int i = 0; i < keyAttributes.length; i++) {
            long attKey = this._resolver.key(keyAttributes[i]);
            Object attValue = toIndexNodeState.get(attKey);
            if (attValue != null) {
                flatQuery.add(attKey, attValue.toString());
            } else {
                flatQuery.add(attKey, null);
            }
        }
        flatQuery.compute();
        //TODO AUTOMATIC UPDATE
        indexMap.put(flatQuery.hash, nodeToIndex.id());
        if (PrimitiveHelper.isDefined(callback)) {
            callback.on(null);
        }
    }

    @Override
    public void find(String indexName, String query, KCallback<KNode> callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.get(this._resolver.key(indexName));
        if (indexMap != null) {
            final Node selfPointer = this;
            final Query flatQuery = Query.parseQuery(query, selfPointer._resolver);
            final long[] foundId = indexMap.get(flatQuery.hash);
            if (foundId == null) {
                callback.on(null);
                return;
            }
            final KNode[] resolved = new KNode[foundId.length];
            final DeferCounter waiter = new DeferCounter(foundId.length);
            //TODO replace by a par lookup
            final AtomicInteger loopInteger = new AtomicInteger(-1);
            for (int i = 0; i < foundId.length; i++) {
                selfPointer._resolver.lookup(selfPointer._world, selfPointer._time, foundId[i], new KCallback<KNode>() {
                    @Override
                    public void on(KNode resolvedNode) {
                        resolved[loopInteger.incrementAndGet()] = resolvedNode;
                        waiter.count();
                    }
                });
            }
            waiter.then(new KCallback() {
                @Override
                public void on(Object o) {
                    //filter
                    for (int i = 0; i < foundId.length; i++) {
                        KNode resolvedNode = resolved[i];
                        KResolver.KNodeState resolvedState = selfPointer._resolver.resolveState(resolvedNode, true);
                        boolean exact = true;
                        for (int j = 0; j < flatQuery.attributes.length; j++) {
                            Object obj = resolvedState.get(flatQuery.attributes[j]);
                            if (flatQuery.values[j] == null) {
                                if (obj != null) {
                                    exact = false;
                                    break;
                                }
                            } else {
                                if (obj == null) {
                                    exact = false;
                                    break;
                                } else {
                                    if (!PrimitiveHelper.equals(flatQuery.values[j], obj.toString())) {
                                        exact = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (exact) {
                            callback.on(resolvedNode);
                            return;
                        }
                    }
                    //not found :(
                    callback.on(null);
                }
            });
        } else {
            callback.on(null);
        }
    }

    @Override
    public void all(String indexName, KCallback<KNode[]> callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.get(this._resolver.key(indexName));
        if (indexMap != null) {
            final Node selfPointer = this;
            final KNode[] resolved = new KNode[(int) indexMap.size()];
            DeferCounter waiter = new DeferCounter((int) indexMap.size());
            //TODO replace by a parralel lookup
            final AtomicInteger loopInteger = new AtomicInteger(-1);
            indexMap.each(new KLongLongArrayMapCallBack() {
                @Override
                public void on(final long hash, final long nodeId) {
                    selfPointer._resolver.lookup(selfPointer._world, selfPointer._time, nodeId, new KCallback<KNode>() {
                        @Override
                        public void on(KNode resolvedNode) {
                            resolved[loopInteger.incrementAndGet()] = resolvedNode;
                            waiter.count();
                        }
                    });
                }
            });
            waiter.then(new KCallback() {
                @Override
                public void on(Object o) {
                    callback.on(resolved);
                }
            });
        } else {
            callback.on(new KNode[0]);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"world\":");
        builder.append(_world);
        builder.append(",\"time\":");
        builder.append(_time);
        builder.append(",\"id\":");
        builder.append(_id);
        KStateChunk state = (KStateChunk) this._resolver.resolveState(this, true);
        if (state != null) {
            builder.append(",\"data\": {");
            final boolean[] isFirst = {true};
            state.each(new KStateChunkCallBack() {
                @Override
                public void on(String attributeName, int elemType, Object elem) {
                    if (elem != null) {
                        if (isFirst[0]) {
                            isFirst[0] = false;
                        } else {
                            builder.append(",");
                        }
                        builder.append("\"");
                        builder.append(attributeName);
                        builder.append("\": ");
                        switch (elemType) {
                            /** Primitive types */
                            case KType.BOOL: {
                                if ((boolean) elem) {
                                    builder.append("0");
                                } else {
                                    builder.append("1");
                                }
                                break;
                            }
                            case KType.STRING: {
                                builder.append("\"");
                                builder.append(elem);
                                builder.append("\"");
                                break;
                            }
                            case KType.LONG: {
                                builder.append(elem);
                                break;
                            }
                            case KType.INT: {
                                builder.append(elem);
                                break;
                            }
                            case KType.DOUBLE: {
                                builder.append(elem);
                                break;
                            }
                            /** Array types */
                            case KType.DOUBLE_ARRAY: {
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
                            case KType.LONG_ARRAY: {
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
                            case KType.INT_ARRAY: {
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
            }, this._resolver);
            builder.append("}}");
        }
        return builder.toString();
    }

}
