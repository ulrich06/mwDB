package org.mwdb;

import org.mwdb.manager.KeyCalculator;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.chunk.*;
import org.mwdb.utility.DeferCounter;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class Graph implements KGraph {

    private final KStorage _storage;

    private final KChunkSpace _space;

    private final KScheduler _scheduler;

    private final KResolver _resolver;

    //TODO rest of elements

    private KeyCalculator _objectKeyCalculator = null;
    private KeyCalculator _universeKeyCalculator = null;

    private final AtomicBoolean _isConnected;
    private final AtomicBoolean _lock;

    /**
     * Local constants
     */
    private static final int UNIVERSE_INDEX = 0;
    private static final int OBJ_INDEX = 1;
    private static final int GLO_TREE_INDEX = 2;
    private static final int GLO_DIC_INDEX = 3;

    protected Graph(KStorage p_storage, KChunkSpace p_space, KScheduler p_scheduler, KResolver p_resolver) {
        //subElements set
        this._storage = p_storage;
        this._space = p_space;
        this._space.setGraph(this);
        this._scheduler = p_scheduler;
        this._resolver = p_resolver;
        //variables init
        this._isConnected = new AtomicBoolean(false);
        this._lock = new AtomicBoolean(false);
    }

    @Override
    public long diverge(long world) {
        long childWorld = this._universeKeyCalculator.nextKey();
        this._resolver.initWorld(world, childWorld);
        return childWorld;
    }

    @Override
    public KNode newNode(long world, long time) {
        if (!_isConnected.get()) {
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }
        KNode newNode = new Node(world, time, this._objectKeyCalculator.nextKey(), this._resolver, world, time, Constants.NULL_LONG, Constants.NULL_LONG);
        this._resolver.initNode(newNode);
        return newNode;
    }

    @Override
    public void lookup(long world, long time, long id, KCallback<KNode> callback) {
        if (!_isConnected.get()) {
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }
        this._resolver.lookup(world, time, id, callback);
    }

    @Override
    public void save(KCallback callback) {
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
            this._storage.connect(new KCallback<Boolean>() {
                @Override
                public void on(Boolean connectResult) {
                    if (connectResult) {
                        selfPointer._storage.atomicGetIncrement(Constants.PREFIX_KEY,
                                new KCallback<Short>() {
                                    @Override
                                    public void on(final Short graphPrefix) {
                                        long[] connectionKeys = new long[]{
                                                Constants.BEGINNING_OF_TIME, Constants.NULL_LONG, graphPrefix, //LastUniverseIndexFromPrefix
                                                Constants.END_OF_TIME, Constants.NULL_LONG, graphPrefix, //LastObjectIndexFromPrefix
                                                Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, //GlobalUniverseTree
                                                Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2] //Global dictionary
                                        };
                                        selfPointer._storage.get(connectionKeys, new KCallback<String[]>() {
                                            @Override
                                            public void on(String[] strings) {
                                                if (strings.length == 4) {
                                                    Boolean noError = true;
                                                    try {
                                                        String uniIndexPayload = strings[UNIVERSE_INDEX];
                                                        if (uniIndexPayload == null || PrimitiveHelper.equals(uniIndexPayload, "")) {
                                                            uniIndexPayload = "0";
                                                        }
                                                        String objIndexPayload = strings[OBJ_INDEX];
                                                        if (objIndexPayload == null || PrimitiveHelper.equals(objIndexPayload, "")) {
                                                            objIndexPayload = "0";
                                                        }

                                                        //init the global universe tree (mandatory for synchronious create)
                                                        KWorldOrderChunk globalWorldOrder = (KWorldOrderChunk) selfPointer._space.create(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, Constants.WORLD_ORDER_CHUNK, strings[GLO_TREE_INDEX], null);
                                                        selfPointer._space.putAndMark(globalWorldOrder);

                                                        //init the global dictionary chunk
                                                        KStateChunk globalDictionaryChunk = (KStateChunk) selfPointer._space.create(Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2], Constants.STATE_CHUNK, strings[GLO_DIC_INDEX], null);
                                                        selfPointer._space.putAndMark(globalDictionaryChunk);

                                                        //TODO call the manager
                                                        long newUniIndex = PrimitiveHelper.parseLong(uniIndexPayload);
                                                        long newObjIndex = PrimitiveHelper.parseLong(objIndexPayload);
                                                        selfPointer._universeKeyCalculator = new KeyCalculator(graphPrefix, newUniIndex);
                                                        selfPointer._objectKeyCalculator = new KeyCalculator(graphPrefix, newObjIndex);

                                                        //init the resolver
                                                        selfPointer._resolver.init();

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
                        selfPointer._lock.set(true);
                        if (PrimitiveHelper.isDefined(callback)) {
                            callback.on(null);
                        }
                    }
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
            save(new KCallback<Throwable>() {
                @Override
                public void on(Throwable throwable) {
                    //TODO maybe change to asynchronous code
                    selfPointer._scheduler.stop();
                    //_blas.disconnect();
                    if (selfPointer._storage != null) {
                        selfPointer._storage.disconnect(new KCallback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                selfPointer._lock.set(true);
                                if (PrimitiveHelper.isDefined(callback)) {
                                    callback.on(null);
                                }
                            }
                        });
                    } else {
                        selfPointer._lock.set(true);
                        if (PrimitiveHelper.isDefined(callback)) {
                            callback.on(null);
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

    private void saveDirtyList(final KChunkIterator dirtyIterator, final KCallback<Boolean> callback) {
        if (dirtyIterator.size() == 0) {
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            long sizeToSaveKeys = (dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE) * Constants.KEYS_SIZE;
            long[] toSaveKeys = new long[(int) sizeToSaveKeys];
            long sizeToSaveValues = dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE;
            String[] toSaveValues = new String[(int) sizeToSaveValues];
            int i = 0;
            while (dirtyIterator.hasNext()) {
                KChunk loopChunk = dirtyIterator.next();
                if (loopChunk != null && (loopChunk.flags() & Constants.DIRTY_BIT) == Constants.DIRTY_BIT) {
                    toSaveKeys[i * Constants.KEYS_SIZE] = loopChunk.world();
                    toSaveKeys[i * Constants.KEYS_SIZE + 1] = loopChunk.time();
                    toSaveKeys[i * Constants.KEYS_SIZE + 2] = loopChunk.id();
                    try {
                        toSaveValues[i] = loopChunk.save();
                        this._space.declareClean(loopChunk);
                        i++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            toSaveKeys[i * Constants.KEYS_SIZE] = Constants.BEGINNING_OF_TIME;
            toSaveKeys[i * Constants.KEYS_SIZE + 1] = Constants.NULL_LONG;
            toSaveKeys[i * Constants.KEYS_SIZE + 2] = this._objectKeyCalculator.prefix();
            toSaveValues[i] = "" + this._objectKeyCalculator.lastComputedIndex();
            i++;
            toSaveKeys[i * Constants.KEYS_SIZE] = Constants.END_OF_TIME;
            toSaveKeys[i * Constants.KEYS_SIZE + 1] = Constants.NULL_LONG;
            toSaveKeys[i * Constants.KEYS_SIZE + 2] = this._universeKeyCalculator.prefix();
            toSaveValues[i] = "" + this._universeKeyCalculator.lastComputedIndex();

            //shrink in case of i != full size
            if (i != sizeToSaveValues - 1) {
                //shrinkValue
                String[] toSaveValuesShrinked = new String[i + 1];
                System.arraycopy(toSaveValues, 0, toSaveValuesShrinked, 0, i + 1);
                toSaveValues = toSaveValuesShrinked;

                long[] toSaveKeysShrinked = new long[(i + 1) * Constants.KEYS_SIZE];
                System.arraycopy(toSaveKeys, 0, toSaveKeysShrinked, 0, (i + 1) * Constants.KEYS_SIZE);
                toSaveKeys = toSaveKeysShrinked;
            }
            this._storage.put(toSaveKeys, toSaveValues, callback, -1);
        }
    }

    @Override
    public void index(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback<Boolean> callback) {
        getIndexOrCreate(toIndexNode.world(), toIndexNode.time(), indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode result) {
                result.index(Constants.INDEX_ATTRIBUTE, toIndexNode, keyAttributes, callback);
            }
        }, true);
    }

    @Override
    public void find(long world, long time, String indexName, String query, KCallback<KNode> callback) {
        getIndexOrCreate(world, time, indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode result) {
                if (result == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(null);
                    }
                } else {
                    result.find(Constants.INDEX_ATTRIBUTE, query, callback);
                }
            }
        }, false);
    }

    @Override
    public void all(long world, long time, String indexName, KCallback<KNode[]> callback) {
        getIndexOrCreate(world, time, indexName, new KCallback<KNode>() {
            @Override
            public void on(KNode result) {
                if (result == null) {
                    if (PrimitiveHelper.isDefined(callback)) {
                        callback.on(new KNode[0]);
                    }
                } else {
                    result.all(Constants.INDEX_ATTRIBUTE, callback);
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
                        KNode globalIndexNode = new Node(world, time, Constants.END_OF_TIME, selfPointer._resolver, world, time, Constants.NULL_LONG, Constants.NULL_LONG);
                        selfPointer._resolver.initNode(globalIndexNode);
                        globalIndexContent = (KLongLongMap) globalIndexNode.attMap(Constants.INDEX_ATTRIBUTE, KType.LONG_LONG_MAP);
                    } else {
                        globalIndexContent = (KLongLongMap) globalIndexNodeUnsafe.att(Constants.INDEX_ATTRIBUTE);
                    }
                    long indexId = globalIndexContent.get(indexNameCoded);
                    if (indexId == Constants.NULL_LONG) {
                        if (createIfNull) {
                            //insert null
                            KNode newIndexNode = newNode(0, 0);
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

    public KStorage storage() {
        return this._storage;
    }

    public KChunkSpace space() {
        return this._space;
    }

}
