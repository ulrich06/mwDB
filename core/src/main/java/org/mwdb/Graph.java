package org.mwdb;

import org.mwdb.manager.KeyCalculator;
import org.mwdb.manager.MWGResolver;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.chunk.*;
import org.mwdb.utility.DeferCounter;
import org.mwdb.utility.PrimitiveHelper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    public KNode createNode(long world, long time) {
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
    public void lookupAllTimes(long world, long[] times, long id, KCallback<KNode[]> callback) {
        if (!_isConnected.get()) {
            throw new RuntimeException(Constants.DISCONNECTED_ERROR);
        }
    }

    @Override
    public void save(KCallback callback) {
        KChunkIterator dirtyIterator = this._space.detachDirties();
        saveDirtyList(dirtyIterator, callback);
    }

    @Override
    public void connect(KCallback callback) {
        //negociate a lock
        while (this._lock.compareAndSet(false, true)) ;
        //ok we have it, let's go
        if (_isConnected.compareAndSet(false, true)) {
            //first connect the scheduler
            this._scheduler.start();
            final Graph selfPointer = this;
            this._storage.connect(new KCallback<Throwable>() {
                @Override
                public void on(Throwable throwable) {
                    if (throwable == null) {
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
                                                    Exception detected = null;
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
                                                        KWorldOrderChunk globalWorldOrder = (KWorldOrderChunk) selfPointer._space.create(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, Constants.WORLD_ORDER_CHUNK);
                                                        globalWorldOrder.load(strings[GLO_TREE_INDEX]);
                                                        selfPointer._space.putAndMark(globalWorldOrder);

                                                        //init the global dictionary chunk
                                                        KStateChunk globalDictionaryChunk = (KStateChunk) selfPointer._space.create(Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2], Constants.STATE_CHUNK);
                                                        globalDictionaryChunk.load(strings[GLO_DIC_INDEX]);
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
                                                        detected = e;
                                                    }
                                                    selfPointer._lock.set(true);
                                                    if (PrimitiveHelper.isDefined(callback)) {
                                                        callback.on(detected);
                                                    }
                                                } else {
                                                    selfPointer._lock.set(true);
                                                    if (PrimitiveHelper.isDefined(callback)) {
                                                        callback.on(new Exception("Error while connecting the KDataStore..."));
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
                        selfPointer._storage.disconnect(new KCallback<Throwable>() {
                            @Override
                            public void on(Throwable throwable) {
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

    private void saveDirtyList(final KChunkIterator dirtyIterator, final KCallback<Throwable> callback) {
        if (dirtyIterator.size() == 0) {
            if (PrimitiveHelper.isDefined(callback)) {
                callback.on(null);
            }
        } else {
            int sizeToSaveKeys = (dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE) * Constants.KEYS_SIZE;
            long[] toSaveKeys = new long[sizeToSaveKeys];
            int sizeToSaveValues = dirtyIterator.size() + Constants.PREFIX_TO_SAVE_SIZE;
            String[] toSaveValues = new String[sizeToSaveValues];
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
    public void index(String indexName, KNode toIndexNode, String[] keyAttributes, KCallback callback) {
        long indexNameCoded = this._resolver.key(indexName);

    }

    @Override
    public void find(String indexName, KNode toIndexNode, String query, KCallback<KNode> callback) {
        long indexNameCoded = this._resolver.key(indexName);
        this._resolver.lookup(toIndexNode.world(), toIndexNode.time(), Constants.END_OF_TIME, new KCallback<KNode>() {
            @Override
            public void on(KNode globalIndexNode) {
                //TODO, update later state of index if exists

                //TODO
            }
        });
    }

    @Override
    public void all(long world, long time, String indexName, KCallback<KNode[]> callback) {
        final Graph selfPointer = this;
        final long indexNameCoded = this._resolver.key(indexName);
        this._resolver.lookup(world, time, Constants.END_OF_TIME, new KCallback<KNode>() {
            @Override
            public void on(KNode globalIndexNode) {
                if (globalIndexNode == null) {
                    callback.on(new KNode[0]);
                } else {
                    KLongLongMap globalIndexContent = (KLongLongMap) globalIndexNode.att(Constants.INDEX_ATTRIBUTE);
                    final long indexId = globalIndexContent.get(indexNameCoded);
                    selfPointer._resolver.lookup(world, time, indexId, new KCallback<KNode>() {
                        @Override
                        public void on(KNode namedIndex) {
                            if (namedIndex == null) {
                                callback.on(new KNode[0]);
                            } else {
                                KLongLongArrayMap namedIndexContent = (KLongLongArrayMap) globalIndexNode.att(Constants.INDEX_ATTRIBUTE);
                                final KNode[] resolved = new KNode[namedIndexContent.size()];
                                DeferCounter waiter = new DeferCounter(namedIndexContent.size());
                                //TODO replace by a parralel lookup
                                final AtomicInteger loopInteger = new AtomicInteger(-1);
                                namedIndexContent.each(new KLongLongArrayMapCallBack() {
                                    @Override
                                    public void on(final long hash, final long nodeId) {
                                        selfPointer._resolver.lookup(world, time, nodeId, new KCallback<KNode>() {
                                            @Override
                                            public void on(KNode resolvedNode) {
                                                resolved[loopInteger.incrementAndGet()] = resolvedNode;
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
                            }
                        }
                    });
                }
            }
        });
    }

    /*
    private long buildHash(String ){
        int iParam = 0;
        int lastStart = iParam;
        while (iParam < p_paramString.length()) {
            if (p_paramString.charAt(iParam) == QueryEngine.VALS_SEP) {
                String p = p_paramString.substring(lastStart, iParam).trim();
                if (!PrimitiveHelper.equals(p, "")) {
                    String[] pArray = p.split(QueryEngine.VAL_SEP);
                    if (pArray.length > 1) {
                        params.put(pArray[0].trim(), pArray[1].trim());
                    }
                }
                lastStart = iParam + 1;
            }
            iParam = iParam + 1;
        }
        String lastParam = p_paramString.substring(lastStart, iParam).trim();
        if (!PrimitiveHelper.equals(lastParam, "")) {
            String[] pArray = lastParam.split(QueryEngine.VAL_SEP);
            if (pArray.length > 1) {
                params.put(pArray[0].trim(), pArray[1].trim());
            }
        }
        return params;
    }*/

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
