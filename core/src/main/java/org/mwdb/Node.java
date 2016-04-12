package org.mwdb;

import org.mwdb.chunk.KLongLongArrayMap;
import org.mwdb.chunk.KLongLongArrayMapCallBack;
import org.mwdb.chunk.KStateChunk;
import org.mwdb.chunk.KStateChunkCallBack;
import org.mwdb.plugin.KResolver;
import org.mwdb.utility.DeferCounter;
import org.mwdb.utility.PrimitiveHelper;
import org.mwdb.utility.Query;

import java.util.concurrent.atomic.AtomicInteger;

public class Node extends AbstractNode {

    public Node(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.getOrCreate(this._resolver.stringToLongKey(indexName), KType.LONG_LONG_ARRAY_MAP);
        Query flatQuery = new Query();
        KResolver.KNodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
        for (int i = 0; i < keyAttributes.length; i++) {
            long attKey = this._resolver.stringToLongKey(keyAttributes[i]);
            Object attValue = toIndexNodeState.get(attKey);
            if (attValue != null) {
                flatQuery.add(attKey, attValue.toString());
            } else {
                flatQuery.add(attKey, null);
            }
        }
        flatQuery.compute();
        //TODO AUTOMATIC UPDATE
        indexMap.put(flatQuery.hash(), nodeToIndex.id());
        if (PrimitiveHelper.isDefined(callback)) {
            callback.on(true);
        }
    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.get(this._resolver.stringToLongKey(indexName));
        if (indexMap != null) {
            Query flatQuery = new Query();
            KResolver.KNodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
            for (int i = 0; i < keyAttributes.length; i++) {
                long attKey = this._resolver.stringToLongKey(keyAttributes[i]);
                Object attValue = toIndexNodeState.get(attKey);
                if (attValue != null) {
                    flatQuery.add(attKey, attValue.toString());
                } else {
                    flatQuery.add(attKey, null);
                }
            }
            flatQuery.compute();
            //TODO AUTOMATIC UPDATE
            indexMap.remove(flatQuery.hash(), nodeToIndex.id());
        }
        if (PrimitiveHelper.isDefined(callback)) {
            callback.on(true);
        }
    }

    @Override
    public <A extends KNode> void find(String indexName, long world, long time, String query, KCallback<A[]> callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.get(this._resolver.stringToLongKey(indexName));
        if (indexMap != null) {
            final Node selfPointer = this;
            final Query flatQuery = Query.parseQuery(query, selfPointer._resolver);
            final long[] foundId = indexMap.get(flatQuery.hash());
            if (foundId == null) {
                callback.on((A[]) new KNode[0]);
                return;
            }
            final KNode[] resolved = new KNode[foundId.length];
            final DeferCounter waiter = new DeferCounter(foundId.length);
            //TODO replace by a par lookup
            final AtomicInteger nextResolvedTabIndex = new AtomicInteger(0);

            for (int i = 0; i < foundId.length; i++) {
                selfPointer._resolver.lookup(world, time, foundId[i], new KCallback<KNode>() {
                    @Override
                    public void on(KNode resolvedNode) {
                        if(resolvedNode != null) {
                            resolved[nextResolvedTabIndex.getAndIncrement()] = resolvedNode;
                        }
                        waiter.count();
                    }
                });
            }
            waiter.then(new KCallback() {
                @Override
                public void on(Object o) {
                    //filter
                    A[] resultSet = (A[]) new KNode[nextResolvedTabIndex.get()];
                    int resultSetIndex = 0;

                    for (int i = 0; i < resultSet.length; i++) {
                        KNode resolvedNode = resolved[i];
                        KResolver.KNodeState resolvedState = selfPointer._resolver.resolveState(resolvedNode, true);
                        boolean exact = true;
                        for (int j = 0; j < flatQuery.size; j++) {
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
                            resultSet[resultSetIndex] = (A) resolvedNode;
                            resultSetIndex++;
                        }
                    }
                    if (resultSet.length == resultSetIndex) {
                        callback.on(resultSet);
                    } else {
                        A[] trimmedResultSet = (A[]) new KNode[resultSetIndex];
                        System.arraycopy(resultSet, 0, trimmedResultSet, 0, resultSetIndex);
                        callback.on(trimmedResultSet);
                    }
                }
            });
        } else {
            callback.on((A[]) new KNode[0]);
        }
    }

    @Override
    public <A extends KNode> void find(String indexName, String query, KCallback<A[]> callback) {
        find(indexName,time(), world(),query,callback);
    }

    @Override
    public <A extends KNode> void all(String indexName, long world, long time, KCallback<A[]> callback) {
        KResolver.KNodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        KLongLongArrayMap indexMap = (KLongLongArrayMap) currentNodeState.get(this._resolver.stringToLongKey(indexName));
        if (indexMap != null) {
            final Node selfPointer = this;
            int mapSize = (int) indexMap.size();
            final A[] resolved = (A[]) new KNode[mapSize];
            DeferCounter waiter = new DeferCounter(mapSize);
            //TODO replace by a parralel lookup
            final AtomicInteger loopInteger = new AtomicInteger(0);
            indexMap.each(new KLongLongArrayMapCallBack() {
                @Override
                public void on(final long hash, final long nodeId) {
                    selfPointer._resolver.lookup(world, time, nodeId, new KCallback<KNode>() {
                        @Override
                        public void on(KNode resolvedNode) {
                            resolved[loopInteger.getAndIncrement()] = (A) resolvedNode;
                            waiter.count();
                        }
                    });
                }
            });
            waiter.then(new KCallback() {
                @Override
                public void on(Object o) {
                    if(loopInteger.get() == resolved.length) {
                        callback.on(resolved);
                    } else {
                        A[] toSend = (A[]) new KNode[loopInteger.get()];
                        System.arraycopy(resolved,0,toSend,0,toSend.length);
                        callback.on(toSend);
                    }
                }
            });
        } else {
            callback.on((A[]) new KNode[0]);
        }
    }

    @Override
    public <A extends KNode> void all(String indexName, KCallback<A[]> callback) {
        all(indexName,world(),time(),callback);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"world\":");
        builder.append(world());
        builder.append(",\"time\":");
        builder.append(time());
        builder.append(",\"id\":");
        builder.append(id());
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
