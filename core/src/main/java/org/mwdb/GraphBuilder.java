package org.mwdb;

import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.heap.HeapChunkSpace;
import org.mwdb.manager.*;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.task.NoopScheduler;

public class GraphBuilder {

    /**
     * Builder creation
     */
    private GraphBuilder() {
        //noop
    }

    public static GraphBuilder builder() {
        return new GraphBuilder();
    }

    private KStorage _storage;

    private KChunkSpace _space;

    private KScheduler _scheduler;

    private KNodeTracker _tracker;

    public KStorage storage() {
        if (this._storage == null) {
            this._storage = new NoopStorage();
        }
        return _storage;
    }

    public KChunkSpace space() {
        if (this._space == null) {
            this._space = new HeapChunkSpace(100000, 1000);
        }
        return _space;
    }


    public KScheduler scheduler() {
        if (this._scheduler == null) {
            this._scheduler = new NoopScheduler();
        }
        return _scheduler;
    }

    public KNodeTracker tracker() {
        if (this._tracker == null) {
            this._tracker = new NoopNodeTracker();
        }
        return _tracker;
    }

    public GraphBuilder withStorage(KStorage p_storage) {
        this._storage = p_storage;
        return this;
    }

    public GraphBuilder withScheduler(KScheduler p_scheduler) {
        this._scheduler = p_scheduler;
        return this;
    }

    public GraphBuilder withSpace(KChunkSpace p_space) {
        this._space = p_space;
        return this;
    }

    public KGraph buildGraph() {
        KStorage storagePlugin = storage();
        KChunkSpace spacePlugin = space();
        KNodeTracker nodeTrackerPlugin = tracker();
        KScheduler schedulerPlugin = scheduler();
        return new Graph(storagePlugin, spacePlugin, schedulerPlugin, new MWGResolver(storagePlugin, spacePlugin, nodeTrackerPlugin, schedulerPlugin));
    }

}
