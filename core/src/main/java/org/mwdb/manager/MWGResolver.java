package org.mwdb.manager;

import org.mwdb.*;
import org.mwdb.chunk.*;
import org.mwdb.plugin.KNodeState;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.utility.PrimitiveHelper;

public class MWGResolver implements KResolver {

    private final KStorage _storage;

    private final KChunkSpace _space;

    private final KNodeTracker _tracker;

    private final KScheduler _scheduler;

    private static final String deadNodeError = "This Node has been tagged destroyed, please don't use it anymore!";

    public MWGResolver(KStorage p_storage, KChunkSpace p_space, KNodeTracker p_tracker, KScheduler p_scheduler) {
        this._storage = p_storage;
        this._space = p_space;
        this._tracker = p_tracker;
        this._scheduler = p_scheduler;
    }

    private KIndexStateChunk dictionary;

    @Override
    public void init() {
        dictionary = (KIndexStateChunk) this._space.getAndMark(Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2]);
    }

    @Override
    public void initNode(KNode node) {
        /*
        short chunkType = KChunkTypes.OBJECT_CHUNK;
        if (metaClassIndex == MetaClassIndex.INSTANCE.index()) {
            chunkType = KChunkTypes.OBJECT_CHUNK_INDEX;
        }*/

        KStateChunk cacheEntry = (KStateChunk) this._space.create(node.world(), node.time(), node.id(), Constants.STATE_CHUNK);
        cacheEntry.init(null);
        cacheEntry.setFlags(Constants.DIRTY_BIT, 0);
        //put and mark
        this._space.putAndMark(cacheEntry);
        //declare dirty now because potentially no insert could be done
        this._space.declareDirty(cacheEntry);

        //initiate time management
        KLongTree timeTree = (KLongTree) this._space.create(node.world(), Constants.NULL_LONG, node.id(), Constants.LONG_TREE);
        timeTree.init(null);
        this._space.putAndMark(timeTree);
        timeTree.insertKey(node.time());

        //initiate universe management
        KLongLongMap objectWorldOrder = (KLongLongMap) this._space.create(Constants.NULL_LONG, Constants.NULL_LONG, node.id(), Constants.LONG_LONG_MAP);
        objectWorldOrder.init(null);
        this._space.putAndMark(objectWorldOrder);
        objectWorldOrder.put(node.world(), node.time());
        //mark the global
        this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        //monitor the node object
        this._tracker.monitor(node);
    }

    @Override
    public void freeNode(KNode node) {
        Node casted = (Node) node;
        long nodeId = node.id();
        long[] previous;
        do {
            previous = casted._previousResolveds.get();
        } while (!casted._previousResolveds.compareAndSet(previous, null));
        if (previous != null) {
            this._space.unmark(previous[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previous[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);//FREE OBJECT CHUNK
            this._space.unmark(previous[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);//FREE TIME TREE
            this._space.unmark(Constants.NULL_LONG, Constants.NULL_LONG, nodeId); //FREE OBJECT UNIVERSE MAP
            this._space.unmark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG); //FREE GLOBAL UNIVERSE MAP
        }
    }

    @Override
    public void lookup(long world, long time, long id, KCallback<KNode> callback) {
        this._scheduler.dispatch(lookupTask(world, time, id, callback));
    }

    @Override
    public KCallback lookupTask(final long world, final long time, final long id, final KCallback<KNode> callback) {
        final MWGResolver selfPointer = this;
        return new KCallback() {
            @Override
            public void on(Object o) {
                try {
                    selfPointer.getOrLoadAndMark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, new KCallback<KChunk>() {
                        @Override
                        public void on(KChunk theGlobalUniverseOrderElement) {
                            if (theGlobalUniverseOrderElement != null) {
                                selfPointer.getOrLoadAndMark(Constants.NULL_LONG, Constants.NULL_LONG, id, new KCallback<KChunk>() {
                                    @Override
                                    public void on(KChunk theObjectUniverseOrderElement) {
                                        if (theObjectUniverseOrderElement == null) {
                                            selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                            callback.on(null);
                                        } else {
                                            final long closestUniverse = resolve_universe((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) theObjectUniverseOrderElement, time, world);
                                            selfPointer.getOrLoadAndMark(closestUniverse, Constants.NULL_LONG, id, new KCallback<KChunk>() {
                                                @Override
                                                public void on(KChunk theObjectTimeTreeElement) {
                                                    if (theObjectTimeTreeElement == null) {
                                                        selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                        selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                        callback.on(null);
                                                    } else {
                                                        long closestTime = ((KLongTree) theObjectTimeTreeElement).previousOrEqual(time);
                                                        if (closestTime == Constants.NULL_LONG) {
                                                            selfPointer._space.unmarkChunk(theObjectTimeTreeElement);
                                                            selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                            selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                            callback.on(null);
                                                            return;
                                                        }
                                                        selfPointer.getOrLoadAndMark(closestUniverse, closestTime, id, new KCallback<KChunk>() {
                                                            @Override
                                                            public void on(KChunk theObjectChunk) {
                                                                if (theObjectChunk == null) {
                                                                    selfPointer._space.unmarkChunk(theObjectTimeTreeElement);
                                                                    selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                                    selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                                    callback.on(null);
                                                                } else {
                                                                    Node newNode = new Node(world, time, id, selfPointer, closestUniverse, closestTime, ((KLongLongMap) theObjectUniverseOrderElement).magic(), ((KLongTree) theObjectTimeTreeElement).magic());
                                                                    selfPointer._tracker.monitor(newNode);
                                                                    callback.on(newNode);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                callback.on(null);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private long resolve_universe(final KLongLongMap globalWorldOrder, final KLongLongMap nodeWorldOrder, final long timeToResolve, long originWorld) {
        if (globalWorldOrder == null || nodeWorldOrder == null) {
            return originWorld;
        }
        long currentUniverse = originWorld;
        long previousUniverse = Constants.NULL_LONG;
        long divergenceTime = nodeWorldOrder.get(currentUniverse);
        while (currentUniverse != previousUniverse) {
            //check range
            if (divergenceTime != Constants.NULL_LONG && divergenceTime <= timeToResolve) {
                return currentUniverse;
            }
            //next round
            previousUniverse = currentUniverse;
            currentUniverse = globalWorldOrder.get(currentUniverse);
            divergenceTime = nodeWorldOrder.get(currentUniverse);
        }
        return originWorld;
    }

    private void getOrLoadAndMark(final long world, final long time, final long id, final KCallback<KChunk> callback) {
        if (world == Constants.NULL_KEY[0] && time == Constants.NULL_KEY[1] && id == Constants.NULL_KEY[2]) {
            callback.on(null);
            return;
        }
        KChunk cached = this._space.getAndMark(world, time, id);
        if (cached != null) {
            callback.on(cached);
        } else {
            load(new long[]{world, time, id}, new KCallback<KChunk[]>() {
                @Override
                public void on(KChunk[] loadedElements) {
                    callback.on(loadedElements[0]);
                }
            });
        }
    }

    private void getOrLoadAndMarkAll(long[] keys, final KCallback<KChunk[]> callback) {
        int nbKeys = keys.length / Constants.KEYS_SIZE;
        final boolean[] toLoadIndexes = new boolean[nbKeys];
        int nbElem = 0;
        final KChunk[] result = new KChunk[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            if (keys[i * Constants.KEYS_SIZE] == Constants.NULL_KEY[0] && keys[i * Constants.KEYS_SIZE + 1] == Constants.NULL_KEY[1] && keys[i * Constants.KEYS_SIZE + 2] == Constants.NULL_KEY[2]) {
                toLoadIndexes[i] = false;
                result[i] = null;
            } else {
                result[i] = this._space.getAndMark(keys[i * Constants.KEYS_SIZE], keys[i * Constants.KEYS_SIZE + 1], keys[i * Constants.KEYS_SIZE + 2]);
                if (result[i] == null) {
                    toLoadIndexes[i] = true;
                    nbElem++;
                } else {
                    toLoadIndexes[i] = false;
                }
            }
        }
        if (nbElem == 0) {
            callback.on(result);
        } else {
            long[] keysToLoad = new long[nbElem * 3];
            int lastInsertedIndex = 0;
            for (int i = 0; i < nbKeys; i++) {
                if (toLoadIndexes[i]) {
                    keysToLoad[lastInsertedIndex] = keys[i * Constants.KEYS_SIZE];
                    lastInsertedIndex++;
                    keysToLoad[lastInsertedIndex] = keys[i * Constants.KEYS_SIZE + 1];
                    lastInsertedIndex++;
                    keysToLoad[lastInsertedIndex] = keys[i * Constants.KEYS_SIZE + 2];
                    lastInsertedIndex++;
                }
            }
            load(keysToLoad, new KCallback<KChunk[]>() {
                @Override
                public void on(KChunk[] loadedElements) {
                    int currentIndexToMerge = 0;
                    for (int i = 0; i < nbKeys; i++) {
                        if (toLoadIndexes[i]) {
                            result[i] = loadedElements[currentIndexToMerge];
                            currentIndexToMerge++;
                        }
                    }
                    callback.on(result);
                }
            });
        }
    }

    private void load(long[] keys, KCallback<KChunk[]> callback) {
        MWGResolver selfPointer = this;
        this._storage.get(keys, new KCallback<String[]>() {
            @Override
            public void on(String[] payloads) {
                KChunk[] results = new KChunk[keys.length / 3];
                for (int i = 0; i < payloads.length; i++) {
                    long loopWorld = keys[i * 3];
                    long loopTime = keys[i * 3 + 1];
                    long loopUuid = keys[i * 3 + 2];
                    short elemType;
                    if (loopWorld == Constants.NULL_LONG) {
                        elemType = Constants.LONG_LONG_MAP;
                    } else {
                        if (loopTime == Constants.NULL_LONG) {
                            elemType = Constants.LONG_TREE;
                        } else {
                            if (payloads[i] == null || payloads[i].length() < 1) {
                                elemType = Constants.STATE_CHUNK;
                            } else {
                                char flag = payloads[i].charAt(0);
                                if (flag == '#') {
                                    elemType = Constants.INDEX_STATE_CHUNK;
                                } else {
                                    elemType = Constants.STATE_CHUNK;
                                }
                            }
                        }
                    }
                    results[i] = selfPointer._space.create(loopWorld, loopTime, loopUuid, elemType);
                    results[i].init(payloads[i]);
                    selfPointer._space.putAndMark(results[i]);
                }
                callback.on(results);
            }
        });
    }


    @Override
    public KNodeState resolveState(KNode node, boolean allowDephasing) {
        Node castedNode = (Node) node;
        //protection against deleted KNode
        long[] previousResolveds = castedNode._previousResolveds.get();
        if (previousResolveds == null) {
            throw new RuntimeException(deadNodeError);
        }
        //let's go for the resolution now
        long nodeWorld = node.world();
        long nodeTime = node.time();
        long nodeId = node.id();

        //OPTIMIZATION #1: NO DEPHASING
        if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] == nodeWorld && previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX] == nodeTime) {
            KStateChunk currentEntry = (KStateChunk) this._space.getAndMark(nodeWorld, nodeTime, nodeId);
            if (currentEntry != null) {
                this._space.unmarkChunk(currentEntry);
                return currentEntry;
            }
        }

        //Retrieve Node needed chunks
        KLongLongMap objectUniverseMap = (KLongLongMap) this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, nodeId);
        if (objectUniverseMap == null) {
            return null;
        }
        KLongTree objectTimeTree = (KLongTree) this._space.getAndMark(previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);
        if (objectTimeTree == null) {
            this._space.unmarkChunk(objectUniverseMap);
            return null;
        }
        long objectUniverseMapMagic = objectUniverseMap.magic();
        long objectTimeTreeMagic = objectTimeTree.magic();

        //OPTIMIZATION #2: SAME DEPHASING
        if (allowDephasing && previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] == objectUniverseMapMagic && previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] == objectTimeTreeMagic) {
            KStateChunk currentEntry = (KStateChunk) this._space.getAndMark(previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
            this._space.unmarkChunk(objectUniverseMap);
            this._space.unmarkChunk(objectTimeTree);
            if (currentEntry != null) {
                //ERROR case protection, chunk has been removed from cache
                this._space.unmarkChunk(currentEntry);
            }
            return currentEntry;
        }

        //NOMINAL CASE, MAGIC NUMBER ARE NOT VALID ANYMORE
        KLongLongMap globalUniverseTree = (KLongLongMap) this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        if (globalUniverseTree == null) {
            this._space.unmarkChunk(objectUniverseMap);
            this._space.unmarkChunk(objectTimeTree);
            return null;
        }

        //SOMETHING WILL MOVE HERE ANYWAY SO WE SYNC THE OBJECT
        /*
        int magic;
        do {
            magic = random.nextInt();
        } while (!objectUniverseMap.tokenCompareAndSwap(-1, magic));
        */

        //OK NOW WE HAVE THE MAGIC FOR UUID
        try {
            long resolvedWorld = resolve_universe(globalUniverseTree, objectUniverseMap, nodeTime, nodeWorld);
            long resolvedTime = objectTimeTree.previousOrEqual(nodeTime);
            if (resolvedWorld != Constants.NULL_LONG && resolvedTime != Constants.NULL_LONG) {
                if (allowDephasing) {
                    KStateChunk newObjectEntry = (KStateChunk) this._space.getAndMark(resolvedWorld, resolvedTime, nodeId);
                    long[] current;
                    boolean diff = false;
                    do {
                        previousResolveds = castedNode._previousResolveds.get();
                        if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] != resolvedWorld || previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX] != resolvedTime) {
                            current = new long[]{resolvedWorld, resolvedTime, objectUniverseMapMagic, objectTimeTreeMagic};
                            diff = true;
                        } else {
                            current = previousResolveds;
                        }
                    } while (!castedNode._previousResolveds.compareAndSet(previousResolveds, current));
                    if (diff) {
                        //unmark previous
                        this._space.unmark(previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
                    } else {
                        //concurrent usage of the same Node object, somebody else has already aligned the object, nothing to do, remove unnecessary mark
                        this._space.unmarkChunk(newObjectEntry);
                    }
                    //in all the case free tree and map
                    this._space.unmarkChunk(objectTimeTree);
                    this._space.unmarkChunk(globalUniverseTree);
                    this._space.unmarkChunk(objectUniverseMap);
                    /*
                    //free lock
                    if (!objectUniverseMap.tokenCompareAndSwap(magic, -1)) {
                        throw new RuntimeException("BadCompareAndSwap");
                    }
                    */
                    return newObjectEntry;
                } else {
                    long[] current;
                    boolean diff = false;
                    do {
                        previousResolveds = castedNode._previousResolveds.get();
                        if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] != nodeWorld || previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX] != nodeTime) {
                            //universeMap and objectTree magic numbers are set to null
                            current = new long[]{nodeWorld, nodeTime, Constants.NULL_LONG, Constants.NULL_LONG};
                            diff = true;
                        } else {
                            current = previousResolveds;
                        }
                    } while (!castedNode._previousResolveds.compareAndSet(previousResolveds, current));
                    if (diff) {
                        KStateChunk currentEntry = (KStateChunk) this._space.getAndMark(previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
                        //clone the chunk
                        KStateChunk clonedChunk = (KStateChunk) this._space.create(nodeWorld, nodeTime, nodeId, Constants.STATE_CHUNK);
                        clonedChunk.cloneFrom(currentEntry);
                        this._space.putAndMark(clonedChunk);
                        clonedChunk.setFlags(Constants.DIRTY_BIT, 0);
                        this._space.declareDirty(clonedChunk);

                        if (resolvedWorld == nodeWorld) {
                            objectTimeTree.insertKey(nodeTime);
                        } else {
                            KLongTree newTemporalTree = (KLongTree) this._space.create(nodeWorld, Constants.NULL_LONG, nodeId, Constants.LONG_TREE);
                            this._space.putAndMark(newTemporalTree);
                            newTemporalTree.insertKey(nodeTime);
                            //unmark the previous time tree, now we have switched to the new one
                            this._space.unmarkChunk(objectTimeTree);
                            objectUniverseMap.put(nodeWorld, nodeTime);
                        }
                        //double unMarking, because, we should not use anymore this object
                        this._space.unmarkChunk(currentEntry);
                        this._space.unmarkChunk(currentEntry);
                        //free the rest of used object
                        this._space.unmarkChunk(objectTimeTree);
                        this._space.unmarkChunk(objectUniverseMap);
                        this._space.unmarkChunk(globalUniverseTree);
                        /*
                        //free lock
                        if (!objectUniverseMap.tokenCompareAndSwap(magic, -1)) {
                            throw new RuntimeException("BadCompareAndSwap");
                        }
                        */
                        return clonedChunk;
                    } else {
                        //somebody as clone for us, now waiting for the chunk to be available
                        this._space.unmarkChunk(objectTimeTree);
                        this._space.unmarkChunk(objectUniverseMap);
                        this._space.unmarkChunk(globalUniverseTree);
                        KStateChunk waitingChunk = (KStateChunk) this._space.getAndMark(nodeWorld, nodeTime, nodeId);
                        this._space.unmarkChunk(waitingChunk);
                        /*
                        //free lock
                        if (!objectUniverseMap.tokenCompareAndSwap(magic, -1)) {
                            throw new RuntimeException("BadCompareAndSwap");
                        }
                        */
                        return waitingChunk;
                    }
                }
            } else {
                this._space.unmarkChunk(objectTimeTree);
                this._space.unmarkChunk(globalUniverseTree);
                this._space.unmarkChunk(objectUniverseMap);
                /*
                //free lock
                if (!objectUniverseMap.tokenCompareAndSwap(magic, -1)) {
                    throw new RuntimeException("BadCompareAndSwap");
                }*/
                return null;
            }

        } catch (Throwable r) {
            //free lock
            r.printStackTrace();
            /*
            if (!objectUniverseMap.tokenCompareAndSwap(magic, -1)) {
                throw new RuntimeException("BadCompareAndSwap");
            }*/
            return null;
        }
    }

    /**
     * Dictionary methods
     */
    @Override
    public long key(String name) {
        long encodedKey = this.dictionary.getValue(name);
        if (encodedKey == Constants.NULL_LONG) {
            this.dictionary.put(name, Constants.NULL_LONG);
            encodedKey = this.dictionary.getValue(name);
        }
        return encodedKey;
    }

    @Override
    public String value(long key) {
        //Need inverted dictionary, let's see if we need it!
        return this.dictionary.getKey(key);
    }

}
