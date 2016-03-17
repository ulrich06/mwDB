package org.mwdb.manager;

import org.mwdb.*;
import org.mwdb.chunk.heap.ArrayLongLongMap;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.chunk.*;

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

    private KStateChunk dictionary;

    @Override
    public void init() {
        dictionary = (KStateChunk) this._space.getAndMark(Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2]);
    }

    @Override
    public void initNode(KNode node) {
        KStateChunk cacheEntry = (KStateChunk) this._space.create(node.world(), node.time(), node.id(), Constants.STATE_CHUNK, null, null);
        //put and mark
        this._space.putAndMark(cacheEntry);
        //declare dirty now because potentially no insert could be done
        this._space.declareDirty(cacheEntry);

        //initiate time management
        KTimeTreeChunk timeTree = (KTimeTreeChunk) this._space.create(node.world(), Constants.NULL_LONG, node.id(), Constants.TIME_TREE_CHUNK, null, null);
        timeTree = (KTimeTreeChunk) this._space.putAndMark(timeTree);
        timeTree.insert(node.time());

        //initiate universe management
        KWorldOrderChunk objectWorldOrder = (KWorldOrderChunk) this._space.create(Constants.NULL_LONG, Constants.NULL_LONG, node.id(), Constants.WORLD_ORDER_CHUNK, null, null);
        objectWorldOrder = (KWorldOrderChunk) this._space.putAndMark(objectWorldOrder);
        objectWorldOrder.put(node.world(), node.time());
        //mark the global

        this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        //monitor the node object
        this._tracker.monitor(node);
    }

    @Override
    public void initWorld(long parentWorld, long childWorld) {
        KWorldOrderChunk worldOrder = (KWorldOrderChunk) this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        worldOrder.put(childWorld, parentWorld);
        this._space.unmarkChunk(worldOrder);
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
                                            final long closestUniverse = resolve_world((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) theObjectUniverseOrderElement, time, world);
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
                                                                    Node newNode = new Node(world, time, id, selfPointer, closestUniverse, closestTime, ((KWorldOrderChunk) theObjectUniverseOrderElement).magic(), ((KLongTree) theObjectTimeTreeElement).magic());
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

    private long resolve_world(final KLongLongMap globalWorldOrder, final KLongLongMap nodeWorldOrder, final long timeToResolve, long originWorld) {
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
        this._storage.get(keys, new KCallback<KStorage.KBuffer[]>() {
            @Override
            public void on(KStorage.KBuffer[] payloads) {
                KChunk[] results = new KChunk[keys.length / 3];
                for (int i = 0; i < payloads.length; i++) {
                    long loopWorld = keys[i * 3];
                    long loopTime = keys[i * 3 + 1];
                    long loopUuid = keys[i * 3 + 2];
                    byte elemType;
                    if (loopWorld == Constants.NULL_LONG) {
                        elemType = Constants.WORLD_ORDER_CHUNK;
                    } else {
                        if (loopTime == Constants.NULL_LONG) {
                            elemType = Constants.TIME_TREE_CHUNK;
                        } else {
                            if (payloads[i] == null || payloads[i].size() < 1) {
                                elemType = Constants.STATE_CHUNK;
                            } else {
                                elemType = Constants.STATE_CHUNK;
                            }
                        }
                    }
                    if (payloads[i] != null) {
                        results[i] = selfPointer._space.create(loopWorld, loopTime, loopUuid, elemType, payloads[i], null);
                        selfPointer._space.putAndMark(results[i]);
                    }
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
        KWorldOrderChunk objectUniverseMap = (KWorldOrderChunk) this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, nodeId);
        if (objectUniverseMap == null) {
            return null;
        }
        KTimeTreeChunk objectTimeTree = (KTimeTreeChunk) this._space.getAndMark(previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);
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
        KWorldOrderChunk globalUniverseTree = (KWorldOrderChunk) this._space.getAndMark(Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        if (globalUniverseTree == null) {
            this._space.unmarkChunk(objectUniverseMap);
            this._space.unmarkChunk(objectTimeTree);
            return null;
        }

        //SOMETHING WILL MOVE HERE ANYWAY SO WE SYNC THE OBJECT

        objectUniverseMap.lock();
        //OK NOW WE HAVE THE TOKEN globally FOR the node ID

        //REFRESH magics
        objectUniverseMapMagic = objectUniverseMap.magic();
        objectTimeTreeMagic = objectTimeTree.magic();
        try {
            long resolvedWorld;
            long resolvedTime;
            // OPTIMIZATION #3: SAME DEPHASING THAN BEFORE, DIRECTLY CLONE THE PREVIOUSLY RESOLVED TUPLE
            if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] == objectUniverseMapMagic && previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] == objectTimeTreeMagic) {
                resolvedWorld = previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX];
                resolvedTime = previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX];
            } else {
                resolvedWorld = resolve_world(globalUniverseTree, objectUniverseMap, nodeTime, nodeWorld);
                resolvedTime = objectTimeTree.previousOrEqual(nodeTime);
                System.err.println("WTF");
            }

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
                    //free lock
                    objectUniverseMap.unlock();
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
                        KStateChunk clonedChunk = (KStateChunk) this._space.create(nodeWorld, nodeTime, nodeId, Constants.STATE_CHUNK, null, currentEntry);

                        this._space.putAndMark(clonedChunk);
                        this._space.declareDirty(clonedChunk);

                        if (resolvedWorld == nodeWorld) {
                            objectTimeTree.insert(nodeTime);
                        } else {
                            KTimeTreeChunk newTemporalTree = (KTimeTreeChunk) this._space.create(nodeWorld, Constants.NULL_LONG, nodeId, Constants.TIME_TREE_CHUNK, null, null);
                            this._space.putAndMark(newTemporalTree);
                            newTemporalTree.insert(nodeTime);
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
                        //free lock
                        objectUniverseMap.unlock();
                        return clonedChunk;
                    } else {
                        //somebody as clone for us, now waiting for the chunk to be available
                        this._space.unmarkChunk(objectTimeTree);
                        this._space.unmarkChunk(objectUniverseMap);
                        this._space.unmarkChunk(globalUniverseTree);
                        KStateChunk waitingChunk = (KStateChunk) this._space.getAndMark(nodeWorld, nodeTime, nodeId);
                        this._space.unmarkChunk(waitingChunk);
                        //free lock
                        objectUniverseMap.unlock();
                        return waitingChunk;
                    }
                }
            } else {
                this._space.unmarkChunk(objectTimeTree);
                this._space.unmarkChunk(globalUniverseTree);
                this._space.unmarkChunk(objectUniverseMap);
                //free lock
                objectUniverseMap.unlock();
                return null;
            }

        } catch (Throwable r) {
            //free lock
            r.printStackTrace();
            objectUniverseMap.unlock();
            return null;
        }
    }

    @Override
    public void resolveTimepoints(final KNode node, final long beginningOfSearch, final long endOfSearch, final KCallback<long[]> callback) {
        final MWGResolver selfPointer = this;
        long[] keys = new long[]{
                Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG,
                Constants.NULL_LONG, Constants.NULL_LONG, node.id()
        };
        getOrLoadAndMarkAll(keys, new KCallback<KChunk[]>() {
            @Override
            public void on(KChunk[] orders) {
                if (orders == null || orders.length != 2) {
                    callback.on(new long[0]);
                    return;
                }
                final KWorldOrderChunk globalWorldOrder = (KWorldOrderChunk) orders[0];
                final KWorldOrderChunk objectWorldOrder = (KWorldOrderChunk) orders[1];
                //worlds collector
                final int[] collectionSize = {Constants.MAP_INITIAL_CAPACITY};
                final long[][] collectedWorlds = {new long[collectionSize[0]]};
                int collectedIndex = 0;

                long currentWorld = node.world();
                while (currentWorld != Constants.NULL_LONG) {
                    long divergenceTimepoint = objectWorldOrder.get(currentWorld);
                    if (divergenceTimepoint != Constants.NULL_LONG) {
                        if (divergenceTimepoint < beginningOfSearch) {
                            break;
                        } else if (divergenceTimepoint > endOfSearch) {
                            //next round, go to parent world
                            currentWorld = globalWorldOrder.get(currentWorld);
                        } else {
                            //that's fit, add to search
                            collectedWorlds[0][collectedIndex] = currentWorld;
                            collectedIndex++;
                            if (collectedIndex == collectionSize[0]) {
                                //reallocate
                                long[] temp_collectedWorlds = new long[collectionSize[0] * 2];
                                System.arraycopy(collectedWorlds[0], 0, temp_collectedWorlds, 0, collectionSize[0]);
                                collectedWorlds[0] = temp_collectedWorlds;
                                collectionSize[0] = collectionSize[0] * 2;
                            }
                            //go to parent
                            currentWorld = globalWorldOrder.get(currentWorld);
                        }
                    } else {
                        //go to parent
                        currentWorld = globalWorldOrder.get(currentWorld);
                    }
                }
                //create request concat keys
                int nbKeys = collectedIndex * 3;
                final long[] timeTreeKeys = new long[nbKeys];
                for (int i = 0; i < collectedIndex; i++) {
                    timeTreeKeys[i * 3] = collectedWorlds[0][i];
                    timeTreeKeys[i * 3 + 1] = Constants.NULL_LONG;
                    timeTreeKeys[i * 3 + 2] = node.id();
                }
                final int finalCollectedIndex = collectedIndex;
                final long[] finalCollectedWorlds = collectedWorlds[0];
                getOrLoadAndMarkAll(timeTreeKeys, new KCallback<KChunk[]>() {
                    @Override
                    public void on(final KChunk[] timeTrees) {
                        if (timeTrees == null) {
                            selfPointer._space.unmarkChunk(objectWorldOrder);
                            selfPointer._space.unmarkChunk(globalWorldOrder);
                            callback.on(new long[0]);
                        } else {
                            //time collector
                            final int[] timelineSize = {Constants.MAP_INITIAL_CAPACITY};
                            final long[][] timeline = {new long[timelineSize[0]]};
                            final int[] timeline_index = {0};
                            long previousDivergenceTime = endOfSearch;
                            for (int i = 0; i < finalCollectedIndex; i++) {
                                KTimeTreeChunk timeTree = (KTimeTreeChunk) timeTrees[i];
                                if (timeTree != null) {
                                    long currentDivergenceTime = objectWorldOrder.get(finalCollectedWorlds[i]);
                                    if (currentDivergenceTime < beginningOfSearch) {
                                        currentDivergenceTime = beginningOfSearch;
                                    }
                                    final long finalPreviousDivergenceTime = previousDivergenceTime;
                                    timeTree.range(currentDivergenceTime, previousDivergenceTime, new KTreeWalker() {
                                        @Override
                                        public void elem(long t) {
                                            timeline[0][timeline_index[0]] = t;
                                            timeline_index[0]++;
                                            if (timelineSize[0] == timeline_index[0]) {
                                                //reallocate
                                                long[] temp_timeline = new long[timelineSize[0] * 2];
                                                System.arraycopy(timeline[0], 0, temp_timeline, 0, timelineSize[0]);
                                                timeline[0] = temp_timeline;
                                                timelineSize[0] = timelineSize[0] * 2;
                                            }
                                        }
                                    });
                                    previousDivergenceTime = currentDivergenceTime;
                                }
                                selfPointer._space.unmarkChunk(timeTree);
                            }
                            if (timeline_index[0] != timelineSize[0]) {
                                long[] tempTimeline = new long[timeline_index[0]];
                                System.arraycopy(timeline[0], 0, tempTimeline, 0, timeline_index[0]);
                                timeline[0] = tempTimeline;
                            }
                            selfPointer._space.unmarkChunk(objectWorldOrder);
                            selfPointer._space.unmarkChunk(globalWorldOrder);
                            callback.on(timeline[0]);
                        }
                    }
                });
            }
        });
    }

    /**
     * Dictionary methods
     */
    @Override
    public long stringToLongKey(String name) {
        KStringLongMap dictionaryIndex = (KStringLongMap) this.dictionary.get(0);
        if (dictionaryIndex == null) {
            dictionaryIndex = (KStringLongMap) this.dictionary.getOrCreate(0, KType.STRING_LONG_MAP);
        }
        long encodedKey = dictionaryIndex.getValue(name);
        if (encodedKey == Constants.NULL_LONG) {
            dictionaryIndex.put(name, Constants.NULL_LONG);
            encodedKey = dictionaryIndex.getValue(name);
        }
        return encodedKey;
    }

    @Override
    public String longKeyToString(long key) {
        KStringLongMap dictionaryIndex = (KStringLongMap) this.dictionary.get(0);
        if (dictionaryIndex != null) {
            return dictionaryIndex.getKey(key);
        }
        return null;
    }

}
