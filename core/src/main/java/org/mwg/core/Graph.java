package org.mwg.core;

import org.mwg.*;
import org.mwg.struct.*;
import org.mwg.core.chunk.heap.ArrayLongLongMap;
import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.Resolver;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.Storage;
import org.mwg.core.chunk.*;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.task.Task;

import java.util.concurrent.atomic.AtomicBoolean;

class Graph implements org.mwg.Graph {

    private final Storage _storage;

    private final KChunkSpace _space;

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

    private static final int UNIVERSE_INDEX = 0;
    private static final int OBJ_INDEX = 1;
    private static final int GLO_TREE_INDEX = 2;
    private static final int GLO_DIC_INDEX = 3;

    Graph(Storage p_storage, KChunkSpace p_space, Scheduler p_scheduler, Resolver p_resolver, NodeFactory[] p_factories) {
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
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }

        long[] initPreviouslyResolved = new long[6];
        //init previously resolved values
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
        //init previous magics
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] = Constants.NULL_LONG;

        org.mwg.Node newNode = new Node(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        this._resolver.initNode(newNode, Constants.NULL_LONG);
        return newNode;
    }

    @Override
    public org.mwg.Node newNode(long world, long time, String nodeType) {
        if (!_isConnected.get()) {
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }

        long[] initPreviouslyResolved = new long[6];
        //init previously resolved values
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
        //init previous magics
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = Constants.NULL_LONG;
        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] = Constants.NULL_LONG;

        long extraCode = _resolver.stringToLongKey(nodeType);
        NodeFactory resolvedFactory = factoryByCode(extraCode);
        org.mwg.Node newNode;
        if (resolvedFactory == null) {
            System.out.println("WARNING: UnKnow NodeType " + nodeType + ", missing plugin configuration in the builder ? Using generic node as a fallback");
            newNode = new Node(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        } else {
            newNode = resolvedFactory.create(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        }
        this._resolver.initNode(newNode, extraCode);
        return newNode;
    }

    public final NodeFactory factoryByCode(long code) {
        if (_factoryNames != null) {
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
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }
        this._resolver.lookup(world, time, id, callback);
    }

    @Override
    public void save(Callback<Boolean> callback) {
        KChunkIterator dirtyIterator = this._space.detachDirties();
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
            final Graph selfPointer = this;
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

                    final Buffer[] connectionKeys = new Buffer[4];
                    //preload ObjKeyGenerator
                    connectionKeys[0] = newBuffer();
                    org.mwg.core.utility.Buffer.keyToBuffer(connectionKeys[0], Constants.KEY_GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, graphPrefix);
                    //preload WorldKeyGenerator
                    connectionKeys[1] = newBuffer();
                    org.mwg.core.utility.Buffer.keyToBuffer(connectionKeys[1], Constants.KEY_GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, graphPrefix);
                    //preload GlobalWorldOrder
                    connectionKeys[2] = newBuffer();
                    org.mwg.core.utility.Buffer.keyToBuffer(connectionKeys[2], Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
                    //preload GlobalDictionary
                    connectionKeys[3] = newBuffer();
                    org.mwg.core.utility.Buffer.keyToBuffer(connectionKeys[3], Constants.STATE_CHUNK, Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2]);
                    selfPointer._storage.get(connectionKeys, new Callback<Buffer[]>() {
                        @Override
                        public void on(Buffer[] payloads) {

                            for (int i = 0; i < connectionKeys.length; i++) {
                                connectionKeys[i].free();
                            }

                            if (payloads.length == 4) {
                                Boolean noError = true;
                                try {
                                    //init the global universe tree (mandatory for synchronious create)
                                    KWorldOrderChunk globalWorldOrder = (KWorldOrderChunk) selfPointer._space.create(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, payloads[GLO_TREE_INDEX], null);
                                    selfPointer._space.putAndMark(globalWorldOrder);

                                    //init the global dictionary chunk
                                    KStateChunk globalDictionaryChunk = (KStateChunk) selfPointer._space.create(Constants.STATE_CHUNK, Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2], payloads[GLO_DIC_INDEX], null);
                                    selfPointer._space.putAndMark(globalDictionaryChunk);

                                    if (payloads[UNIVERSE_INDEX] != null) {
                                        selfPointer._worldKeyCalculator = new KeyCalculator(graphPrefix, Base64.decodeToLongWithBounds(payloads[UNIVERSE_INDEX], 0, payloads[UNIVERSE_INDEX].size()));
                                    } else {
                                        selfPointer._worldKeyCalculator = new KeyCalculator(graphPrefix, 0);
                                    }

                                    if (payloads[OBJ_INDEX] != null) {
                                        selfPointer._nodeKeyCalculator = new KeyCalculator(graphPrefix, Base64.decodeToLongWithBounds(payloads[OBJ_INDEX], 0, payloads[OBJ_INDEX].size()));
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
            final Graph selfPointer = this;
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
            return org.mwg.core.utility.Buffer.newOffHeapBuffer();
        } else {
            return org.mwg.core.utility.Buffer.newHeapBuffer();
        }
    }

    @Override
    public Task newTask() {
        return new org.mwg.core.task.Task(this);
    }

    private void saveDirtyList(final KChunkIterator dirtyIterator, final Callback<Boolean> callback) {
        if (dirtyIterator.size() == 0) {
            dirtyIterator.free();
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            long sizeToSaveKeys = (dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE);
            Buffer[] toSaveKeys = new Buffer[(int) sizeToSaveKeys];
            long sizeToSaveValues = dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE;
            Buffer[] toSaveValues = new Buffer[(int) sizeToSaveValues];
            int i = 0;
            while (dirtyIterator.hasNext()) {
                KChunk loopChunk = dirtyIterator.next();
                if (loopChunk != null && (loopChunk.flags() & Constants.DIRTY_BIT) == Constants.DIRTY_BIT) {
                    //Save chunk Key
                    toSaveKeys[i] = newBuffer();
                    org.mwg.core.utility.Buffer.keyToBuffer(toSaveKeys[i], loopChunk.chunkType(), loopChunk.world(), loopChunk.time(), loopChunk.id());
                    //Save chunk payload
                    try {
                        Buffer newBuffer = newBuffer();
                        toSaveValues[i] = newBuffer;
                        loopChunk.save(newBuffer);
                        this._space.declareClean(loopChunk);
                        i++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //save obj key gen key
            toSaveKeys[i] = newBuffer();
            org.mwg.core.utility.Buffer.keyToBuffer(toSaveKeys[i], Constants.KEY_GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, this._nodeKeyCalculator.prefix());
            //save obj key gen payload
            toSaveValues[i] = newBuffer();
            Base64.encodeLongToBuffer(this._nodeKeyCalculator.lastComputedIndex(), toSaveValues[i]);
            i++;
            //save world key gen key
            toSaveKeys[i] = newBuffer();
            org.mwg.core.utility.Buffer.keyToBuffer(toSaveKeys[i], Constants.KEY_GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, this._worldKeyCalculator.prefix());
            //save world key gen payload
            toSaveValues[i] = newBuffer();
            Base64.encodeLongToBuffer(this._worldKeyCalculator.lastComputedIndex(), toSaveValues[i]);
            i++;

            //shrink in case of i != full size
            if (i != sizeToSaveValues) {
                //shrinkValue
                Buffer[] toSaveValuesShrinked = new Buffer[i];
                System.arraycopy(toSaveValues, 0, toSaveValuesShrinked, 0, i);
                toSaveValues = toSaveValuesShrinked;

                Buffer[] toSaveKeysShrinked = new Buffer[i];
                System.arraycopy(toSaveKeys, 0, toSaveKeysShrinked, 0, i);
                toSaveKeys = toSaveKeysShrinked;
            }
            final Buffer[] finalToSaveValues = toSaveValues;
            final Buffer[] finalToSaveKeys = toSaveKeys;
            this._storage.put(toSaveKeys, toSaveValues, new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    //free all value
                    for (int i = 0; i < finalToSaveValues.length; i++) {
                        finalToSaveValues[i].free();
                        finalToSaveKeys[i].free();
                    }
                    dirtyIterator.free();
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(result);
                    }
                }
            });
        }
    }

    @Override
    public void index(String indexName, org.mwg.Node toIndexNode, String[] keyAttributes, Callback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    throw new RuntimeException("Index creation failed, cache is probably full !!!");
                }
                foundIndex.index(Constants.INDEX_ATTRIBUTE, toIndexNode, keyAttributes, new Callback<Boolean>() {
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
    public void unindex(String indexName, org.mwg.Node toIndexNode, String[] keyAttributes, Callback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex != null) {
                    foundIndex.unindex(Constants.INDEX_ATTRIBUTE, toIndexNode, keyAttributes, new Callback<Boolean>() {
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
    public <A extends org.mwg.Node> void find(long world, long time, String indexName, String query, Callback<A[]> callback) {
        getIndexOrCreate(world, time, indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on((A[]) new org.mwg.Node[0]);
                    }
                } else {
                    foundIndex.find(Constants.INDEX_ATTRIBUTE, world, time, query, new Callback<A[]>() {
                        @Override
                        public void on(A[] collectedNodes) {
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
    public <A extends org.mwg.Node> void all(long world, long time, String indexName, Callback<A[]> callback) {
        getIndexOrCreate(world, time, indexName, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on((A[]) new org.mwg.Node[0]);
                    }
                } else {
                    foundIndex.all(Constants.INDEX_ATTRIBUTE, world, time, new Callback<A[]>() {
                        @Override
                        public void on(A[] collectedNodes) {
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
        final Graph selfPointer = this;
        final long indexNameCoded = this._resolver.stringToLongKey(indexName);
        this._resolver.lookup(world, time, Constants.END_OF_TIME, new Callback<org.mwg.Node>() {
            @Override
            public void on(org.mwg.Node globalIndexNodeUnsafe) {
                if (globalIndexNodeUnsafe == null && !createIfNull) {
                    callback.on(null);
                } else {
                    LongLongMap globalIndexContent;
                    if (globalIndexNodeUnsafe == null) {
                        long[] initPreviouslyResolved = new long[6];
                        //init previously resolved values
                        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
                        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
                        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
                        //init previous magics
                        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] = Constants.NULL_LONG;
                        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = Constants.NULL_LONG;
                        initPreviouslyResolved[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] = Constants.NULL_LONG;

                        globalIndexNodeUnsafe = new Node(world, time, Constants.END_OF_TIME, selfPointer, initPreviouslyResolved);
                        selfPointer._resolver.initNode(globalIndexNodeUnsafe, Constants.NULL_LONG);
                        globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.map(Constants.INDEX_ATTRIBUTE, Type.LONG_LONG_MAP);
                    } else {
                        globalIndexContent = (LongLongMap) globalIndexNodeUnsafe.get(Constants.INDEX_ATTRIBUTE);
                    }

                    long indexId = globalIndexContent.get(indexNameCoded);
                    //globalIndexNodeUnsafe.free();
                    if (indexId == Constants.NULL_LONG) {
                        if (createIfNull) {
                            //insert null
                            org.mwg.Node newIndexNode = newNode(world, time);
                            newIndexNode.map(Constants.INDEX_ATTRIBUTE, Type.LONG_LONG_ARRAY_MAP);
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
        return new org.mwg.core.utility.DeferCounter(expectedCountCalls);
    }

    @Override
    public Resolver resolver() {
        return _resolver;
    }

    @Override
    public Scheduler scheduler() {
        return _scheduler;
    }

    public KChunkSpace space() {
        return _space;
    }

}
