package org.mwg.core;

import org.mwg.*;
import org.mwg.struct.*;
import org.mwg.plugin.*;
import org.mwg.core.chunk.*;

class MWGResolver implements Resolver {

    private final Storage _storage;

    private final ChunkSpace _space;

    private final NodeTracker _tracker;

    private final Scheduler _scheduler;

    private static final String deadNodeError = "This Node hasField been tagged destroyed, please don't use it anymore!";

    private org.mwg.Graph _graph;

    MWGResolver(Storage p_storage, ChunkSpace p_space, NodeTracker p_tracker, Scheduler p_scheduler) {
        this._storage = p_storage;
        this._space = p_space;
        this._tracker = p_tracker;
        this._scheduler = p_scheduler;
    }

    private StateChunk dictionary;

    @Override
    public void init(org.mwg.Graph graph) {
        _graph = graph;
        dictionary = (StateChunk) this._space.getAndMark(CoreConstants.STATE_CHUNK, CoreConstants.GLOBAL_DICTIONARY_KEY[0], CoreConstants.GLOBAL_DICTIONARY_KEY[1], CoreConstants.GLOBAL_DICTIONARY_KEY[2]);
    }

    @Override
    public void initNode(org.mwg.Node node, long codeType) {
        StateChunk cacheEntry_0 = (StateChunk) this._space.create(CoreConstants.STATE_CHUNK, node.world(), node.time(), node.id(), null, null);
        //put and mark
        StateChunk cacheEntry = (StateChunk) this._space.putAndMark(cacheEntry_0);
        if (cacheEntry_0 != cacheEntry) {
            this._space.freeChunk(cacheEntry_0);
        }
        //declare dirty now because potentially no insert could be done
        this._space.declareDirty(cacheEntry);

        //initiate superTime management
        TimeTreeChunk superTimeTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, node.world(), Constants.NULL_LONG, node.id(), null, null);
        TimeTreeChunk superTimeTree = (TimeTreeChunk) this._space.putAndMark(superTimeTree_0);
        if (superTimeTree != superTimeTree_0) {
            this._space.freeChunk(superTimeTree_0);
        }
        superTimeTree.insert(node.time());

        //initiate time management
        TimeTreeChunk timeTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, node.world(), node.time(), node.id(), null, null);
        TimeTreeChunk timeTree = (TimeTreeChunk) this._space.putAndMark(timeTree_0);
        if (timeTree_0 != timeTree) {
            this._space.freeChunk(timeTree_0);
        }
        timeTree.insert(node.time());

        //initiate universe management
        WorldOrderChunk objectWorldOrder_0 = (WorldOrderChunk) this._space.create(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, node.id(), null, null);
        WorldOrderChunk objectWorldOrder = (WorldOrderChunk) this._space.putAndMark(objectWorldOrder_0);
        if (objectWorldOrder_0 != objectWorldOrder) {
            this._space.freeChunk(objectWorldOrder_0);
        }

        objectWorldOrder.put(node.world(), node.time());
        objectWorldOrder.setExtra(codeType);
        //mark the global

        this._space.getAndMark(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        //monitor the node object
        this._tracker.monitor(node);
    }

    @Override
    public void initWorld(long parentWorld, long childWorld) {
        WorldOrderChunk worldOrder = (WorldOrderChunk) this._space.getAndMark(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG);
        worldOrder.put(childWorld, parentWorld);
        this._space.unmarkChunk(worldOrder);
    }

    @Override
    public void freeNode(org.mwg.Node node) {
        AbstractNode casted = (AbstractNode) node;
        long nodeId = node.id();
        long[] previous;
        do {
            previous = casted._previousResolveds.get();
        } while (!casted._previousResolveds.compareAndSet(previous, null));
        if (previous != null) {
            this._space.unmark(CoreConstants.STATE_CHUNK, previous[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previous[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);//FREE OBJECT CHUNK
            this._space.unmark(CoreConstants.TIME_TREE_CHUNK, previous[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);//FREE TIME TREE
            this._space.unmark(CoreConstants.TIME_TREE_CHUNK, previous[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previous[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX], nodeId);//FREE TIME TREE
            this._space.unmark(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, nodeId); //FREE OBJECT UNIVERSE MAP
            this._space.unmark(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG); //FREE GLOBAL UNIVERSE MAP
        }
    }

    @Override
    public <A extends org.mwg.Node> void lookup(long world, long time, long id, Callback<A> callback) {
        this._scheduler.dispatch(lookupJob(world, time, id, callback));
    }

    @Override
    public <A extends org.mwg.Node> Job lookupJob(final long world, final long time, final long id, final Callback<A> callback) {
        final MWGResolver selfPointer = this;
        return new Job() {
            @Override
            public void run() {
                try {
                    selfPointer.getOrLoadAndMark(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, Constants.NULL_LONG, new Callback<Chunk>() {
                        @Override
                        public void on(Chunk theGlobalWorldOrder) {
                            if (theGlobalWorldOrder != null) {
                                selfPointer.getOrLoadAndMark(CoreConstants.WORLD_ORDER_CHUNK, Constants.NULL_LONG, Constants.NULL_LONG, id, new Callback<Chunk>() {
                                    @Override
                                    public void on(Chunk theNodeWorldOrder) {
                                        if (theNodeWorldOrder == null) {
                                            selfPointer._space.unmarkChunk(theGlobalWorldOrder);
                                            callback.on(null);
                                        } else {
                                            final long closestWorld = resolve_world((LongLongMap) theGlobalWorldOrder, (LongLongMap) theNodeWorldOrder, time, world);
                                            selfPointer.getOrLoadAndMark(CoreConstants.TIME_TREE_CHUNK, closestWorld, Constants.NULL_LONG, id, new Callback<Chunk>() {
                                                @Override
                                                public void on(Chunk theNodeSuperTimeTree) {
                                                    if (theNodeSuperTimeTree == null) {
                                                        selfPointer._space.unmarkChunk(theNodeWorldOrder);
                                                        selfPointer._space.unmarkChunk(theGlobalWorldOrder);
                                                        callback.on(null);
                                                    } else {
                                                        final long closestSuperTime = ((KLongTree) theNodeSuperTimeTree).previousOrEqual(time);
                                                        if (closestSuperTime == Constants.NULL_LONG) {
                                                            selfPointer._space.unmarkChunk(theNodeSuperTimeTree);
                                                            selfPointer._space.unmarkChunk(theNodeWorldOrder);
                                                            selfPointer._space.unmarkChunk(theGlobalWorldOrder);
                                                            callback.on(null);
                                                            return;
                                                        }
                                                        selfPointer.getOrLoadAndMark(CoreConstants.TIME_TREE_CHUNK, closestWorld, closestSuperTime, id, new Callback<Chunk>() {
                                                            @Override
                                                            public void on(Chunk theNodeTimeTree) {
                                                                if (theNodeTimeTree == null) {
                                                                    selfPointer._space.unmarkChunk(theNodeSuperTimeTree);
                                                                    selfPointer._space.unmarkChunk(theNodeWorldOrder);
                                                                    selfPointer._space.unmarkChunk(theGlobalWorldOrder);
                                                                    callback.on(null);
                                                                } else {
                                                                    long closestTime = ((KLongTree) theNodeTimeTree).previousOrEqual(time);
                                                                    if (closestTime == Constants.NULL_LONG) {
                                                                        selfPointer._space.unmarkChunk(theNodeTimeTree);
                                                                        selfPointer._space.unmarkChunk(theNodeSuperTimeTree);
                                                                        selfPointer._space.unmarkChunk(theNodeWorldOrder);
                                                                        selfPointer._space.unmarkChunk(theGlobalWorldOrder);
                                                                        callback.on(null);
                                                                        return;
                                                                    }
                                                                    selfPointer.getOrLoadAndMark(CoreConstants.STATE_CHUNK, closestWorld, closestTime, id, new Callback<Chunk>() {
                                                                        @Override
                                                                        public void on(Chunk theObjectChunk) {
                                                                            if (theObjectChunk == null) {
                                                                                selfPointer._space.unmarkChunk(theNodeTimeTree);
                                                                                selfPointer._space.unmarkChunk(theNodeSuperTimeTree);
                                                                                selfPointer._space.unmarkChunk(theNodeWorldOrder);
                                                                                selfPointer._space.unmarkChunk(theGlobalWorldOrder);
                                                                                callback.on(null);
                                                                            } else {
                                                                                WorldOrderChunk castedNodeWorldOrder = (WorldOrderChunk) theNodeWorldOrder;
                                                                                long extraCode = castedNodeWorldOrder.extra();
                                                                                NodeFactory resolvedFactory = null;
                                                                                if (extraCode != Constants.NULL_LONG) {
                                                                                    resolvedFactory = ((CoreGraph) _graph).factoryByCode(extraCode);
                                                                                }

                                                                                long[] initPreviouslyResolved = new long[6];
                                                                                //init previously resolved values
                                                                                initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = closestWorld;
                                                                                initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = closestSuperTime;
                                                                                initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = closestTime;
                                                                                //init previous magics
                                                                                initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = ((WorldOrderChunk) theNodeWorldOrder).magic();
                                                                                initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = ((KLongTree) theNodeSuperTimeTree).magic();
                                                                                initPreviouslyResolved[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = ((KLongTree) theNodeTimeTree).magic();

                                                                                org.mwg.Node resolvedNode;
                                                                                if (resolvedFactory == null) {
                                                                                    resolvedNode = new CoreNode(world, time, id, _graph, initPreviouslyResolved);
                                                                                } else {
                                                                                    resolvedNode = resolvedFactory.create(world, time, id, _graph, initPreviouslyResolved);
                                                                                }
                                                                                selfPointer._tracker.monitor(resolvedNode);
                                                                                callback.on((A) resolvedNode);
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

    private long resolve_world(final LongLongMap globalWorldOrder, final LongLongMap nodeWorldOrder, final long timeToResolve, long originWorld) {
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

    private void getOrLoadAndMark(final byte type, final long world, final long time, final long id, final Callback<Chunk> callback) {
        if (world == CoreConstants.NULL_KEY[0] && time == CoreConstants.NULL_KEY[1] && id == CoreConstants.NULL_KEY[2]) {
            callback.on(null);
            return;
        }
        final MWGResolver selfPointer = this;
        Chunk cached = this._space.getAndMark(type, world, time, id);
        if (cached != null) {
            callback.on(cached);
        } else {
            Buffer buffer = _graph.newBuffer();
            org.mwg.core.utility.Buffer.keyToBuffer(buffer, type, world, time, id);

            this._storage.get(new Buffer[]{buffer}, new Callback<Buffer[]>() {
                @Override
                public void on(Buffer[] payloads) {
                    buffer.free();
                    Chunk result = null;
                    if (payloads.length > 0 && payloads[0] != null) {
                        result = selfPointer._space.create(type, world, time, id, payloads[0], null);
                        selfPointer._space.putAndMark(result);
                    }
                    callback.on(result);
                }
            });

        }
    }

    private static int KEY_SIZE = 3;

    private void getOrLoadAndMarkAll(byte[] types, long[] keys, final Callback<Chunk[]> callback) {
        int nbKeys = keys.length / KEY_SIZE;
        final boolean[] toLoadIndexes = new boolean[nbKeys];
        int nbElem = 0;
        final Chunk[] result = new Chunk[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            if (keys[i * KEY_SIZE] == CoreConstants.NULL_KEY[0] && keys[i * KEY_SIZE + 1] == CoreConstants.NULL_KEY[1] && keys[i * KEY_SIZE + 2] == CoreConstants.NULL_KEY[2]) {
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
            final Buffer[] keysToLoad = new Buffer[nbElem];
            final int[] reverseIndex = new int[nbElem];
            int lastInsertedIndex = 0;
            for (int i = 0; i < nbKeys; i++) {
                if (toLoadIndexes[i]) {
                    reverseIndex[lastInsertedIndex] = i;
                    keysToLoad[lastInsertedIndex] = _graph.newBuffer();
                    org.mwg.core.utility.Buffer.keyToBuffer(keysToLoad[lastInsertedIndex], types[i], keys[i * KEY_SIZE], keys[i * KEY_SIZE + 1], keys[i * KEY_SIZE + 2]);
                    lastInsertedIndex = lastInsertedIndex + 1;
                }
            }
            final MWGResolver selfPointer = this;
            this._storage.get(keysToLoad, new Callback<Buffer[]>() {
                @Override
                public void on(Buffer[] fromDbBuffers) {
                    for (int i = 0; i < keysToLoad.length; i++) {
                        keysToLoad[i].free();
                    }
                    for (int i = 0; i < fromDbBuffers.length; i++) {
                        if (fromDbBuffers[i] != null) {
                            int reversedIndex = reverseIndex[i];
                            result[reversedIndex] = selfPointer._space.create(types[reversedIndex], keys[reversedIndex * KEY_SIZE], keys[reversedIndex * KEY_SIZE + 1], keys[reversedIndex * KEY_SIZE + 2], fromDbBuffers[i], null);
                        }
                    }
                    callback.on(result);
                }
            });
        }
    }

    @Override
    public NodeState newState(org.mwg.Node node, long world, long time) {

        //Retrieve Node needed chunks
        WorldOrderChunk nodeWorldOrder = (WorldOrderChunk) this._space.getAndMark(CoreConstants.WORLD_ORDER_CHUNK, CoreConstants.NULL_LONG, CoreConstants.NULL_LONG, node.id());
        if (nodeWorldOrder == null) {
            return null;
        }
        //SOMETHING WILL MOVE HERE ANYWAY SO WE SYNC THE OBJECT, even for dePhasing read only objects because they can be unaligned after
        nodeWorldOrder.lock();
        //OK NOW WE HAVE THE TOKEN globally FOR the node ID

        Chunk resultState = null;
        try {
            AbstractNode castedNode = (AbstractNode) node;
            //protection against deleted Node
            long[] previousResolveds = castedNode._previousResolveds.get();
            if (previousResolveds == null) {
                throw new RuntimeException(deadNodeError);
            }

            if (time < previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC]) {
                throw new RuntimeException("New state cannot be used to create state before the previously resolved state");
            }

            long nodeId = node.id();

            //check if anything as moved
            if (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] == world && previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] == time) {
                //no new state to create
                resultState = this._space.getAndMark(CoreConstants.STATE_CHUNK, world, time, nodeId);
                this._space.unmarkChunk(resultState);
                this._space.unmarkChunk(nodeWorldOrder);
                return (NodeState) resultState;
            }

            //first we create and insert the empty state
            Chunk resultState_0 = this._space.create(CoreConstants.STATE_CHUNK, world, time, nodeId, null, null);
            resultState = _space.putAndMark(resultState_0);
            if (resultState_0 != resultState) {
                _space.freeChunk(resultState_0);
            }

            if (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] == world || nodeWorldOrder.get(world) != CoreConstants.NULL_LONG) {

                //let's go for the resolution now
                TimeTreeChunk nodeSuperTimeTree = (TimeTreeChunk) this._space.getAndMark(CoreConstants.TIME_TREE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], CoreConstants.NULL_LONG, nodeId);
                if (nodeSuperTimeTree == null) {
                    this._space.unmarkChunk(nodeWorldOrder);
                    return null;
                }
                TimeTreeChunk nodeTimeTree = (TimeTreeChunk) this._space.getAndMark(CoreConstants.TIME_TREE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX], nodeId);
                if (nodeTimeTree == null) {
                    this._space.unmarkChunk(nodeSuperTimeTree);
                    this._space.unmarkChunk(nodeWorldOrder);
                    return null;
                }

                //manage super tree here
                long superTreeSize = nodeSuperTimeTree.size();
                long threshold = CoreConstants.SCALE_1 * 2;
                if (superTreeSize > threshold) {
                    threshold = CoreConstants.SCALE_2 * 2;
                }
                if (superTreeSize > threshold) {
                    threshold = CoreConstants.SCALE_3 * 2;
                }
                if (superTreeSize > threshold) {
                    threshold = CoreConstants.SCALE_4 * 2;
                }
                nodeTimeTree.insert(time);
                if (nodeTimeTree.size() == threshold) {
                    final long[] medianPoint = {-1};
                    //we iterate over the tree selectWithout boundaries for values, but selectWith boundaries for number of collected times
                    nodeTimeTree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, nodeTimeTree.size() / 2, new TreeWalker() {
                        @Override
                        public void elem(long t) {
                            medianPoint[0] = t;
                        }
                    });

                    TimeTreeChunk rightTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, world, medianPoint[0], nodeId, null, null);
                    TimeTreeChunk rightTree = (TimeTreeChunk) this._space.putAndMark(rightTree_0);
                    if (rightTree_0 != rightTree) {
                        this._space.freeChunk(rightTree_0);
                    }

                    //TODO second iterate that can be avoided, however we need the median point to create the right tree
                    //we iterate over the tree selectWithout boundaries for values, but selectWith boundaries for number of collected times
                    final TimeTreeChunk finalRightTree = rightTree;
                    //rang iterate fromVar the end of the tree
                    nodeTimeTree.range(Constants.BEGINNING_OF_TIME, Constants.END_OF_TIME, nodeTimeTree.size() / 2, new TreeWalker() {
                        @Override
                        public void elem(long t) {
                            finalRightTree.insert(t);
                        }
                    });
                    nodeSuperTimeTree.insert(medianPoint[0]);
                    //remove times insert in the right tree
                    nodeTimeTree.clearAt(medianPoint[0]);

                    //ok ,now manage marks
                    if (time < medianPoint[0]) {

                        this._space.unmarkChunk(rightTree);
                        this._space.unmarkChunk(nodeSuperTimeTree);
                        this._space.unmarkChunk(nodeTimeTree);

                        long[] newResolveds = new long[6];
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;

                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX];
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrder.magic();
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = nodeSuperTimeTree.magic();
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTree.magic();
                        castedNode._previousResolveds.set(newResolveds);
                    } else {

                        //double unMark current nodeTimeTree
                        this._space.unmarkChunk(nodeTimeTree);
                        this._space.unmarkChunk(nodeTimeTree);
                        //unmark node superTimeTree
                        this._space.unmarkChunk(nodeSuperTimeTree);

                        //let's store the new state if necessary
                        long[] newResolveds = new long[6];
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = medianPoint[0];
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrder.magic();
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = rightTree.magic();
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTree.magic();
                        castedNode._previousResolveds.set(newResolveds);
                    }
                } else {
                    //update the state cache selectWithout superTree modification
                    long[] newResolveds = new long[6];
                    //previously resolved
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX];
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
                    //previously magics
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrder.magic();
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = nodeSuperTimeTree.magic();
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTree.magic();
                    castedNode._previousResolveds.set(newResolveds);

                    this._space.unmarkChunk(nodeSuperTimeTree);
                    this._space.unmarkChunk(nodeTimeTree);
                }
            } else {

                //create a new node superTimeTree
                TimeTreeChunk newSuperTimeTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, world, CoreConstants.NULL_LONG, nodeId, null, null);
                TimeTreeChunk newSuperTimeTree = (TimeTreeChunk) this._space.putAndMark(newSuperTimeTree_0);
                if (newSuperTimeTree != newSuperTimeTree_0) {
                    this._space.freeChunk(newSuperTimeTree_0);
                }
                newSuperTimeTree.insert(time);

                //create a new node timeTree
                TimeTreeChunk newTimeTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, world, time, nodeId, null, null);
                TimeTreeChunk newTimeTree = (TimeTreeChunk) this._space.putAndMark(newTimeTree_0);
                if (newTimeTree != newTimeTree_0) {
                    this._space.freeChunk(newTimeTree_0);
                }
                newTimeTree.insert(time);

                //insert into node world order
                nodeWorldOrder.put(world, time);

                //let's store the new state if necessary
                long[] newResolveds = new long[6];
                //previously resolved
                newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = world;
                newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = time;
                newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = time;
                //previously magics
                newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrder.magic();
                newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = newSuperTimeTree.magic();
                newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = newTimeTree.magic();
                castedNode._previousResolveds.set(newResolveds);

                //unMark previous super Tree
                _space.unmark(CoreConstants.TIME_TREE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], Constants.NULL_LONG, nodeId);
                //unMark previous time Tree
                _space.unmark(CoreConstants.TIME_TREE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX], nodeId);

            }

            //unMark previous state, for the newly created one
            _space.unmark(CoreConstants.STATE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
            _space.unmarkChunk(nodeWorldOrder);

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            nodeWorldOrder.unlock();
        }
        return (NodeState) resultState;
    }

    @Override
    public NodeState resolveState(org.mwg.Node node, boolean allowDephasing) {
        AbstractNode castedNode = (AbstractNode) node;
        //protection against deleted Node
        long[] previousResolveds = castedNode._previousResolveds.get();
        if (previousResolveds == null) {
            throw new RuntimeException(deadNodeError);
        }
        //let's go for the resolution now
        long nodeWorld = node.world();
        long nodeTime = node.time();
        long nodeId = node.id();

        //OPTIMIZATION #1: NO DEPHASING
        if (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] == nodeWorld && previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] == nodeTime) {
            StateChunk currentEntry = (StateChunk) this._space.getAndMark(CoreConstants.STATE_CHUNK, nodeWorld, nodeTime, nodeId);
            if (currentEntry != null) {
                this._space.unmarkChunk(currentEntry);
                return currentEntry;
            }
        }

        //Retrieve Node needed chunks
        WorldOrderChunk nodeWorldOrder = (WorldOrderChunk) this._space.getAndMark(CoreConstants.WORLD_ORDER_CHUNK, CoreConstants.NULL_LONG, CoreConstants.NULL_LONG, nodeId);
        if (nodeWorldOrder == null) {
            return null;
        }
        TimeTreeChunk nodeSuperTimeTree = (TimeTreeChunk) this._space.getAndMark(CoreConstants.TIME_TREE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], CoreConstants.NULL_LONG, nodeId);
        if (nodeSuperTimeTree == null) {
            this._space.unmarkChunk(nodeWorldOrder);
            return null;
        }
        TimeTreeChunk nodeTimeTree = (TimeTreeChunk) this._space.getAndMark(CoreConstants.TIME_TREE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX], nodeId);
        if (nodeTimeTree == null) {
            this._space.unmarkChunk(nodeSuperTimeTree);
            this._space.unmarkChunk(nodeWorldOrder);
            return null;
        }

        long nodeWorldOrderMagic = nodeWorldOrder.magic();
        long nodeSuperTimeTreeMagic = nodeSuperTimeTree.magic();
        long nodeTimeTreeMagic = nodeTimeTree.magic();

        //OPTIMIZATION #2: SAME DEPHASING
        if (allowDephasing && (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] == nodeWorldOrderMagic) && (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] == nodeSuperTimeTreeMagic) && (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] == nodeTimeTreeMagic)) {
            StateChunk currentNodeState = (StateChunk) this._space.getAndMark(CoreConstants.STATE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
            this._space.unmarkChunk(nodeWorldOrder);
            this._space.unmarkChunk(nodeSuperTimeTree);
            this._space.unmarkChunk(nodeTimeTree);
            if (currentNodeState != null) {
                //ERROR case protection, chunk hasField been removed fromVar cache
                this._space.unmarkChunk(currentNodeState);
            }
            return currentNodeState;
        }

        //NOMINAL CASE, MAGIC NUMBER ARE NOT VALID ANYMORE
        WorldOrderChunk globalWorldOrder = (WorldOrderChunk) this._space.getAndMark(CoreConstants.WORLD_ORDER_CHUNK, CoreConstants.NULL_LONG, CoreConstants.NULL_LONG, CoreConstants.NULL_LONG);
        if (globalWorldOrder == null) {
            this._space.unmarkChunk(nodeWorldOrder);
            this._space.unmarkChunk(nodeSuperTimeTree);
            this._space.unmarkChunk(nodeTimeTree);
            return null;
        }

        //SOMETHING WILL MOVE HERE ANYWAY SO WE SYNC THE OBJECT, even for dePhasing read only objects because they can be unaligned after
        nodeWorldOrder.lock();
        //OK NOW WE HAVE THE TOKEN globally FOR the node ID

        //OPTIMIZATION #1: NO DEPHASING
        if (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] == nodeWorld && previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] == nodeTime) {
            StateChunk currentEntry = (StateChunk) this._space.getAndMark(CoreConstants.STATE_CHUNK, nodeWorld, nodeTime, nodeId);
            if (currentEntry != null) {
                this._space.unmarkChunk(globalWorldOrder);
                this._space.unmarkChunk(nodeWorldOrder);
                this._space.unmarkChunk(nodeSuperTimeTree);
                this._space.unmarkChunk(nodeTimeTree);
                this._space.unmarkChunk(currentEntry);
                return currentEntry;
            }
        }

        //REFRESH
        previousResolveds = castedNode._previousResolveds.get();
        if (previousResolveds == null) {
            throw new RuntimeException(deadNodeError);
        }

        nodeWorldOrderMagic = nodeWorldOrder.magic();
        nodeSuperTimeTreeMagic = nodeSuperTimeTree.magic();
        nodeTimeTreeMagic = nodeTimeTree.magic();

        StateChunk resultStateChunk = null;
        boolean hasToCleanSuperTimeTree = false;
        boolean hasToCleanTimeTree = false;

        try {
            long resolvedWorld;
            long resolvedSuperTime;
            long resolvedTime;
            // OPTIMIZATION #3: SAME DEPHASING THAN BEFORE, DIRECTLY CLONE THE PREVIOUSLY RESOLVED TUPLE
            if (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] == nodeWorldOrderMagic && previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] == nodeSuperTimeTreeMagic && previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] == nodeTimeTreeMagic) {
                resolvedWorld = previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX];
                resolvedSuperTime = previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX];
                resolvedTime = previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX];
                hasToCleanSuperTimeTree = true;
                hasToCleanTimeTree = true;
            } else {
                //Common case, we have to traverseIndex World Order and Time chunks
                resolvedWorld = resolve_world(globalWorldOrder, nodeWorldOrder, nodeTime, nodeWorld);
                if (resolvedWorld != previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX]) {
                    //we have to update the superTree
                    TimeTreeChunk tempNodeSuperTimeTree = (TimeTreeChunk) this._space.getAndMark(CoreConstants.TIME_TREE_CHUNK, resolvedWorld, CoreConstants.NULL_LONG, nodeId);
                    if (tempNodeSuperTimeTree == null) {
                        throw new RuntimeException("Simultaneous rePhasing leading to cache miss!!!");
                    }
                    //free the method mark
                    _space.unmarkChunk(nodeSuperTimeTree);
                    //free the previous lookup mark
                    _space.unmarkChunk(nodeSuperTimeTree);
                    nodeSuperTimeTree = tempNodeSuperTimeTree;
                }
                resolvedSuperTime = nodeSuperTimeTree.previousOrEqual(nodeTime);
                if (previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] != resolvedSuperTime) {
                    //we have to update the timeTree
                    TimeTreeChunk tempNodeTimeTree = (TimeTreeChunk) this._space.getAndMark(CoreConstants.TIME_TREE_CHUNK, resolvedWorld, resolvedSuperTime, nodeId);
                    if (tempNodeTimeTree == null) {
                        throw new RuntimeException("Simultaneous rephasing leading to cache miss!!!");
                    }
                    //free the method mark
                    _space.unmarkChunk(nodeTimeTree);
                    //free the lookup mark
                    _space.unmarkChunk(nodeTimeTree);
                    nodeTimeTree = tempNodeTimeTree;
                }
                resolvedTime = nodeTimeTree.previousOrEqual(nodeTime);
                //we only unMark superTimeTree in case of world phasing, otherwise we keep the mark (as new lookup mark)
                if (resolvedWorld == previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX]) {
                    hasToCleanSuperTimeTree = true;
                }
                //we only unMark timeTree in case of superTime phasing, otherwise we keep the mark (as new lookup mark)
                if (resolvedSuperTime == previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX]) {
                    hasToCleanTimeTree = true;
                }
            }
            boolean worldMoved = resolvedWorld != previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX];
            boolean superTimeTreeMoved = resolvedSuperTime != previousResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX];
            boolean timeTreeMoved = resolvedTime != previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX];

            //so we are dePhase
            if (allowDephasing) {
                resultStateChunk = (StateChunk) this._space.getAndMark(CoreConstants.STATE_CHUNK, resolvedWorld, resolvedTime, nodeId);
                if (resultStateChunk == null) {
                    throw new RuntimeException("Simultaneous rePhasing leading to cache miss!!!");
                }
                boolean refreshNodeCache = false;
                if (worldMoved || timeTreeMoved) {
                    _space.unmark(CoreConstants.STATE_CHUNK, previousResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX], previousResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX], nodeId);
                    refreshNodeCache = true;
                } else {
                    if (superTimeTreeMoved) {
                        refreshNodeCache = true;
                    }
                    _space.unmarkChunk(resultStateChunk);
                }
                if (refreshNodeCache) {
                    long[] newResolveds = new long[6];
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = resolvedWorld;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = resolvedSuperTime;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = resolvedTime;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrderMagic;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = nodeSuperTimeTreeMagic;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTreeMagic;
                    castedNode._previousResolveds.set(newResolveds);
                }
            } else {
                StateChunk previousNodeState = (StateChunk) this._space.getAndMark(CoreConstants.STATE_CHUNK, resolvedWorld, resolvedTime, nodeId);
                //clone the chunk
                resultStateChunk = (StateChunk) this._space.create(CoreConstants.STATE_CHUNK, nodeWorld, nodeTime, nodeId, null, previousNodeState);
                this._space.putAndMark(resultStateChunk);
                this._space.declareDirty(resultStateChunk);
                //free the method mark
                this._space.unmarkChunk(previousNodeState);
                //free the previous lookup lock
                this._space.unmarkChunk(previousNodeState);

                if (resolvedWorld == nodeWorld || nodeWorldOrder.get(nodeWorld) != CoreConstants.NULL_LONG) {
                    //manage super tree here
                    long superTreeSize = nodeSuperTimeTree.size();
                    long threshold = CoreConstants.SCALE_1 * 2;
                    if (superTreeSize > threshold) {
                        threshold = CoreConstants.SCALE_2 * 2;
                    }
                    if (superTreeSize > threshold) {
                        threshold = CoreConstants.SCALE_3 * 2;
                    }
                    if (superTreeSize > threshold) {
                        threshold = CoreConstants.SCALE_4 * 2;
                    }
                    nodeTimeTree.insert(nodeTime);
                    if (nodeTimeTree.size() == threshold) {
                        final long[] medianPoint = {-1};
                        //we iterate over the tree selectWithout boundaries for values, but selectWith boundaries for number of collected times
                        nodeTimeTree.range(CoreConstants.BEGINNING_OF_TIME, CoreConstants.END_OF_TIME, nodeTimeTree.size() / 2, new TreeWalker() {
                            @Override
                            public void elem(long t) {
                                medianPoint[0] = t;
                            }
                        });

                        TimeTreeChunk rightTree = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, nodeWorld, medianPoint[0], nodeId, null, null);
                        rightTree = (TimeTreeChunk) this._space.putAndMark(rightTree);
                        //TODO second iterate that can be avoided, however we need the median point to create the right tree
                        //we iterate over the tree selectWithout boundaries for values, but selectWith boundaries for number of collected times
                        final TimeTreeChunk finalRightTree = rightTree;
                        //rang iterate fromVar the end of the tree
                        nodeTimeTree.range(CoreConstants.BEGINNING_OF_TIME, CoreConstants.END_OF_TIME, nodeTimeTree.size() / 2, new TreeWalker() {
                            @Override
                            public void elem(long t) {
                                finalRightTree.unsafe_insert(t);
                            }
                        });
                        _space.declareDirty(finalRightTree);
                        nodeSuperTimeTree.insert(medianPoint[0]);
                        //remove times insert in the right tree
                        nodeTimeTree.clearAt(medianPoint[0]);

                        //ok ,now manage marks
                        if (nodeTime < medianPoint[0]) {
                            _space.unmarkChunk(rightTree);
                            long[] newResolveds = new long[6];
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = nodeWorld;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = resolvedSuperTime;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = nodeTime;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrderMagic;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = nodeSuperTimeTree.magic();
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTree.magic();
                            castedNode._previousResolveds.set(newResolveds);
                        } else {
                            //TODO check potentially marking bug (bad mark retention here...)
                            hasToCleanTimeTree = true;

                            //let's store the new state if necessary
                            long[] newResolveds = new long[6];
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = nodeWorld;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = medianPoint[0];
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = nodeTime;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrderMagic;
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = rightTree.magic();
                            newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTree.magic();
                            castedNode._previousResolveds.set(newResolveds);
                        }
                    } else {
                        //update the state cache selectWithout superTree modification
                        long[] newResolveds = new long[6];
                        //previously resolved
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = nodeWorld;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = resolvedSuperTime;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = nodeTime;
                        //previously magics
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrderMagic;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = nodeSuperTimeTreeMagic;
                        newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = nodeTimeTree.magic();
                        castedNode._previousResolveds.set(newResolveds);
                    }
                } else {

                    //create a new node superTimeTree
                    TimeTreeChunk newSuperTimeTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, nodeWorld, CoreConstants.NULL_LONG, nodeId, null, null);
                    TimeTreeChunk newSuperTimeTree = (TimeTreeChunk) this._space.putAndMark(newSuperTimeTree_0);
                    if (newSuperTimeTree_0 != newSuperTimeTree) {
                        this._space.freeChunk(newSuperTimeTree_0);
                    }
                    newSuperTimeTree.insert(nodeTime);

                    //create a new node timeTree
                    TimeTreeChunk newTimeTree_0 = (TimeTreeChunk) this._space.create(CoreConstants.TIME_TREE_CHUNK, nodeWorld, nodeTime, nodeId, null, null);
                    TimeTreeChunk newTimeTree = (TimeTreeChunk) this._space.putAndMark(newTimeTree_0);
                    if (newTimeTree_0 != newTimeTree) {
                        this._space.freeChunk(newTimeTree_0);
                    }
                    newTimeTree.insert(nodeTime);

                    //insert into node world order
                    nodeWorldOrder.put(nodeWorld, nodeTime);

                    //let's store the new state if necessary
                    long[] newResolveds = new long[6];
                    //previously resolved
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_INDEX] = nodeWorld;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_INDEX] = nodeTime;
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_INDEX] = nodeTime;
                    //previously magics
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_WORLD_MAGIC] = nodeWorldOrder.magic();
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_SUPER_TIME_MAGIC] = newSuperTimeTree.magic();
                    newResolveds[CoreConstants.PREVIOUS_RESOLVED_TIME_MAGIC] = newTimeTree.magic();
                    castedNode._previousResolveds.set(newResolveds);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            //free lock
            nodeWorldOrder.unlock();
        }
        if (hasToCleanSuperTimeTree) {
            _space.unmarkChunk(nodeSuperTimeTree);
        }
        if (hasToCleanTimeTree) {
            _space.unmarkChunk(nodeTimeTree);
        }
        //unMark World order chunks
        _space.unmarkChunk(globalWorldOrder);
        _space.unmarkChunk(nodeWorldOrder);
        return resultStateChunk;
    }

    @Override
    public void resolveTimepoints(final org.mwg.Node node, final long beginningOfSearch, final long endOfSearch, final Callback<long[]> callback) {
        long[] keys = new long[]{
                CoreConstants.NULL_LONG, CoreConstants.NULL_LONG, CoreConstants.NULL_LONG,
                CoreConstants.NULL_LONG, CoreConstants.NULL_LONG, node.id()
        };
        getOrLoadAndMarkAll(new byte[]{CoreConstants.WORLD_ORDER_CHUNK, CoreConstants.WORLD_ORDER_CHUNK}, keys, new Callback<Chunk[]>() {
            @Override
            public void on(Chunk[] orders) {
                if (orders == null || orders.length != 2) {
                    callback.on(new long[0]);
                    return;
                }
                final WorldOrderChunk globalWorldOrder = (WorldOrderChunk) orders[0];
                final WorldOrderChunk objectWorldOrder = (WorldOrderChunk) orders[1];
                //worlds collector
                final int[] collectionSize = {CoreConstants.MAP_INITIAL_CAPACITY};
                final long[][] collectedWorlds = {new long[collectionSize[0]]};
                int collectedIndex = 0;

                long currentWorld = node.world();
                while (currentWorld != CoreConstants.NULL_LONG) {
                    long divergenceTimepoint = objectWorldOrder.get(currentWorld);
                    if (divergenceTimepoint != CoreConstants.NULL_LONG) {
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

    private void resolveTimepointsFromWorlds(final WorldOrderChunk globalWorldOrder, final WorldOrderChunk objectWorldOrder, final org.mwg.Node node, final long beginningOfSearch, final long endOfSearch, final long[] collectedWorlds, final int collectedWorldsSize, final Callback<long[]> callback) {
        final MWGResolver selfPointer = this;

        final long[] timeTreeKeys = new long[collectedWorldsSize * 3];
        final byte[] types = new byte[collectedWorldsSize];
        for (int i = 0; i < collectedWorldsSize; i++) {
            timeTreeKeys[i * 3] = collectedWorlds[i];
            timeTreeKeys[i * 3 + 1] = CoreConstants.NULL_LONG;
            timeTreeKeys[i * 3 + 2] = node.id();
            types[i] = CoreConstants.TIME_TREE_CHUNK;
        }
        getOrLoadAndMarkAll(types, timeTreeKeys, new Callback<Chunk[]>() {
            @Override
            public void on(final Chunk[] superTimeTrees) {
                if (superTimeTrees == null) {
                    selfPointer._space.unmarkChunk(objectWorldOrder);
                    selfPointer._space.unmarkChunk(globalWorldOrder);
                    callback.on(new long[0]);
                } else {
                    //time collector
                    final int[] collectedSize = {CoreConstants.MAP_INITIAL_CAPACITY};
                    final long[][] collectedSuperTimes = {new long[collectedSize[0]]};
                    final long[][] collectedSuperTimesAssociatedWorlds = {new long[collectedSize[0]]};
                    final int[] insert_index = {0};

                    long previousDivergenceTime = endOfSearch;
                    for (int i = 0; i < collectedWorldsSize; i++) {
                        final TimeTreeChunk timeTree = (TimeTreeChunk) superTimeTrees[i];
                        if (timeTree != null) {
                            long currentDivergenceTime = objectWorldOrder.get(collectedWorlds[i]);
                            if (currentDivergenceTime < beginningOfSearch) {
                                currentDivergenceTime = beginningOfSearch;
                            }
                            final long finalPreviousDivergenceTime = previousDivergenceTime;
                            timeTree.range(currentDivergenceTime, previousDivergenceTime, CoreConstants.END_OF_TIME, new TreeWalker() {
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

    private void resolveTimepointsFromSuperTimes(final WorldOrderChunk globalWorldOrder, final WorldOrderChunk objectWorldOrder, final org.mwg.Node node, final long beginningOfSearch, final long endOfSearch, final long[] collectedWorlds, final long[] collectedSuperTimes, final int collectedSize, final Callback<long[]> callback) {
        final MWGResolver selfPointer = this;

        final long[] timeTreeKeys = new long[collectedSize * 3];
        final byte[] types = new byte[collectedSize];
        for (int i = 0; i < collectedSize; i++) {
            timeTreeKeys[i * 3] = collectedWorlds[i];
            timeTreeKeys[i * 3 + 1] = collectedSuperTimes[i];
            timeTreeKeys[i * 3 + 2] = node.id();
            types[i] = CoreConstants.TIME_TREE_CHUNK;
        }
        getOrLoadAndMarkAll(types, timeTreeKeys, new Callback<Chunk[]>() {
            @Override
            public void on(Chunk[] timeTrees) {
                if (timeTrees == null) {
                    selfPointer._space.unmarkChunk(objectWorldOrder);
                    selfPointer._space.unmarkChunk(globalWorldOrder);
                    callback.on(new long[0]);
                } else {
                    //time collector
                    final int[] collectedTimesSize = {CoreConstants.MAP_INITIAL_CAPACITY};
                    final long[][] collectedTimes = {new long[collectedTimesSize[0]]};
                    final int[] insert_index = {0};
                    long previousDivergenceTime = endOfSearch;
                    for (int i = 0; i < collectedSize; i++) {
                        final TimeTreeChunk timeTree = (TimeTreeChunk) timeTrees[i];
                        if (timeTree != null) {
                            long currentDivergenceTime = objectWorldOrder.get(collectedWorlds[i]);
                            if (currentDivergenceTime < beginningOfSearch) {
                                currentDivergenceTime = beginningOfSearch;
                            }
                            final long finalPreviousDivergenceTime = previousDivergenceTime;
                            timeTree.range(currentDivergenceTime, previousDivergenceTime, CoreConstants.END_OF_TIME, new TreeWalker() {
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
                            if (i < collectedSize - 1) {
                                if (collectedWorlds[i + 1] != collectedWorlds[i]) {
                                    //world overriding semantic
                                    previousDivergenceTime = currentDivergenceTime;
                                }
                            }
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
        StringLongMap dictionaryIndex = (StringLongMap) this.dictionary.get(0);
        if (dictionaryIndex == null) {
            dictionaryIndex = (StringLongMap) this.dictionary.getOrCreate(0, Type.STRING_LONG_MAP);
        }
        long encodedKey = dictionaryIndex.getValue(name);
        if (encodedKey == CoreConstants.NULL_LONG) {
            dictionaryIndex.put(name, CoreConstants.NULL_LONG);
            encodedKey = dictionaryIndex.getValue(name);
        }
        return encodedKey;
    }

    @Override
    public String longKeyToString(long key) {
        StringLongMap dictionaryIndex = (StringLongMap) this.dictionary.get(0);
        if (dictionaryIndex != null) {
            return dictionaryIndex.getKey(key);
        }
        return null;
    }

}
