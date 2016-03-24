package org.mwdb.manager;

import org.mwdb.*;
import org.mwdb.plugin.KResolver;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.chunk.*;
import org.mwdb.utility.Buffer;

public class MWGResolver implements KResolver {

    private final KStorage _storage;

    private final KChunkSpace _space;

    private final KNodeTracker _tracker;

    private final KScheduler _scheduler;

    private static final String deadNodeError = "This Node has been tagged destroyed, please don't use it anymore!";

    private KGraph _graph;

    public MWGResolver(KStorage p_storage, KChunkSpace p_space, KNodeTracker p_tracker, KScheduler p_scheduler) {
        this._storage = p_storage;
        this._space = p_space;
        this._tracker = p_tracker;
        this._scheduler = p_scheduler;
    }

    private KStateChunk dictionary;

    @Override
    public void init(KGraph graph) {
        _graph = graph;
        dictionary = (KStateChunk) this._space.getAndMark(Constants.STATE_CHUNK, Constants.GLOBAL_DICTIONARY_KEY[0], Constants.GLOBAL_DICTIONARY_KEY[1], Constants.GLOBAL_DICTIONARY_KEY[2]);
    }

    @Override
    public void initNode(KNode node) {
        KStateChunk cacheEntry = (KStateChunk) this._space.create(Constants.STATE_CHUNK, node.world(), node.time(), node.id(), null, null);
        //put and mark
        this._space.putAndMark(cacheEntry);
        //declare dirty now because potentially no insert could be done
        this._space.declareDirty(cacheEntry);

        //initiate superTime management
        KTimeTreeChunk superTimeTree = (KTimeTreeChunk) this._space.create(Constants.TIME_TREE_CHUNK, node.world(), Constants.NULL_LONG, node.id(), null, null);
        superTimeTree = (KTimeTreeChunk) this._space.putAndMark(superTimeTree);
        superTimeTree.insert(node.time());

        //initiate time management
        KTimeTreeChunk timeTree = (KTimeTreeChunk) this._space.create(Constants.TIME_TREE_CHUNK, node.world(), node.time(), node.id(), null, null);
        timeTree = (KTimeTreeChunk) this._space.putAndMark(timeTree);
        timeTree.insert(node.time());

        //initiate universe management
        KWorldOrderChunk objectWorldOrder = (KWorldOrderChunk) this._space.create(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, node.id(), null, null);
        objectWorldOrder = (KWorldOrderChunk) this._space.putAndMark(objectWorldOrder);
        objectWorldOrder.put(node.world(), node.time());
        //mark the global

        this._space.getAndMark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        //monitor the node object
        this._tracker.monitor(node);
    }

    @Override
    public void initWorld(long parentWorld, long childWorld) {
        KWorldOrderChunk worldOrder = (KWorldOrderChunk) this._space.getAndMark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
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
            this._space.unmark(Constants.STATE_CHUNK, previous[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previous[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);//FREE OBJECT CHUNK
            this._space.unmark(Constants.TIME_TREE_CHUNK, previous[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);//FREE TIME TREE
            this._space.unmark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, nodeId); //FREE OBJECT UNIVERSE MAP
            this._space.unmark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG); //FREE GLOBAL UNIVERSE MAP
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
                    selfPointer.getOrLoadAndMark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, new KCallback<KChunk>() {
                        @Override
                        public void on(KChunk theGlobalUniverseOrderElement) {
                            if (theGlobalUniverseOrderElement != null) {
                                selfPointer.getOrLoadAndMark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, id, new KCallback<KChunk>() {
                                    @Override
                                    public void on(KChunk theObjectUniverseOrderElement) {
                                        if (theObjectUniverseOrderElement == null) {
                                            selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                            callback.on(null);
                                        } else {
                                            final long closestUniverse = resolve_world((KLongLongMap) theGlobalUniverseOrderElement, (KLongLongMap) theObjectUniverseOrderElement, time, world);
                                            selfPointer.getOrLoadAndMark(Constants.TIME_TREE_CHUNK, closestUniverse, Constants.NULL_LONG, id, new KCallback<KChunk>() {
                                                @Override
                                                public void on(KChunk theObjectSuperTimeTreeElement) {
                                                    if (theObjectSuperTimeTreeElement == null) {
                                                        selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                        selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                        callback.on(null);
                                                    } else {
                                                        final long closestSuperTime = ((KLongTree) theObjectSuperTimeTreeElement).previousOrEqual(time);
                                                        if (closestSuperTime == Constants.NULL_LONG) {
                                                            selfPointer._space.unmarkChunk(theObjectSuperTimeTreeElement);
                                                            selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                            selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                            callback.on(null);
                                                            return;
                                                        }
                                                        selfPointer.getOrLoadAndMark(Constants.TIME_TREE_CHUNK, closestUniverse, closestSuperTime, id, new KCallback<KChunk>() {
                                                            @Override
                                                            public void on(KChunk theObjectTimeTreeElement) {
                                                                if (theObjectTimeTreeElement == null) {
                                                                    selfPointer._space.unmarkChunk(theObjectSuperTimeTreeElement);
                                                                    selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                                    selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                                    callback.on(null);
                                                                } else {
                                                                    long closestTime = ((KLongTree) theObjectTimeTreeElement).previousOrEqual(time);
                                                                    if (closestTime == Constants.NULL_LONG) {
                                                                        selfPointer._space.unmarkChunk(theObjectTimeTreeElement);
                                                                        selfPointer._space.unmarkChunk(theObjectSuperTimeTreeElement);
                                                                        selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                                        selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                                        callback.on(null);
                                                                        return;
                                                                    }
                                                                    selfPointer.getOrLoadAndMark(Constants.STATE_CHUNK, closestUniverse, closestTime, id, new KCallback<KChunk>() {
                                                                        @Override
                                                                        public void on(KChunk theObjectChunk) {
                                                                            if (theObjectChunk == null) {
                                                                                selfPointer._space.unmarkChunk(theObjectTimeTreeElement);
                                                                                selfPointer._space.unmarkChunk(theObjectSuperTimeTreeElement);
                                                                                selfPointer._space.unmarkChunk(theObjectUniverseOrderElement);
                                                                                selfPointer._space.unmarkChunk(theGlobalUniverseOrderElement);
                                                                                callback.on(null);
                                                                            } else {
                                                                                Node newNode = new Node(_graph, world, time, id, selfPointer, closestUniverse, closestSuperTime, closestTime, ((KWorldOrderChunk) theObjectUniverseOrderElement).magic(), ((KLongTree) theObjectSuperTimeTreeElement).magic(), ((KLongTree) theObjectTimeTreeElement).magic());
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

    private void getOrLoadAndMark(final byte type, final long world, final long time, final long id, final KCallback<KChunk> callback) {
        if (world == Constants.NULL_KEY[0] && time == Constants.NULL_KEY[1] && id == Constants.NULL_KEY[2]) {
            callback.on(null);
            return;
        }
        KChunk cached = this._space.getAndMark(type, world, time, id);
        if (cached != null) {
            callback.on(cached);
        } else {
            KBuffer buffer = _graph.newBuffer();
            Buffer.keyToBuffer(buffer, type, world, time, id);
            load(new byte[]{type}, new long[]{world, time, id}, new KBuffer[]{buffer}, new KCallback<KChunk[]>() {
                @Override
                public void on(KChunk[] loadedElements) {
                    callback.on(loadedElements[0]);
                }
            });
        }
    }

    private static int KEY_SIZE = 3;

    private void getOrLoadAndMarkAll(byte[] types, long[] keys, final KCallback<KChunk[]> callback) {
        int nbKeys = keys.length / KEY_SIZE;
        final boolean[] toLoadIndexes = new boolean[nbKeys];
        int nbElem = 0;
        final KChunk[] result = new KChunk[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            if (keys[i * KEY_SIZE] == Constants.NULL_KEY[0] && keys[i * KEY_SIZE + 1] == Constants.NULL_KEY[1] && keys[i * KEY_SIZE + 2] == Constants.NULL_KEY[2]) {
                toLoadIndexes[i] = false;
                result[i] = null;
            } else {
                result[i] = this._space.getAndMark(types[i], keys[i * KEY_SIZE], keys[i * KEY_SIZE + 1], keys[i * KEY_SIZE + 2]);
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
            final long[] keysToLoadFlat = new long[nbElem * KEY_SIZE];
            final KBuffer[] keysToLoad = new KBuffer[nbElem];
            final byte[] typesToLoad = new byte[nbElem];
            int lastInsertedIndex = 0;
            for (int i = 0; i < nbKeys; i++) {
                if (toLoadIndexes[i]) {
                    keysToLoadFlat[lastInsertedIndex] = keys[i * KEY_SIZE];
                    keysToLoadFlat[lastInsertedIndex + 1] = keys[i * KEY_SIZE + 1];
                    keysToLoadFlat[lastInsertedIndex + 2] = keys[i * KEY_SIZE + 2];
                    typesToLoad[lastInsertedIndex] = types[i];
                    keysToLoad[lastInsertedIndex] = _graph.newBuffer();
                    Buffer.keyToBuffer(keysToLoad[lastInsertedIndex], types[i], keys[i * KEY_SIZE], keys[i * KEY_SIZE + 1], keys[i * KEY_SIZE + 2]);
                    lastInsertedIndex = lastInsertedIndex + 3;
                }
            }
            load(typesToLoad, keysToLoadFlat, keysToLoad, new KCallback<KChunk[]>() {
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

    private void load(byte[] types, long[] flatKeys, KBuffer[] keys, KCallback<KChunk[]> callback) {
        MWGResolver selfPointer = this;
        this._storage.get(keys, new KCallback<KBuffer[]>() {
            @Override
            public void on(KBuffer[] payloads) {
                KChunk[] results = new KChunk[keys.length];
                for (int i = 0; i < payloads.length; i++) {
                    keys[i].free(); //free the temp KBuffer
                    long loopWorld = flatKeys[i * KEY_SIZE];
                    long loopTime = flatKeys[i * KEY_SIZE + 1];
                    long loopUuid = flatKeys[i * KEY_SIZE + 2];
                    byte elemType = types[i];
                    if (payloads[i] != null) {
                        results[i] = selfPointer._space.create(elemType, loopWorld, loopTime, loopUuid, payloads[i], null);
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
            KStateChunk currentEntry = (KStateChunk) this._space.getAndMark(Constants.STATE_CHUNK, nodeWorld, nodeTime, nodeId);
            if (currentEntry != null) {
                this._space.unmarkChunk(currentEntry);
                return currentEntry;
            }
        }

        //Retrieve Node needed chunks
        KWorldOrderChunk objectWorldMap = (KWorldOrderChunk) this._space.getAndMark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, nodeId);
        if (objectWorldMap == null) {
            return null;
        }
        KTimeTreeChunk objectSuperTimeTree = (KTimeTreeChunk) this._space.getAndMark(Constants.TIME_TREE_CHUNK, previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);
        if (objectSuperTimeTree == null) {
            this._space.unmarkChunk(objectWorldMap);
            return null;
        }
        KTimeTreeChunk objectTimeTree = (KTimeTreeChunk) this._space.getAndMark(Constants.TIME_TREE_CHUNK, previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX], nodeId);
        if (objectTimeTree == null) {
            this._space.unmarkChunk(objectSuperTimeTree);
            this._space.unmarkChunk(objectWorldMap);
            return null;
        }

        long objectWorldMapMagic = objectWorldMap.magic();
        long objectSuperTimeTreeMagic = objectSuperTimeTree.magic();
        long objectTimeTreeMagic = objectTimeTree.magic();

        //OPTIMIZATION #2: SAME DEPHASING
        if (allowDephasing && previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] == objectWorldMapMagic && previousResolveds[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] == objectSuperTimeTreeMagic && previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] == objectTimeTreeMagic) {
            KStateChunk currentEntry = (KStateChunk) this._space.getAndMark(Constants.STATE_CHUNK, previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
            this._space.unmarkChunk(objectWorldMap);
            this._space.unmarkChunk(objectSuperTimeTree);
            this._space.unmarkChunk(objectTimeTree);
            if (currentEntry != null) {
                //ERROR case protection, chunk has been removed from cache
                this._space.unmarkChunk(currentEntry);
            }
            return currentEntry;
        }

        //NOMINAL CASE, MAGIC NUMBER ARE NOT VALID ANYMORE
        KWorldOrderChunk globalWorldOrder = (KWorldOrderChunk) this._space.getAndMark(Constants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        if (globalWorldOrder == null) {
            this._space.unmarkChunk(objectWorldMap);
            this._space.unmarkChunk(objectSuperTimeTree);
            this._space.unmarkChunk(objectTimeTree);
            return null;
        }

        //SOMETHING WILL MOVE HERE ANYWAY SO WE SYNC THE OBJECT

        objectWorldMap.lock();
        //OK NOW WE HAVE THE TOKEN globally FOR the node ID

        //REFRESH all magics
        objectWorldMapMagic = objectWorldMap.magic();
        objectSuperTimeTreeMagic = objectSuperTimeTree.magic();
        objectTimeTreeMagic = objectTimeTree.magic();

        try {
            long resolvedWorld;
            long resolvedSuperTime;
            long resolvedTime;
            // OPTIMIZATION #3: SAME DEPHASING THAN BEFORE, DIRECTLY CLONE THE PREVIOUSLY RESOLVED TUPLE
            if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] == objectWorldMapMagic && previousResolveds[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] == objectSuperTimeTreeMagic && previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] == objectTimeTreeMagic) {
                resolvedWorld = previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX];
                resolvedSuperTime = previousResolveds[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX];
                resolvedTime = previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX];
            } else {
                resolvedWorld = resolve_world(globalWorldOrder, objectWorldMap, nodeTime, nodeWorld);
                if (resolvedWorld != previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX]) {
                    //we have to update the superTree
                    KTimeTreeChunk tempObjectTimeTree = (KTimeTreeChunk) this._space.getAndMark(Constants.TIME_TREE_CHUNK, resolvedWorld, Constants.NULL_LONG, nodeId);
                    _space.unmarkChunk(objectSuperTimeTree);
                    objectSuperTimeTree = tempObjectTimeTree;
                }
                //TODO detect potentially cache miss
                resolvedSuperTime = objectSuperTimeTree.previousOrEqual(nodeTime);
                if (previousResolveds[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] != resolvedSuperTime) {
                    //we have to update the timeTree
                    KTimeTreeChunk tempObjectTimeTree = (KTimeTreeChunk) this._space.getAndMark(Constants.TIME_TREE_CHUNK, previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], resolvedSuperTime, nodeId);
                    _space.unmarkChunk(objectTimeTree);
                    objectTimeTree = tempObjectTimeTree;
                }
                //TODO detect potentially cache miss
                resolvedTime = objectTimeTree.previousOrEqual(nodeTime);
            }
            if (resolvedWorld != Constants.NULL_LONG && resolvedSuperTime != Constants.NULL_LONG && resolvedTime != Constants.NULL_LONG) {
                if (allowDephasing) {
                    KStateChunk newObjectEntry = (KStateChunk) this._space.getAndMark(Constants.STATE_CHUNK, resolvedWorld, resolvedTime, nodeId);
                    long[] current;
                    boolean diff = false;
                    do {
                        previousResolveds = castedNode._previousResolveds.get();
                        if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] != resolvedWorld || previousResolveds[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] != resolvedSuperTime || previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX] != resolvedTime) {
                            current = new long[6];
                            //previously resolved
                            current[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] = resolvedWorld;
                            current[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = resolvedSuperTime;
                            current[Constants.PREVIOUS_RESOLVED_TIME_INDEX] = resolvedTime;
                            //previously magics
                            current[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] = objectWorldMapMagic;
                            current[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = objectSuperTimeTreeMagic;
                            current[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] = objectTimeTreeMagic;
                            diff = true;
                        } else {
                            current = previousResolveds;
                        }
                    } while (!castedNode._previousResolveds.compareAndSet(previousResolveds, current));
                    if (diff) {
                        //unmark previous
                        //TODO unmark previous super time tree

                        //TODO unmark previous time tree

                        this._space.unmark(Constants.STATE_CHUNK, previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
                    } else {
                        //concurrent usage of the same Node object, somebody else has already aligned the object, nothing to do, remove unnecessary mark
                        this._space.unmarkChunk(newObjectEntry);
                    }
                    //in all the case free tree and map
                    this._space.unmarkChunk(objectTimeTree);
                    this._space.unmarkChunk(objectSuperTimeTree);
                    this._space.unmarkChunk(objectWorldMap);
                    this._space.unmarkChunk(globalWorldOrder);
                    //free lock
                    objectWorldMap.unlock();
                    return newObjectEntry;
                } else {
                    long[] current;
                    boolean diff = false;
                    do {
                        previousResolveds = castedNode._previousResolveds.get();
                        if (previousResolveds[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] != nodeWorld || previousResolveds[Constants.PREVIOUS_RESOLVED_TIME_INDEX] != nodeTime) {
                            //universeMap and objectTree magic numbers are set to null
                            current = new long[6];
                            //previously resolved
                            current[Constants.PREVIOUS_RESOLVED_WORLD_INDEX] = nodeWorld;
                            current[Constants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = resolvedSuperTime;
                            current[Constants.PREVIOUS_RESOLVED_TIME_INDEX] = nodeTime;
                            //previously magics
                            current[Constants.PREVIOUS_RESOLVED_WORLD_MAGIC] = Constants.NULL_LONG;
                            current[Constants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = Constants.NULL_LONG;
                            current[Constants.PREVIOUS_RESOLVED_TIME_MAGIC] = Constants.NULL_LONG;
                            diff = true;
                        } else {
                            current = previousResolveds;
                        }
                    } while (!castedNode._previousResolveds.compareAndSet(previousResolveds, current));
                    if (diff) {
                        KStateChunk currentEntry = (KStateChunk) this._space.getAndMark(Constants.STATE_CHUNK, resolvedWorld, resolvedTime, nodeId);
                        //clone the chunk
                        KStateChunk clonedChunk = (KStateChunk) this._space.create(Constants.STATE_CHUNK, nodeWorld, nodeTime, nodeId, null, currentEntry);

                        //TODO unmark previous super time tree

                        //TODO unmark preivous time tree

                        this._space.putAndMark(clonedChunk);
                        this._space.declareDirty(clonedChunk);

                        if (resolvedWorld == nodeWorld) {
                            //manage super tree here
                            long superTreeSize = objectSuperTimeTree.size();
                            long threshold = Constants.SCALE_1;
                            if (superTreeSize > threshold) {
                                threshold = Constants.SCALE_2;
                            }
                            if (superTreeSize > threshold) {
                                threshold = Constants.SCALE_3;
                            }
                            if (superTreeSize > threshold) {
                                threshold = Constants.SCALE_4;
                            }
                            objectTimeTree.insert(nodeTime);
                            if (objectTimeTree.size() == threshold) {
                                //split in two
                                //extract the two

                                final long[] medianPoint = {-1};
                                //we iterate over the tree without boundaries for values, but with boundaries for number of collected times
                                objectTimeTree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, objectTimeTree.size() / 2, new KTreeWalker() {
                                    @Override
                                    public void elem(long t) {
                                        medianPoint[0] = t;
                                    }
                                });

                                KTimeTreeChunk rightTree = (KTimeTreeChunk) this._space.create(Constants.TIME_TREE_CHUNK, nodeWorld, medianPoint[0], nodeId, null, null);
                                this._space.putAndMark(rightTree);
                                //TODO second iterate that can be avoided, however we need the median point to create the right tree
                                //we iterate over the tree without boundaries for values, but with boundaries for number of collected times
                                objectTimeTree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, objectTimeTree.size() / 2, new KTreeWalker() {
                                    @Override
                                    public void elem(long t) {
                                        rightTree.insert(t);
                                    }
                                });
                                objectSuperTimeTree.insert(medianPoint[0]);
                                objectTimeTree.clearAt(medianPoint[0]);
                            }
                        } else {
                            //create a new super timeTree
                            KTimeTreeChunk newSuperTimeTree = (KTimeTreeChunk) this._space.create(Constants.TIME_TREE_CHUNK, nodeWorld, Constants.NULL_LONG, nodeId, null, null);
                            this._space.putAndMark(newSuperTimeTree);
                            newSuperTimeTree.insert(nodeTime);

                            KTimeTreeChunk newTimeTree = (KTimeTreeChunk) this._space.create(Constants.TIME_TREE_CHUNK, nodeWorld, nodeTime, nodeId, null, null);
                            this._space.putAndMark(newTimeTree);
                            newTimeTree.insert(nodeTime);

                            //unmark the previous time tree, now we have switched to the new one
                            this._space.unmarkChunk(objectTimeTree);

                            //TODO what about subTree unmarking :-)
                            objectWorldMap.put(nodeWorld, nodeTime);

                        }
                        //double unMarking, because, we should not use anymore this object
                        this._space.unmarkChunk(currentEntry);
                        this._space.unmarkChunk(currentEntry);
                        //free the rest of used object
                        this._space.unmarkChunk(objectTimeTree);
                        this._space.unmarkChunk(objectSuperTimeTree);
                        this._space.unmarkChunk(objectWorldMap);
                        this._space.unmarkChunk(globalWorldOrder);
                        //free lock
                        objectWorldMap.unlock();
                        return clonedChunk;
                    } else {
                        //somebody as clone for us, now waiting for the chunk to be available
                        this._space.unmarkChunk(objectTimeTree);
                        this._space.unmarkChunk(objectSuperTimeTree);
                        this._space.unmarkChunk(objectWorldMap);
                        this._space.unmarkChunk(globalWorldOrder);
                        KStateChunk waitingChunk = (KStateChunk) this._space.getAndMark(Constants.STATE_CHUNK, nodeWorld, nodeTime, nodeId);
                        this._space.unmarkChunk(waitingChunk);
                        //free lock
                        objectWorldMap.unlock();
                        return waitingChunk;
                    }
                }
            } else {
                this._space.unmarkChunk(objectTimeTree);
                this._space.unmarkChunk(objectSuperTimeTree);
                this._space.unmarkChunk(objectWorldMap);
                this._space.unmarkChunk(globalWorldOrder);
                //free lock
                objectWorldMap.unlock();
                return null;
            }

        } catch (Throwable r) {
            //free lock
            r.printStackTrace();
            objectWorldMap.unlock();
            return null;
        }
    }

    @Override
    public void resolveTimepoints(final KNode node, final long beginningOfSearch, final long endOfSearch, final KCallback<long[]> callback) {
        long[] keys = new long[]{
                Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG,
                Constants.NULL_LONG, Constants.NULL_LONG, node.id()
        };
        getOrLoadAndMarkAll(new byte[]{Constants.WORLD_ORDER_CHUNK, Constants.WORLD_ORDER_CHUNK}, keys, new KCallback<KChunk[]>() {
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
                resolveTimepointsFromWorlds(globalWorldOrder, objectWorldOrder, node, beginningOfSearch, endOfSearch, collectedWorlds[0], collectedIndex, callback);
            }
        });
    }

    private void resolveTimepointsFromWorlds(final KWorldOrderChunk globalWorldOrder, final KWorldOrderChunk objectWorldOrder, final KNode node, final long beginningOfSearch, final long endOfSearch, final long[] collectedWorlds, final int collectedWorldsSize, final KCallback<long[]> callback) {
        final MWGResolver selfPointer = this;

        final long[] timeTreeKeys = new long[collectedWorldsSize * 3];
        final byte[] types = new byte[collectedWorldsSize];
        for (int i = 0; i < collectedWorldsSize; i++) {
            timeTreeKeys[i * 3] = collectedWorlds[i];
            timeTreeKeys[i * 3 + 1] = Constants.NULL_LONG;
            timeTreeKeys[i * 3 + 2] = node.id();
            types[i] = Constants.TIME_TREE_CHUNK;
        }
        getOrLoadAndMarkAll(types, timeTreeKeys, new KCallback<KChunk[]>() {
            @Override
            public void on(final KChunk[] superTimeTrees) {
                if (superTimeTrees == null) {
                    selfPointer._space.unmarkChunk(objectWorldOrder);
                    selfPointer._space.unmarkChunk(globalWorldOrder);
                    callback.on(new long[0]);
                } else {
                    //time collector
                    final int[] collectedSize = {Constants.MAP_INITIAL_CAPACITY};
                    final long[][] collectedSuperTimes = {new long[collectedSize[0]]};
                    final long[][] collectedSuperTimesAssociatedWorlds = {new long[collectedSize[0]]};
                    final int[] insert_index = {0};

                    long previousDivergenceTime = endOfSearch;
                    for (int i = 0; i < collectedWorldsSize; i++) {
                        final KTimeTreeChunk timeTree = (KTimeTreeChunk) superTimeTrees[i];
                        if (timeTree != null) {
                            long currentDivergenceTime = objectWorldOrder.get(collectedWorlds[i]);
                            if (currentDivergenceTime < beginningOfSearch) {
                                currentDivergenceTime = beginningOfSearch;
                            }
                            final long finalPreviousDivergenceTime = previousDivergenceTime;
                            timeTree.range(currentDivergenceTime, previousDivergenceTime, Constants.END_OF_TIME, new KTreeWalker() {
                                @Override
                                public void elem(long t) {
                                    if (t != finalPreviousDivergenceTime) {
                                        collectedSuperTimes[0][insert_index[0]] = t;
                                        collectedSuperTimesAssociatedWorlds[0][insert_index[0]] = timeTree.world();
                                        insert_index[0]++;
                                        if (collectedSize[0] == insert_index[0]) {
                                            //reallocate
                                            long[] temp_collectedSuperTimes = new long[collectedSize[0] * 2];
                                            long[] temp_collectedSuperTimesAssociatedWorlds = new long[collectedSize[0] * 2];
                                            System.arraycopy(collectedSuperTimes[0], 0, temp_collectedSuperTimes, 0, collectedSize[0]);
                                            System.arraycopy(collectedSuperTimesAssociatedWorlds[0], 0, temp_collectedSuperTimesAssociatedWorlds, 0, collectedSize[0]);

                                            collectedSuperTimes[0] = temp_collectedSuperTimes;
                                            collectedSuperTimesAssociatedWorlds[0] = temp_collectedSuperTimesAssociatedWorlds;

                                            collectedSize[0] = collectedSize[0] * 2;
                                        }
                                    }
                                }
                            });
                            previousDivergenceTime = currentDivergenceTime;
                        }
                        selfPointer._space.unmarkChunk(timeTree);
                    }
                    //now we have superTimes, lets convert them to all times
                    resolveTimepointsFromSuperTimes(globalWorldOrder, objectWorldOrder, node, beginningOfSearch, endOfSearch, collectedSuperTimesAssociatedWorlds[0], collectedSuperTimes[0], insert_index[0], callback);
                }
            }
        });
    }

    private void resolveTimepointsFromSuperTimes(final KWorldOrderChunk globalWorldOrder, final KWorldOrderChunk objectWorldOrder, final KNode node, final long beginningOfSearch, final long endOfSearch, final long[] collectedWorlds, final long[] collectedSuperTimes, final int collectedSize, final KCallback<long[]> callback) {
        final MWGResolver selfPointer = this;

        final long[] timeTreeKeys = new long[collectedSize * 3];
        final byte[] types = new byte[collectedSize];
        for (int i = 0; i < collectedSize; i++) {
            timeTreeKeys[i * 3] = collectedWorlds[i];
            timeTreeKeys[i * 3 + 1] = collectedSuperTimes[i];
            timeTreeKeys[i * 3 + 2] = node.id();
            types[i] = Constants.TIME_TREE_CHUNK;
        }
        getOrLoadAndMarkAll(types, timeTreeKeys, new KCallback<KChunk[]>() {
            @Override
            public void on(KChunk[] timeTrees) {
                if (timeTrees == null) {
                    selfPointer._space.unmarkChunk(objectWorldOrder);
                    selfPointer._space.unmarkChunk(globalWorldOrder);
                    callback.on(new long[0]);
                } else {
                    //time collector
                    final int[] collectedTimesSize = {Constants.MAP_INITIAL_CAPACITY};
                    final long[][] collectedTimes = {new long[collectedTimesSize[0]]};
                    final int[] insert_index = {0};
                    long previousDivergenceTime = endOfSearch;
                    for (int i = 0; i < collectedSize; i++) {
                        final KTimeTreeChunk timeTree = (KTimeTreeChunk) timeTrees[i];
                        if (timeTree != null) {
                            long currentDivergenceTime = objectWorldOrder.get(collectedWorlds[i]);
                            if (currentDivergenceTime < beginningOfSearch) {
                                currentDivergenceTime = beginningOfSearch;
                            }
                            final long finalPreviousDivergenceTime = previousDivergenceTime;
                            timeTree.range(currentDivergenceTime, previousDivergenceTime, Constants.END_OF_TIME, new KTreeWalker() {
                                @Override
                                public void elem(long t) {
                                    if (t != finalPreviousDivergenceTime) {
                                        collectedTimes[0][insert_index[0]] = t;
                                        insert_index[0]++;
                                        if (collectedTimesSize[0] == insert_index[0]) {
                                            //reallocate
                                            long[] temp_collectedTimes = new long[collectedTimesSize[0] * 2];
                                            System.arraycopy(collectedTimes[0], 0, temp_collectedTimes, 0, collectedTimesSize[0]);
                                            collectedTimes[0] = temp_collectedTimes;
                                            collectedTimesSize[0] = collectedTimesSize[0] * 2;
                                        }
                                    }
                                }
                            });
                            previousDivergenceTime = currentDivergenceTime;
                        }
                        selfPointer._space.unmarkChunk(timeTree);
                    }
                    //now we have times
                    if (insert_index[0] != collectedTimesSize[0]) {
                        long[] tempTimeline = new long[insert_index[0]];
                        System.arraycopy(collectedTimes[0], 0, tempTimeline, 0, insert_index[0]);
                        collectedTimes[0] = tempTimeline;
                    }
                    selfPointer._space.unmarkChunk(objectWorldOrder);
                    selfPointer._space.unmarkChunk(globalWorldOrder);
                    callback.on(collectedTimes[0]);
                }
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
