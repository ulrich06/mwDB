package org.mwg.core;

import org.mwg.*;
import org.mwg.Graph;
import org.mwg.core.chunk.StateChunk;
import org.mwg.plugin.NodeState;
import org.mwg.struct.LongLongArrayMap;
import org.mwg.struct.LongLongArrayMapCallBack;
import org.mwg.core.chunk.StateChunkCallBack;
import org.mwg.plugin.AbstractNode;
import org.mwg.core.utility.DeferCounter;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.core.utility.Query;

import java.util.concurrent.atomic.AtomicInteger;

class Node extends AbstractNode {

    public Node(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void index(String indexName, org.mwg.Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {
        NodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.getOrCreate(this._resolver.stringToLongKey(indexName), Type.LONG_LONG_ARRAY_MAP);
        Query flatQuery = new Query();
        NodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
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
    public void unindex(String indexName, org.mwg.Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {
        NodeState currentNodeState = this._resolver.resolveState(this, true);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.get(this._resolver.stringToLongKey(indexName));
        if (indexMap != null) {
            Query flatQuery = new Query();
            NodeState toIndexNodeState = this._resolver.resolveState(nodeToIndex, true);
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
    public <A extends org.mwg.Node> void findAt(String indexName, long world, long time, String query, Callback<A[]> callback) {
        NodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.get(this._resolver.stringToLongKey(indexName));
        if (indexMap != null) {
            final Node selfPointer = this;
            final Query flatQuery = Query.parseQuery(query, selfPointer._resolver);
            final long[] foundId = indexMap.get(flatQuery.hash());
            if (foundId == null) {
                callback.on((A[]) new org.mwg.Node[0]);
                return;
            }
            final org.mwg.Node[] resolved = new org.mwg.Node[foundId.length];
            final DeferCounter waiter = new DeferCounter(foundId.length);
            //TODO replace by a par lookup
            final AtomicInteger nextResolvedTabIndex = new AtomicInteger(0);

            for (int i = 0; i < foundId.length; i++) {
                selfPointer._resolver.lookup(world, time, foundId[i], new Callback<org.mwg.Node>() {
                    @Override
                    public void on(org.mwg.Node resolvedNode) {
                        if (resolvedNode != null) {
                            resolved[nextResolvedTabIndex.getAndIncrement()] = resolvedNode;
                        }
                        waiter.count();
                    }
                });
            }
            waiter.then(new Callback() {
                @Override
                public void on(Object o) {
                    //select
                    A[] resultSet = (A[]) new org.mwg.Node[nextResolvedTabIndex.get()];
                    int resultSetIndex = 0;

                    for (int i = 0; i < resultSet.length; i++) {
                        org.mwg.Node resolvedNode = resolved[i];
                        NodeState resolvedState = selfPointer._resolver.resolveState(resolvedNode, true);
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
                        A[] trimmedResultSet = (A[]) new org.mwg.Node[resultSetIndex];
                        System.arraycopy(resultSet, 0, trimmedResultSet, 0, resultSetIndex);
                        callback.on(trimmedResultSet);
                    }
                }
            });
        } else {
            callback.on((A[]) new org.mwg.Node[0]);
        }
    }

    @Override
    public <A extends org.mwg.Node> void find(String indexName, String query, Callback<A[]> callback) {
        findAt(indexName, time(), world(), query, callback);
    }

    @Override
    public <A extends org.mwg.Node> void allAt(String indexName, long world, long time, Callback<A[]> callback) {
        NodeState currentNodeState = this._resolver.resolveState(this, false);
        if (currentNodeState == null) {
            throw new RuntimeException(Constants.CACHE_MISS_ERROR);
        }
        LongLongArrayMap indexMap = (LongLongArrayMap) currentNodeState.get(this._resolver.stringToLongKey(indexName));
        if (indexMap != null) {
            final Node selfPointer = this;
            int mapSize = (int) indexMap.size();
            final A[] resolved = (A[]) new org.mwg.Node[mapSize];
            DeferCounter waiter = new DeferCounter(mapSize);
            //TODO replace by a parralel lookup
            final AtomicInteger loopInteger = new AtomicInteger(0);
            indexMap.each(new LongLongArrayMapCallBack() {
                @Override
                public void on(final long hash, final long nodeId) {
                    selfPointer._resolver.lookup(world, time, nodeId, new Callback<org.mwg.Node>() {
                        @Override
                        public void on(org.mwg.Node resolvedNode) {
                            resolved[loopInteger.getAndIncrement()] = (A) resolvedNode;
                            waiter.count();
                        }
                    });
                }
            });
            waiter.then(new Callback() {
                @Override
                public void on(Object o) {
                    if (loopInteger.get() == resolved.length) {
                        callback.on(resolved);
                    } else {
                        A[] toSend = (A[]) new org.mwg.Node[loopInteger.get()];
                        System.arraycopy(resolved, 0, toSend, 0, toSend.length);
                        callback.on(toSend);
                    }
                }
            });
        } else {
            callback.on((A[]) new org.mwg.Node[0]);
        }
    }

    @Override
    public <A extends org.mwg.Node> void all(String indexName, Callback<A[]> callback) {
        allAt(indexName, world(), time(), callback);
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
        StateChunk state = (StateChunk) this._resolver.resolveState(this, true);
        if (state != null) {
            builder.append(",\"data\": {");
            final boolean[] isFirst = {true};
            state.each(new StateChunkCallBack() {
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
                            case Type.BOOL: {
                                if ((boolean) elem) {
                                    builder.append("0");
                                } else {
                                    builder.append("1");
                                }
                                break;
                            }
                            case Type.STRING: {
                                builder.append("\"");
                                builder.append(elem);
                                builder.append("\"");
                                break;
                            }
                            case Type.LONG: {
                                builder.append(elem);
                                break;
                            }
                            case Type.INT: {
                                builder.append(elem);
                                break;
                            }
                            case Type.DOUBLE: {
                                builder.append(elem);
                                break;
                            }
                            /** Array types */
                            case Type.DOUBLE_ARRAY: {
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
                            case Type.LONG_ARRAY: {
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
