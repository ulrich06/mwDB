package org.mwg;

import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.Storage;

public class GraphBuilder {

    public static GraphBuilder builder() {
        return new GraphBuilder();
    }

    private Storage _storage = null;
    private Scheduler _scheduler = null;
    private NodeFactory[] _factories = null;
    private boolean _offHeap = false;
    private boolean _gc = false;
    private long _memorySize = -1;
    private long _saveBatchSize = -1;

    private static InternalBuilder internalBuilder = null;

    public interface InternalBuilder {
        Graph newGraph(Storage storage, Scheduler scheduler, NodeFactory[] factories, boolean usingGC, boolean usingOffHeapMemory, long memorySize, long autoSaveSize);
    }

    private GraphBuilder() {
        //NOOP
    }

    public GraphBuilder withStorage(Storage p_storage) {
        this._storage = p_storage;
        return this;
    }

    public GraphBuilder withScheduler(Scheduler p_scheduler) {
        this._scheduler = p_scheduler;
        return this;
    }

    public GraphBuilder withFactory(NodeFactory p_factory) {
        if (_factories == null) {
            _factories = new NodeFactory[1];
            _factories[0] = p_factory;
        } else {
            NodeFactory[] _factories2 = new NodeFactory[_factories.length + 1];
            System.arraycopy(_factories, 0, _factories2, 0, _factories.length);
            _factories2[_factories.length] = p_factory;
            _factories = _factories2;
        }
        return this;
    }

    public GraphBuilder withGC() {
        this._gc = true;
        return this;
    }

    public GraphBuilder withOffHeapMemory() {
        this._offHeap = true;
        return this;
    }

    public GraphBuilder withMemorySize(long size) {
        this._memorySize = size;
        return this;
    }

    public GraphBuilder withAutoSave(long batchSize) {
        this._saveBatchSize = batchSize;
        return this;
    }

    public Graph build() {
        if (internalBuilder == null) {
            synchronized (this) {
                try {
                    internalBuilder = (InternalBuilder) getClass().getClassLoader().loadClass("org.mwg.core.Builder").newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return internalBuilder.newGraph(_storage, _scheduler, _factories, _gc, _offHeap, _memorySize, _saveBatchSize);
    }

}
