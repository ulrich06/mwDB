package org.mwdb;

import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.heap.HeapChunkSpace;
import org.mwdb.manager.*;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;

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
            this._space = new HeapChunkSpace(100000, 10);
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



    /*

    private KBlas _blas;

    private KChunkSpaceManager _spaceManager;
    */



    /*
    public KBlas blas() {
        if (this._blas == null) {
            this._blas = new JavaBlas();
        }
        return _blas;
    }*/


    /**
     * @native ts
     * if (this._spaceManager == null) { this._spaceManager = new org.kevoree.modeling.memory.space.impl.ManualChunkSpaceManager(); }
     * return this._spaceManager;
     */

    /*
    public KChunkSpaceManager spaceManager() {
        if (this._spaceManager == null) {
            this._spaceManager = new PhantomQueueChunkSpaceManager();
        }
        return _spaceManager;
    }*/
    public GraphBuilder withStorage(KStorage p_storage) {
        this._storage = p_storage;
        return this;
    }

    /*
    public DataManagerBuilder withScheduler(KScheduler p_scheduler) {
        this._scheduler = p_scheduler;
        return this;
    }

    public DataManagerBuilder withSpace(KChunkSpace p_space) {
        this._space = p_space;
        return this;
    }

    public DataManagerBuilder withSpaceManager(KChunkSpaceManager p_spaceManager) {
        this._spaceManager = p_spaceManager;
        return this;
    }

    public DataManagerBuilder withBlas(KBlas p_blas) {
        this._blas = p_blas;
        return this;
    }*/

    public KGraph buildGraph() {
        KStorage storagePlugin = storage();
        KChunkSpace chunkSpace = space();
        KNodeTracker nodeTracker = tracker();
        return new Graph(storagePlugin, chunkSpace, scheduler(), new MWGResolver(storagePlugin, chunkSpace, nodeTracker));
    }

    /*
    public KGraph t() {
        return new Graph(storage(), memory(), scheduler());
    }

    public KGraph buildBasicGraph() {
        return new Graph(storage(), memory(), scheduler());
    }*/


}
