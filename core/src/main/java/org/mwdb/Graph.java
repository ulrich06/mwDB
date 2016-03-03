package org.mwdb;

import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.KIndexStateChunk;
import org.mwdb.chunk.KLongLongMap;
import org.mwdb.manager.KeyCalculator;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
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

    private static final String disconnectError = "Please connect your graph, prior to any usage of it";

    protected Graph(KStorage p_storage, KChunkSpace p_space, KScheduler p_scheduler, KResolver p_resolver) {
        //subElements set
        this._storage = p_storage;
        this._space = p_space;
        this._scheduler = p_scheduler;
        this._resolver = p_resolver;
        //variables init
        this._isConnected = new AtomicBoolean(false);
        this._lock = new AtomicBoolean(false);
    }

    @Override
    public KNode createNode(long world, long time) {
        if (!_isConnected.get()) {
            throw new RuntimeException(disconnectError);
        }
        KNode newNode = new Node(world, time, _objectKeyCalculator.nextKey(), this._resolver, world, time, Constants.NULL_LONG, Constants.NULL_LONG);
        this._resolver.initNode(newNode);
        return newNode;
    }

    @Override
    public void lookup(long world, long time, long id, KCallback<KNode> callback) {
        if (!_isConnected.get()) {
            throw new RuntimeException(disconnectError);
        }
    }

    @Override
    public void lookupAllTimes(long world, long[] times, long id, KCallback<KNode[]> callback) {
        if (!_isConnected.get()) {
            throw new RuntimeException(disconnectError);
        }
    }

    @Override
    public void save(KCallback callback) {
        //TODO
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
                                                        KLongLongMap globalUniverseTree = (KLongLongMap) selfPointer._space.create(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, Constants.LONG_LONG_MAP);
                                                        globalUniverseTree.init(strings[GLO_TREE_INDEX]);
                                                        selfPointer._space.putAndMark(globalUniverseTree);

                                                        //init the global dictionary chunk
                                                        KIndexStateChunk globalDictionaryChunk = (KIndexStateChunk) selfPointer._space.create(Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2], Constants.INDEX_STATE_CHUNK);
                                                        globalDictionaryChunk.init(strings[GLO_DIC_INDEX]);
                                                        selfPointer._space.putAndMark(globalDictionaryChunk);

                                                        //TODO call the manager
                                                        long newUniIndex = PrimitiveHelper.parseLong(uniIndexPayload);
                                                        long newObjIndex = PrimitiveHelper.parseLong(objIndexPayload);
                                                        selfPointer._universeKeyCalculator = new KeyCalculator(graphPrefix, newUniIndex);
                                                        selfPointer._objectKeyCalculator = new KeyCalculator(graphPrefix, newObjIndex);

                                                        //init the resolver
                                                        selfPointer._resolver.init();

                                                    } catch (Exception e) {
                                                        //e.printStackTrace();
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
