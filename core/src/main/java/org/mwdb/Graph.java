package org.mwdb;

import org.mwdb.chunk.heap.ArrayLongLongMap;
import org.mwdb.manager.KeyCalculator;
import org.mwdb.plugin.KFactory;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.chunk.*;
import org.mwdb.utility.Base64;
import org.mwdb.utility.Buffer;
import org.mwdb.utility.DeferCounter;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class Graph implements KGraph {

    private final KStorage _storage;

    private final KChunkSpace _space;

    private final KScheduler _scheduler;

    private final KResolver _resolver;

    private final KFactory[] _factories;

    private final KLongLongMap _factoryNames;

    public boolean offHeapBuffer = false;

    //TODO rest of elements

    private Short _prefix = null;
    private KeyCalculator _nodeKeyCalculator = null;
    private KeyCalculator _worldKeyCalculator = null;

    private final AtomicBoolean _isConnected;
    private final AtomicBoolean _lock;

    /**
     * Local constants
     */
    private static final int UNIVERSE_INDEX = 0;
    private static final int OBJ_INDEX = 1;
    private static final int GLO_TREE_INDEX = 2;
    private static final int GLO_DIC_INDEX = 3;

    Graph(KStorage p_storage, KChunkSpace p_space, KScheduler p_scheduler, KResolver p_resolver, KFactory[] p_factories) {
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
    public KNode newNode(long world, long time) {
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

        KNode newNode = new Node(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        this._resolver.initNode(newNode, Constants.NULL_LONG);
        return newNode;
    }

    @Override
    public KNode newNode(long world, long time, String nodeType) {
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
        KFactory resolvedFactory = factoryByCode(extraCode);
        KNode newNode;
        if (resolvedFactory == null) {
            System.out.println("WARNING: UnKnow NodeType " + nodeType + ", missing plugin configuration in the builder ? Using generic node as a fallback");
            newNode = new Node(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        } else {
            newNode = resolvedFactory.create(world, time, this._nodeKeyCalculator.nextKey(), this, initPreviouslyResolved);
        }
        this._resolver.initNode(newNode, extraCode);
        return newNode;
    }

    public final KFactory factoryByCode(long code) {
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
    public <A extends KNode> void lookup(long world, long time, long id, KCallback<A> callback) {
        if (!_isConnected.get()) {
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }
        this._resolver.lookup(world, time, id, callback);
    }

    @Override
    public void save(KCallback<Boolean> callback) {
        KChunkIterator dirtyIterator = this._space.detachDirties();
        saveDirtyList(dirtyIterator, callback);
    }

    @Override
    public void connect(KCallback<Boolean> callback) {
        //negociate a lock
        while (this._lock.compareAndSet(false, true)) ;
        //ok we have it, let's go
        if (_isConnected.compareAndSet(false, true)) {
            //first connect the scheduler
            this._scheduler.start();
            final Graph selfPointer = this;
            this._storage.connect(this, new KCallback<Short>() {
                @Override
                public void on(Short graphPrefix) {
                    _prefix = graphPrefix;
                    if (_prefix == null) {
                        if (PrimitiveHelper.isDefined(callback)) {
                            callback.on(false);
                            return;
                        }
                    }

                    final KBuffer[] connectionKeys = new KBuffer[4];
                    //preload ObjKeyGenerator
                    connectionKeys[0] = newBuffer();
                    Buffer.keyToBuffer(connectionKeys[0], Constants.KEY_GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, graphPrefix);
                    //preload WorldKeyGenerator
                    connectionKeys[1] = newBuffer();
                    Buffer.keyToBuffer(connectionKeys[1], Constants.KEY_GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, graphPrefix);
                    //preload GlobalWorldOrder
                    connectionKeys[2] = newBuffer();
                    Buffer.keyToBuffer(connectionKeys[2], Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
                    //preload GlobalDictionary
                    connectionKeys[3] = newBuffer();
                    Buffer.keyToBuffer(connectionKeys[3], Constants.STATE_CHUNK, Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2]);
                    selfPointer._storage.get(connectionKeys, new KCallback<KBuffer[]>() {
                        @Override
                        public void on(KBuffer[] payloads) {

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

                                    final KFactory[] localFactories = selfPointer._factories;
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
    public void disconnect(KCallback callback) {
        while (this._lock.compareAndSet(false, true)) ;
        //ok we have the lock
        if (_isConnected.compareAndSet(true, false)) {
            //JS workaround for closure encapsulation and this variable
            final Graph selfPointer = this;
            //first we stop scheduler, no tasks will be executed anymore
            selfPointer._scheduler.stop();
            save(new KCallback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    //TODO maybe change to asynchronous code
                    selfPointer._space.free();
                    //_blas.disconnect();
                    if (selfPointer._storage != null) {
                        selfPointer._storage.disconnect(selfPointer._prefix, new KCallback<Boolean>() {
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
    public KBuffer newBuffer() {
        if (offHeapBuffer) {
            return Buffer.newOffHeapBuffer();
        } else {
            return Buffer.newHeapBuffer();
        }
    }

    private void saveDirtyList(final KChunkIterator dirtyIterator, final KCallback<Boolean> callback) {
        if (dirtyIterator.size() == 0) {
            dirtyIterator.free();
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            long sizeToSaveKeys = (dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE);
            KBuffer[] toSaveKeys = new KBuffer[(int) sizeToSaveKeys];
            long sizeToSaveValues = dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE;
            KBuffer[] toSaveValues = new KBuffer[(int) sizeToSaveValues];
            int i = 0;
            while (dirtyIterator.hasNext()) {
                KChunk loopChunk = dirtyIterator.next();
                if (loopChunk != null && (loopChunk.flags() & Constants.DIRTY_BIT) == Constants.DIRTY_BIT) {
                    //Save chunk Key
                    toSaveKeys[i] = newBuffer();
                    Buffer.keyToBuffer(toSaveKeys[i], loopChunk.chunkType(), loopChunk.world(), loopChunk.time(), loopChunk.id());
                    //Save chunk payload
                    try {
                        KBuffer newBuffer = newBuffer();
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
            Buffer.keyToBuffer(toSaveKeys[i], Constants.KEY_GEN_CHUNK, Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, this._nodeKeyCalculator.prefix());
            //save obj key gen payload
            toSaveValues[i] = newBuffer();
            Base64.encodeLongToBuffer(this._nodeKeyCalculator.lastComputedIndex(), toSaveValues[i]);
            i++;
            //save world key gen key
            toSaveKeys[i] = newBuffer();
            Buffer.keyToBuffer(toSaveKeys[i], Constants.KEY_GEN_CHUNK, Constants.END_OF_TIME, Constants.NULL_LONG, this._worldKeyCalculator.prefix());
            //save world key gen payload
            toSaveValues[i] = newBuffer();
            Base64.encodeLongToBuffer(this._worldKeyCalculator.lastComputedIndex(), toSaveValues[i]);
            i++;

            //shrink in case of i != full size
            if (i != sizeToSaveValues) {
                //shrinkValue
                KBuffer[] toSaveValuesShrinked = new KBuffer[i];
                System.arraycopy(toSaveValues, 0, toSaveValuesShrinked, 0, i);
                toSaveValues = toSaveValuesShrinked;

                KBuffer[] toSaveKeysShrinked = new KBuffer[i];
                System.arraycopy(toSaveKeys, 0, toSaveKeysShrinked, 0, i);
                toSaveKeys = toSaveKeysShrinked;
            }
            final KBuffer[] finalToSaveValues = toSaveValues;
            final KBuffer[] finalToSaveKeys = toSaveKeys;
            this._storage.put(toSaveKeys, toSaveValues, new KCallback<Boolean>() {
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
    public void index(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode foundIndex) {
                if (foundIndex == null) {
                    throw new RuntimeException("Index creation failed, cache is probably full !!!");
                }
                foundIndex.index(Constants.INDEX_ATTRIBUTE, toIndexNode, keyAttributes, new KCallback<Boolean>() {
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
    public void unindex(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode foundIndex) {
                if (foundIndex != null) {
                    foundIndex.unindex(Constants.INDEX_ATTRIBUTE, toIndexNode, keyAttributes, new KCallback<Boolean>() {
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
    public <A extends KNode> void find(long world, long time, String indexName, String query, KCallback<A[]> callback) {
        getIndexOrCreate(world, time, indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on((A[]) new KNode[0]);
                    }
                } else {
                    foundIndex.find(Constants.INDEX_ATTRIBUTE, world, time, query, new KCallback<A[]>() {
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
    public <A extends KNode> void all(long world, long time, String indexName, KCallback<A[]> callback) {
        getIndexOrCreate(world, time, indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode foundIndex) {
                if (foundIndex == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on((A[]) new KNode[0]);
                    }
                } else {
                    foundIndex.all(Constants.INDEX_ATTRIBUTE, world, time, new KCallback<A[]>() {
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

    private void getIndexOrCreate(long world, long time, String indexName, KCallback<KNode> callback, boolean createIfNull) {
        final Graph selfPointer = this;
        final long indexNameCoded = this._resolver.stringToLongKey(indexName);
        this._resolver.lookup(world, time, Constants.END_OF_TIME, new KCallback<KNode>() {
            @Override
            public void on(KNode globalIndexNodeUnsafe) {
                if (globalIndexNodeUnsafe == null && !createIfNull) {
                    callback.on(null);
                } else {
                    KLongLongMap globalIndexContent;
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
                        globalIndexContent = (KLongLongMap) globalIndexNodeUnsafe.attMap(Constants.INDEX_ATTRIBUTE, KType.LONG_LONG_MAP);
                    } else {
                        globalIndexContent = (KLongLongMap) globalIndexNodeUnsafe.att(Constants.INDEX_ATTRIBUTE);
                    }

                    long indexId = globalIndexContent.get(indexNameCoded);
                    //globalIndexNodeUnsafe.free();
                    if (indexId == Constants.NULL_LONG) {
                        if (createIfNull) {
                            //insert null
                            KNode newIndexNode = newNode(world, time);
                            newIndexNode.attMap(Constants.INDEX_ATTRIBUTE, KType.LONG_LONG_ARRAY_MAP);
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
    public KDeferCounter counter(int expectedCountCalls) {
        return new DeferCounter(expectedCountCalls);
    }

    @Override
    public KResolver resolver() {
        return _resolver;
    }

    public KChunkSpace space() {
        return _space;
    }

}
