package org.mwg.core;

import org.mwg.*;
import org.mwg.core.chunk.GenChunk;
import org.mwg.core.chunk.StateChunk;
import org.mwg.core.chunk.WorldOrderChunk;
import org.mwg.core.task.CoreTask;
import org.mwg.core.utility.BufferBuilder;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.core.utility.CoreDeferCounterSync;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.plugin.*;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;
import org.mwg.struct.LongLongMap;
import org.mwg.struct.LongLongMapCallBack;
import org.mwg.task.TaskActionFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class CoreGraph implements org.mwg.Graph {

    private final Storage _storage;

    private final ChunkSpace _space;

    private final Scheduler _scheduler;

    private final Resolver _resolver;

    private final java.util.Map<Long, NodeFactory> _nodeTypes;

    private final java.util.Map<String, TaskActionFactory> _taskActions;

    boolean offHeapBuffer = false;

    private Short _prefix = null;

    private GenChunk _nodeKeyCalculator = null;
    private GenChunk _worldKeyCalculator = null;

    private final AtomicBoolean _isConnected;
    private final AtomicBoolean _lock;

    private final Plugin[] _plugins;

    CoreGraph(Storage p_storage, ChunkSpace p_space, Scheduler p_scheduler, Resolver p_resolver, Plugin[] p_plugins) {
        //subElements set
        this._storage = p_storage;
        this._space = p_space;
        this._space.setGraph(this);
        this._scheduler = p_scheduler;
        this._resolver = p_resolver;
        //init default taskActions
        this._taskActions = new HashMap<String, TaskActionFactory>();
        CoreTask.fillDefault(this._taskActions);
        if (p_plugins != null) {
            this._nodeTypes = new HashMap<Long, NodeFactory>();
            for (int i = 0; i < p_plugins.length; i++) {
                Plugin loopPlugin = p_plugins[i];
                String[] plugin_names = loopPlugin.nodeTypes();
                for (int j = 0; j < plugin_names.length; j++) {
                    String plugin_name = plugin_names[j];
                    this._nodeTypes.put(_resolver.stringToHash(plugin_name, false), loopPlugin.nodeType(plugin_name));
                }
                String[] task_names = loopPlugin.taskActionTypes();
                for (int j = 0; j < task_names.length; j++) {
                    String task_name = task_names[j];
                    this._taskActions.put(task_name, loopPlugin.taskActionType(task_name));
                }
            }
        } else {
            this._nodeTypes = null;
        }
        //variables init
        this._isConnected = new AtomicBoolean(false);
        this._lock = new AtomicBoolean(false);
        this._plugins = p_plugins;
    }

    @Override
    public long fork(long world) {
        long childWorld = this._worldKeyCalculator.newKey();
        this._resolver.initWorld(world, childWorld);
        return childWorld;
    }

    @Override
    public org.mwg.Node newNode(long world, long time) {
        if (!_isConnected.get()) {
            throw new RuntimeException(CoreConstants.DISCONNECTED_ERROR);
        }
        long[] initPreviouslyResolved = new long[6];
        //init previously resolved values
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
        //init previous magics
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = Constants.NULL_LONG;
        org.mwg.Node newNode = new CoreNode(world, time, this._nodeKeyCalculator.newKey(), this, initPreviouslyResolved);
        this._resolver.initNode(newNode, Constants.NULL_LONG);
        return newNode;
    }

    @Override
    public org.mwg.Node newTypedNode(long world, long time, String nodeType) {
        if (nodeType == null) {
            throw new RuntimeException("nodeType should not be null");
        }
        if (!_isConnected.get()) {
            throw new RuntimeException(CoreConstants.DISCONNECTED_ERROR);
        }
        long[] initPreviouslyResolved = new long[6];
        //init previously resolved values
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
        //init previous magics
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = Constants.NULL_LONG;
        long extraCode = _resolver.stringToHash(nodeType, false);
        NodeFactory resolvedFactory = factoryByCode(extraCode);
        AbstractNode newNode;
        if (resolvedFactory == null) {
            System.out.println("WARNING: UnKnow NodeType " + nodeType + ", missing plugin configuration in the builder ? Using generic node as a fallback");
            newNode = new CoreNode(world, time, this._nodeKeyCalculator.newKey(), this, initPreviouslyResolved);
        } else {
            newNode = (AbstractNode) resolvedFactory.create(world, time, this._nodeKeyCalculator.newKey(), this, initPreviouslyResolved);
        }
        this._resolver.initNode(newNode, extraCode);
        return newNode;
    }

    @Override
    public Node cloneNode(Node origin) {
        if (origin == null) {
            throw new RuntimeException("origin node should not be null");
        }
        if (!_isConnected.get()) {
            throw new RuntimeException(CoreConstants.DISCONNECTED_ERROR);
        }
       /* AbstractNode castedOrigin = (AbstractNode) origin;
        long[] initPreviouslyResolved = castedOrigin._previousResolveds.get();
        if (initPreviouslyResolved == null) {
            throw new RuntimeException(CoreConstants.DEAD_NODE_ERROR + " node id: " + origin.id());
        }
        long typeCode = _resolver.markNodeAndGetType(origin);
        NodeFactory resolvedFactory = factoryByCode(typeCode);
        org.mwg.Node newNode;
        if (resolvedFactory == null) {
            newNode = new CoreNode(castedOrigin.world(), castedOrigin.time(), castedOrigin.id(), this, initPreviouslyResolved);
        } else {
            newNode = resolvedFactory.create(castedOrigin.world(), castedOrigin.time(), castedOrigin.id(), this, initPreviouslyResolved);
        }
        return newNode;*/
        return origin.clone();
    }

    public NodeFactory factoryByCode(long code) {
        if (_nodeTypes != null && code != Constants.NULL_LONG) {
            return _nodeTypes.get(code);
        } else {
            return null;
        }
    }

    @Override
    public TaskActionFactory taskAction(String taskActionName) {
        if (this._taskActions != null && taskActionName != null) {
            return _taskActions.get(taskActionName);
        } else {
            return null;
        }
    }

    @Override
    public <A extends org.mwg.Node> void lookup(long world, long time, long id, Callback<A> callback) {
        if (!_isConnected.get()) {
            throw new RuntimeException(CoreConstants.DISCONNECTED_ERROR);
        }
        this._resolver.lookup(world, time, id, callback);
    }

    @Override
    public void save(Callback<Boolean> callback) {
        ChunkIterator dirtyIterator = this._space.detachDirties();
        saveDirtyList(dirtyIterator, callback);
    }

    @Override
    public void connect(final Callback<Boolean> callback) {
        final CoreGraph selfPointer = this;
        //negociate a lock
        while (selfPointer._lock.compareAndSet(false, true)) ;
        //ok we have it, let's go
        if (_isConnected.compareAndSet(false, true)) {
            //first connect the scheduler
            selfPointer._scheduler.start();
            selfPointer._storage.connect(selfPointer, new Callback<Boolean>() {
                @Override
                public void on(Boolean connection) {
                    selfPointer._storage.lock(new Callback<Buffer>() {
                        @Override
                        public void on(Buffer prefixBuf) {
                            _prefix = (short) Base64.decodeToIntWithBounds(prefixBuf, 0, prefixBuf.length());
                            prefixBuf.free();
                            final Buffer connectionKeys = selfPointer.newBuffer();
                            //preload ObjKeyGenerator
                            BufferBuilder.keyToBuffer(connectionKeys, ChunkType.GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, _prefix);
                            connectionKeys.write(CoreConstants.BUFFER_SEP);
                            //preload WorldKeyGenerator
                            BufferBuilder.keyToBuffer(connectionKeys, ChunkType.GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, _prefix);
                            connectionKeys.write(CoreConstants.BUFFER_SEP);
                            //preload GlobalWorldOrder
                            BufferBuilder.keyToBuffer(connectionKeys, ChunkType.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
                            connectionKeys.write(CoreConstants.BUFFER_SEP);
                            //preload GlobalDictionary
                            BufferBuilder.keyToBuffer(connectionKeys, ChunkType.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2]);
                            connectionKeys.write(CoreConstants.BUFFER_SEP);
                            selfPointer._storage.get(connectionKeys, new Callback<Buffer>() {
                                @Override
                                public void on(Buffer payloads) {
                                    connectionKeys.free();
                                    if (payloads != null) {
                                        BufferIterator it = payloads.iterator();
                                        Buffer view1 = it.next();
                                        Buffer view2 = it.next();
                                        Buffer view3 = it.next();
                                        Buffer view4 = it.next();

                                        Boolean noError = true;
                                        try {
                                            //init the global universe tree (mandatory for synchronious create)

                                            WorldOrderChunk globalWorldOrder;
                                            if (view3.length() > 0) {
                                                globalWorldOrder = (WorldOrderChunk) selfPointer._space.create(ChunkType.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, view3, null);
                                            } else {
                                                globalWorldOrder = (WorldOrderChunk) selfPointer._space.create(ChunkType.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, null, null);
                                            }
                                            selfPointer._space.putAndMark(globalWorldOrder);

                                            //init the global dictionary chunk
                                            StateChunk globalDictionaryChunk;
                                            if (view4.length() > 0) {
                                                globalDictionaryChunk = (StateChunk) selfPointer._space.create(ChunkType.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2], view4, null);
                                            } else {
                                                globalDictionaryChunk = (StateChunk) selfPointer._space.create(ChunkType.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2], null, null);
                                            }
                                            selfPointer._space.putAndMark(globalDictionaryChunk);

                                            if (view2.length() > 0) {
                                                selfPointer._worldKeyCalculator = (GenChunk) selfPointer._space.create(ChunkType.GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, _prefix, view2, null);
                                            } else {
                                                selfPointer._worldKeyCalculator = (GenChunk) selfPointer._space.create(ChunkType.GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, _prefix, null, null);
                                            }
                                            selfPointer._space.putAndMark(selfPointer._worldKeyCalculator);

                                            if (view1.length() > 0) {
                                                selfPointer._nodeKeyCalculator = (GenChunk) selfPointer._space.create(ChunkType.GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, _prefix, view1, null);
                                            } else {
                                                selfPointer._nodeKeyCalculator = (GenChunk) selfPointer._space.create(ChunkType.GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, _prefix, null, null);
                                            }
                                            selfPointer._space.putAndMark(selfPointer._nodeKeyCalculator);

                                            //init the resolver
                                            selfPointer._resolver.init(selfPointer);

                                            if (_plugins != null) {
                                                for (int i = 0; i < _plugins.length; i++) {
                                                    String[] nodeTypes = _plugins[i].nodeTypes();
                                                    if (nodeTypes != null) {
                                                        for (int j = 0; j < nodeTypes.length; j++) {
                                                            String pluginName = nodeTypes[j];
                                                            //make sure that all plugins are present to the graph dictionary
                                                            selfPointer._resolver.stringToHash(pluginName, true);
                                                        }
                                                    }
                                                }
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            noError = false;
                                        }
                                        payloads.free();
                                        selfPointer._lock.set(true);
                                        if (PrimitiveHelper.isDefined(callback)) {
                                            callback.on(noError);
                                        }
                                    } else {
                                        selfPointer._lock.set(true);
                                        if (PrimitiveHelper.isDefined(callback)) {
                                            callback.on(false);
                                        }
                                    }

                                }
                            });
                        }
                    });
                }
            });

        } else {
            //already connected
            selfPointer._lock.set(true);
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        }
    }

    @Override
    public void disconnect(final Callback callback) {
        while (this._lock.compareAndSet(false, true)) ;
        //ok we have the lock
        if (_isConnected.compareAndSet(true, false)) {
            //JS workaround for closure encapsulation and this variable
            final CoreGraph selfPointer = this;
            //first we stop scheduler, no tasks will be executed anymore
            selfPointer._scheduler.stop();
            save(new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    selfPointer._space.free();
                    if (selfPointer._storage != null) {

                        final Buffer prefixBuf = selfPointer.newBuffer();
                        Base64.encodeIntToBuffer(selfPointer._prefix, prefixBuf);

                        selfPointer._storage.unlock(prefixBuf, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                prefixBuf.free();
                                selfPointer._storage.disconnect(new Callback<Boolean>() {
                                    @Override
                                    public void on(Boolean result) {
                                        selfPointer._lock.set(true);
                                        if (PrimitiveHelper.isDefined(callback)) {
                                            callback.on(result);
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        selfPointer._lock.set(true);
                        if (PrimitiveHelper.isDefined(callback)) {
                            callback.on(result);
                        }
                    }
                }
            });
        } else {
            //not previously connected
            this._lock.set(true);
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        }
    }

    @Override
    public Buffer newBuffer() {
        if (offHeapBuffer) {
            return BufferBuilder.newOffHeapBuffer();
        } else {
            return BufferBuilder.newHeapBuffer();
        }
    }

    @Override
    public Query newQuery() {
        return new CoreQuery(_resolver);
    }

    private void saveDirtyList(final ChunkIterator dirtyIterator, final Callback<Boolean> callback) {
        if (dirtyIterator.size() == 0) {
            dirtyIterator.free();
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            boolean isNoop = this._storage instanceof BlackHoleStorage;
            final Buffer stream = newBuffer();
            boolean isFirst = true;
            while (dirtyIterator.hasNext()) {
                Chunk loopChunk = dirtyIterator.next();
                if (loopChunk != null && (loopChunk.flags() & CoreConstants.DIRTY_BIT) == CoreConstants.DIRTY_BIT) {
                    //Save chunk Key
                    if (!isNoop) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            stream.write(CoreConstants.BUFFER_SEP);
                        }
                        BufferBuilder.keyToBuffer(stream, loopChunk.chunkType(), loopChunk.world(), loopChunk.time(), loopChunk.id());
                    }
                    //Save chunk payload
                    stream.write(CoreConstants.BUFFER_SEP);
                    try {
                        if (!isNoop) { //optimization to not save unused bytes
                            loopChunk.save(stream);
                        }
                        this._space.declareClean(loopChunk);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //shrink in case of i != full size
            this._storage.put(stream, new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    //free all value
                    stream.free();
                    dirtyIterator.free();
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(result);
                    }
                }
            });
        }
    }

    @Override
    public void index(final String indexName, final org.mwg.Node toIndexNode, final String flatKeyAttributes, final Callback<Boolean> callback) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        if (toIndexNode == null) {
            throw new RuntimeException("toIndexNode should not be null");
        }
        if (flatKeyAttributes == null) {
            throw new RuntimeException("flatKeyAttributes should not be null");
        }
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(final org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    throw new RuntimeException("Index creation failed, cache is probably full !!!");
                }
                foundIndex.index(CoreConstants.INDEX_ATTRIBUTE, toIndexNode, flatKeyAttributes, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        foundIndex.free();
                        if (PrimitiveHelper.isDefined(callback)) {
                            callback.on(result);
                        }
                    }
                });
            }
        }, true);
    }

    @Override
    public void unindex(final String indexName, final org.mwg.Node nodeToUnindex, final String flatKeyAttributes, final Callback<Boolean> callback) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        if (nodeToUnindex == null) {
            throw new RuntimeException("toIndexNode should not be null");
        }
        if (flatKeyAttributes == null) {
            throw new RuntimeException("flatKeyAttributes should not be null");
        }
        getIndexOrCreate(nodeToUnindex.world(), nodeToUnindex.time(), indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(final org.mwg.Node foundIndex) {
                if (foundIndex != null) {
                    foundIndex.unindex(CoreConstants.INDEX_ATTRIBUTE, nodeToUnindex, flatKeyAttributes, new Callback<Boolean>() {
                        @Override
                        public void on(Boolean result) {
                            foundIndex.free();
                            if (PrimitiveHelper.isDefined(callback)) {
                                callback.on(result);
                            }
                        }
                    });
                } else {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(false);
                    }
                }
            }
        }, false);
    }

    @Override
    public void indexes(final long world, final long time, final Callback<String[]> callback) {
        final CoreGraph selfPointer = this;
        this._resolver.lookup(world, time, CoreConstants.END_OF_TIME, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node globalIndexNodeUnsafe) {
                if (globalIndexNodeUnsafe == null) {
                    callback.on(new String[0]);
                } else {
                    LongLongMap globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.get(CoreConstants.INDEX_ATTRIBUTE);
                    if (globalIndexContent == null) {
                        globalIndexNodeUnsafe.free();
                        callback.on(new String[0]);
                    } else {
                        final String[] result = new String[(int) globalIndexContent.size()];
                        final int[] resultIndex = {0};
                        globalIndexContent.each(new LongLongMapCallBack() {
                            @Override
                            public void on(long key, long value) {
                                result[resultIndex[0]] = selfPointer._resolver.hashToString(key);
                                resultIndex[0]++;
                            }
                        });
                        globalIndexNodeUnsafe.free();
                        callback.on(result);
                    }
                }
            }
        });
    }

    @Override
    public void find(final long world, final long time, final String indexName, final String query, final Callback<org.mwg.Node[]> callback) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        if (query == null) {
            throw new RuntimeException("query should not be null");
        }
        getIndexOrCreate(world, time, indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(final org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(new Node[0]);
                    }
                } else {
                    foundIndex.find(CoreConstants.INDEX_ATTRIBUTE, query, new Callback<org.mwg.Node[]>() {
                        @Override
                        public void on(org.mwg.Node[] collectedNodes) {
                            foundIndex.free();
                            if (PrimitiveHelper.isDefined(callback)) {
                                callback.on(collectedNodes);
                            }
                        }
                    });
                }
            }
        }, false);
    }

    @Override
    public void findByQuery(final Query query, final Callback<Node[]> callback) {
        if (query == null) {
            throw new RuntimeException("query should not be null");
        }
        if (query.world() == Constants.NULL_LONG) {
            throw new RuntimeException("Please fill world parameter in query before first usage!");
        }
        if (query.time() == Constants.NULL_LONG) {
            throw new RuntimeException("Please fill time parameter in query before first usage!");
        }
        if (query.indexName() == null) {
            throw new RuntimeException("Please fill indexName parameter in query before first usage!");
        }

        getIndexOrCreate(query.world(), query.time(), query.indexName(), new Callback<org.mwg.Node>() {
            @Override
            public void on(final org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(new Node[0]);
                    }
                } else {
                    query.setIndexName(CoreConstants.INDEX_ATTRIBUTE);
                    foundIndex.findByQuery(query, new Callback<org.mwg.Node[]>() {
                        @Override
                        public void on(final org.mwg.Node[] collectedNodes) {
                            foundIndex.free();
                            if (PrimitiveHelper.isDefined(callback)) {
                                callback.on(collectedNodes);
                            }
                        }
                    });
                }
            }
        }, false);
    }

    @Override
    public void findAll(final long world, final long time, final String indexName, final Callback<org.mwg.Node[]> callback) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        getIndexOrCreate(world, time, indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(final org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(new Node[0]);
                    }
                } else {
                    foundIndex.findAll(CoreConstants.INDEX_ATTRIBUTE, new Callback<org.mwg.Node[]>() {
                        @Override
                        public void on(org.mwg.Node[] collectedNodes) {
                            foundIndex.free();
                            if (PrimitiveHelper.isDefined(callback)) {
                                callback.on(collectedNodes);
                            }
                        }
                    });
                }
            }
        }, false);
    }

    @Override
    public void getIndexNode(long world, long time, String indexName, Callback<Node> callback) {
        if (indexName == null) {
            throw new RuntimeException("indexName should not be null");
        }
        getIndexOrCreate(world, time, indexName, callback, false);
    }

    private void getIndexOrCreate(final long world, final long time, final String indexName, final Callback<org.mwg.Node> callback, final boolean createIfNull) {
        final CoreGraph selfPointer = this;
        final long indexNameCoded = this._resolver.stringToHash(indexName, createIfNull);
        this._resolver.lookup(world, time, CoreConstants.END_OF_TIME, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node globalIndexNodeUnsafe) {
                if (globalIndexNodeUnsafe == null && !createIfNull) {
                    callback.on(null);
                } else {
                    LongLongMap globalIndexContent;
                    if (globalIndexNodeUnsafe == null) {
                        long[] initPreviouslyResolved = new long[6];
                        //init previously resolved values
                        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
                        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
                        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
                        //init previous magics
                        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = CoreConstants.NULL_LONG;
                        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = CoreConstants.NULL_LONG;
                        initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = CoreConstants.NULL_LONG;

                        globalIndexNodeUnsafe = new CoreNode(world, time, CoreConstants.END_OF_TIME, selfPointer, initPreviouslyResolved);
                        selfPointer._resolver.initNode(globalIndexNodeUnsafe, CoreConstants.NULL_LONG);
                        globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.getOrCreateMap(CoreConstants.INDEX_ATTRIBUTE, Type.LONG_TO_LONG_MAP);
                    } else {
                        globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.get(CoreConstants.INDEX_ATTRIBUTE);
                    }

                    long indexId = globalIndexContent.get(indexNameCoded);
                    //globalIndexNodeUnsafe.free();
                    if (indexId == CoreConstants.NULL_LONG) {
                        if (createIfNull) {
                            //insert null
                            org.mwg.Node newIndexNode = selfPointer.newNode(world, time);
                            newIndexNode.getOrCreateMap(CoreConstants.INDEX_ATTRIBUTE, Type.LONG_TO_LONG_ARRAY_MAP);
                            indexId = newIndexNode.id();
                            globalIndexContent.put(indexNameCoded, indexId);
                            callback.on(newIndexNode);
                        } else {
                            callback.on(null);
                        }
                    } else {
                        selfPointer._resolver.lookup(world, time, indexId, callback);
                    }
                }
            }
        });
    }

    @Override
    public DeferCounter newCounter(int expectedCountCalls) {
        return new CoreDeferCounter(expectedCountCalls);
    }

    @Override
    public DeferCounterSync newSyncCounter(int expectedCountCalls) {
        return new CoreDeferCounterSync(expectedCountCalls);
    }

    @Override
    public Resolver resolver() {
        return _resolver;
    }

    @Override
    public Scheduler scheduler() {
        return _scheduler;
    }

    @Override
    public ChunkSpace space() {
        return _space;
    }

    @Override
    public Storage storage() {
        return _storage;
    }

    @Override
    public void freeNodes(Node[] nodes) {
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    nodes[i].free();
                }
            }
        }
    }
}
