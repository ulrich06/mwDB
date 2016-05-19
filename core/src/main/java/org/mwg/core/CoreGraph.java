package org.mwg.core;

import org.mwg.*;
import org.mwg.core.task.CoreTask;
import org.mwg.core.utility.BufferBuilder;
import org.mwg.core.utility.CoreDeferCounter;
import org.mwg.plugin.*;
import org.mwg.struct.*;
import org.mwg.core.chunk.heap.ArrayLongLongMap;
import org.mwg.core.chunk.*;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.task.Task;

import java.util.concurrent.atomic.AtomicBoolean;

class CoreGraph implements org.mwg.Graph {

    private final Storage _storage;

    private final ChunkSpace _space;

    private final Scheduler _scheduler;

    private final Resolver _resolver;

    private final NodeFactory[] _factories;

    private final LongLongMap _factoryNames;

    boolean offHeapBuffer = false;

    private Short _prefix = null;
    private KeyCalculator _nodeKeyCalculator = null;
    private KeyCalculator _worldKeyCalculator = null;

    private final AtomicBoolean _isConnected;
    private final AtomicBoolean _lock;

    CoreGraph(Storage p_storage, ChunkSpace p_space, Scheduler p_scheduler, Resolver p_resolver, NodeFactory[] p_factories) {
        //subElements set
        this._storage = p_storage;
        this._space = p_space;
        this._space.setGraph(this);
        this._scheduler = p_scheduler;
        this._resolver = p_resolver;

        this._factories = p_factories;
        //will be initialise at the connection
        if (this._factories != null) {
            this._factoryNames = new ArrayLongLongMap(null, this._factories.length, null);
        } else {
            this._factoryNames = null;
        }

        //variables init
        this._isConnected = new AtomicBoolean(false);
        this._lock = new AtomicBoolean(false);
    }

    @Override
    public long diverge(long world) {
        long childWorld = this._worldKeyCalculator.nextKey();
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

        org.mwg.Node newNode = new CoreNode(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        this._resolver.initNode(newNode, Constants.NULL_LONG);
        return newNode;
    }

    @Override
    public org.mwg.Node newTypedNode(long world, long time, String nodeType) {
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

        long extraCode = _resolver.stringToLongKey(nodeType);
        NodeFactory resolvedFactory = factoryByCode(extraCode);
        AbstractNode newNode;
        if (resolvedFactory == null) {
            System.out.println("WARNING: UnKnow NodeType " + nodeType + ", missing plugin configuration in the builder ? Using generic node as a fallback");
            newNode = new CoreNode(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        } else {
            newNode = (AbstractNode) resolvedFactory.create(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        }
        this._resolver.initNode(newNode, extraCode);
        return newNode;
    }

    @Override
    public Node cloneNode(Node origin) {
        if (!_isConnected.get()) {
            throw new RuntimeException(CoreConstants.DISCONNECTED_ERROR);
        }
        AbstractNode castedOrigin = (AbstractNode) origin;
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
        return newNode;
    }

    public NodeFactory factoryByCode(long code) {
        if (_factoryNames != null && code != Constants.NULL_LONG) {
            long resolvedFactoryIndex = _factoryNames.get(code);
            if (resolvedFactoryIndex != Constants.NULL_LONG) {
                return _factories[(int) resolvedFactoryIndex];
            } else {
                return null;
            }
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
    public void connect(Callback<Boolean> callback) {
        //negociate a lock
        while (this._lock.compareAndSet(false, true)) ;
        //ok we have it, let's go
        if (_isConnected.compareAndSet(false, true)) {
            //first connect the scheduler
            this._scheduler.start();
            final CoreGraph selfPointer = this;
            this._storage.connect(this, new Callback<Short>() {
                @Override
                public void on(Short graphPrefix) {
                    _prefix = graphPrefix;
                    if (_prefix == null) {
                        if (PrimitiveHelper.isDefined(callback)) {
                            callback.on(false);
                            return;
                        }
                    }
                    final Buffer connectionKeys = newBuffer();
                    //preload ObjKeyGenerator
                    BufferBuilder.keyToBuffer(connectionKeys, CoreConstants.KEY_GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, graphPrefix);
                    connectionKeys.write(CoreConstants.BUFFER_SEP);
                    //preload WorldKeyGenerator
                    BufferBuilder.keyToBuffer(connectionKeys, CoreConstants.KEY_GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, graphPrefix);
                    connectionKeys.write(CoreConstants.BUFFER_SEP);
                    //preload GlobalWorldOrder
                    BufferBuilder.keyToBuffer(connectionKeys, CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
                    connectionKeys.write(CoreConstants.BUFFER_SEP);
                    //preload GlobalDictionary
                    BufferBuilder.keyToBuffer(connectionKeys, CoreConstants.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2]);
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
                                    if (view3.size() > 0) {
                                        globalWorldOrder = (WorldOrderChunk) selfPointer._space.create(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, view3, null);
                                    } else {
                                        globalWorldOrder = (WorldOrderChunk) selfPointer._space.create(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, null, null);
                                    }
                                    selfPointer._space.putAndMark(globalWorldOrder);

                                    //init the global dictionary chunk
                                    StateChunk globalDictionaryChunk;
                                    if (view4.size() > 0) {
                                        globalDictionaryChunk = (StateChunk) selfPointer._space.create(CoreConstants.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2], view4, null);
                                    } else {
                                        globalDictionaryChunk = (StateChunk) selfPointer._space.create(CoreConstants.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2], null, null);
                                    }
                                    selfPointer._space.putAndMark(globalDictionaryChunk);

                                    if (view2.size() > 0) {
                                        selfPointer._worldKeyCalculator = new KeyCalculator(graphPrefix, Base64.decodeToLongWithBounds(view2, 0, view2.size()));
                                    } else {
                                        selfPointer._worldKeyCalculator = new KeyCalculator(graphPrefix, 0);
                                    }

                                    if (view1.size() > 0) {
                                        selfPointer._nodeKeyCalculator = new KeyCalculator(graphPrefix, Base64.decodeToLongWithBounds(view1, 0, view1.size()));
                                    } else {
                                        selfPointer._nodeKeyCalculator = new KeyCalculator(graphPrefix, 0);
                                    }

                                    //init the resolver
                                    selfPointer._resolver.init(selfPointer);

                                    final NodeFactory[] localFactories = selfPointer._factories;
                                    if (localFactories != null) {
                                        for (int i = 0; i < localFactories.length; i++) {
                                            final long encodedFactoryKey = selfPointer._resolver.stringToLongKey(localFactories[i].name());
                                            selfPointer._factoryNames.put(encodedFactoryKey, i);
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
        } else {
            //already connected
            this._lock.set(true);
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        }
    }

    @Override
    public void disconnect(Callback callback) {
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
                        selfPointer._storage.disconnect(selfPointer._prefix, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                selfPointer._lock.set(true);
                                if (PrimitiveHelper.isDefined(callback)) {
                                    callback.on(result);
                                }
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
    public Task newTask() {
        return new CoreTask(this);
    }

    private void saveDirtyList(final ChunkIterator dirtyIterator, final Callback<Boolean> callback) {
        if (dirtyIterator.size() == 0) {
            dirtyIterator.free();
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            Buffer stream = newBuffer();
            boolean isFirst = true;
            while (dirtyIterator.hasNext()) {
                Chunk loopChunk = dirtyIterator.next();
                if (loopChunk != null && (loopChunk.flags() & CoreConstants.DIRTY_BIT) == CoreConstants.DIRTY_BIT) {
                    //Save chunk Key
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        stream.write(CoreConstants.BUFFER_SEP);
                    }
                    BufferBuilder.keyToBuffer(stream, loopChunk.chunkType(), loopChunk.world(), loopChunk.time(), loopChunk.id());
                    //Save chunk payload
                    stream.write(CoreConstants.BUFFER_SEP);
                    try {
                        loopChunk.save(stream);
                        this._space.declareClean(loopChunk);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //save obj key gen key
            stream.write(CoreConstants.BUFFER_SEP);
            BufferBuilder.keyToBuffer(stream, CoreConstants.KEY_GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, this._nodeKeyCalculator.prefix());
            //save obj key gen payload
            stream.write(CoreConstants.BUFFER_SEP);
            Base64.encodeLongToBuffer(this._nodeKeyCalculator.lastComputedIndex(), stream);
            //save world key gen key
            stream.write(CoreConstants.BUFFER_SEP);
            BufferBuilder.keyToBuffer(stream, CoreConstants.KEY_GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, this._worldKeyCalculator.prefix());
            //save world key gen payload
            stream.write(CoreConstants.BUFFER_SEP);
            Base64.encodeLongToBuffer(this._worldKeyCalculator.lastComputedIndex(), stream);

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
    public void index(String indexName, org.mwg.Node toIndexNode, String flatKeyAttributes, Callback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
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
    public void unindex(String indexName, org.mwg.Node toIndexNode, String flatKeyAttributes, Callback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex != null) {
                    foundIndex.unindex(CoreConstants.INDEX_ATTRIBUTE, toIndexNode, flatKeyAttributes, new Callback<Boolean>() {
                        @Override
                        public void on(Boolean result) {
                            foundIndex.free();
                            if (PrimitiveHelper.isDefined(callback)) {
                                callback.on(result);
                            }
                        }
                    });
                }
            }
        }, false);
    }

    @Override
    public void find(long world, long time, String indexName, String query, Callback<org.mwg.Node[]> callback) {
        getIndexOrCreate(world, time, indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(new AbstractNode[0]);
                    }
                } else {
                    foundIndex.findAt(CoreConstants.INDEX_ATTRIBUTE, world, time, query, new Callback<org.mwg.Node[]>() {
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
    public void all(long world, long time, String indexName, Callback<org.mwg.Node[]> callback) {
        getIndexOrCreate(world, time, indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(new AbstractNode[0]);
                    }
                } else {
                    foundIndex.allAt(CoreConstants.INDEX_ATTRIBUTE, world, time, new Callback<org.mwg.Node[]>() {
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

    private void getIndexOrCreate(long world, long time, String indexName, Callback<org.mwg.Node> callback, boolean createIfNull) {
        final CoreGraph selfPointer = this;
        final long indexNameCoded = this._resolver.stringToLongKey(indexName);
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
                        globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.map(CoreConstants.INDEX_ATTRIBUTE, Type.LONG_LONG_MAP);
                    } else {
                        globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.get(CoreConstants.INDEX_ATTRIBUTE);
                    }

                    long indexId = globalIndexContent.get(indexNameCoded);
                    //globalIndexNodeUnsafe.free();
                    if (indexId == CoreConstants.NULL_LONG) {
                        if (createIfNull) {
                            //insert null
                            org.mwg.Node newIndexNode = newNode(world, time);
                            newIndexNode.map(CoreConstants.INDEX_ATTRIBUTE, Type.LONG_LONG_ARRAY_MAP);
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
    public DeferCounter counter(int expectedCountCalls) {
        return new CoreDeferCounter(expectedCountCalls);
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

}
