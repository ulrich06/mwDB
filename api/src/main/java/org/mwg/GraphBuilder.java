package org.mwg;

import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.Storage;

public class GraphBuilder {

    private Storage _storage = null;
    private Scheduler _scheduler = null;
    private NodeFactory[] _factories = null;
    private boolean _offHeap = false;
    private boolean _gc = false;
    private long _memorySize = -1;
    private long _saveBatchSize = -1;
    private boolean _readOnly = false;

    private static InternalBuilder _internalBuilder = null;

    public interface InternalBuilder {
        Graph newGraph(Storage storage, boolean readOnly, Scheduler scheduler, NodeFactory[] factories, boolean usingGC, boolean usingOffHeapMemory, long memorySize, long autoSaveSize);
    }

    public GraphBuilder withOffHeapMemory() {
        this._offHeap = true;
        return this;
    }

    public GraphBuilder withStorage(Storage storage) {
        this._storage = storage;
        return this;
    }

    public GraphBuilder withReadOnlyStorage(Storage storage) {
        this._storage = storage;
        _readOnly = true;
        return this;
    }

    public GraphBuilder withMemorySize(long numberOfElements) {
        this._memorySize = numberOfElements;
        return this;
    }

    public GraphBuilder saveEvery(long numberOfElements) {
        this._saveBatchSize = numberOfElements;
        return this;
    }

    public GraphBuilder withScheduler(Scheduler scheduler) {
        this._scheduler = scheduler;
        return this;
    }

    public GraphBuilder addNodeType(NodeFactory nodeFactory) {
        if (_factories == null) {
            _factories = new NodeFactory[1];
            _factories[0] = nodeFactory;
        } else {
            NodeFactory[] _factories2 = new NodeFactory[_factories.length + 1];
            System.arraycopy(_factories, 0, _factories2, 0, _factories.length);
            _factories2[_factories.length] = nodeFactory;
            _factories = _factories2;
        }
        return this;
    }

    public GraphBuilder addNodeTypes(NodeFactory[] nodeFactories) {
        if (_factories == null) {
            _factories = nodeFactories;
        } else {
            NodeFactory[] _factories2 = new NodeFactory[_factories.length + nodeFactories.length];
            System.arraycopy(_factories, 0, _factories2, 0, _factories.length);
            System.arraycopy(nodeFactories, 0, _factories2, 0, nodeFactories.length);
            _factories = _factories2;
        }
        return this;
    }

    public GraphBuilder withGC() {
        this._gc = true;
        return this;
    }


    /**
     * @native ts
     * if (org.mwg.GraphBuilder._internalBuilder == null) {
     * org.mwg.GraphBuilder._internalBuilder = new org.mwg.core.Builder();
     * }
     * return org.mwg.GraphBuilder._internalBuilder.newGraph(this._storage, this._readOnly, this._scheduler, this._factories, this._gc, this._offHeap, this._memorySize, this._saveBatchSize);
     */
    public Graph build() {
        if (_internalBuilder == null) {
            synchronized (this) {
                try {
                    _internalBuilder = (InternalBuilder) getClass().getClassLoader().loadClass("org.mwg.core.Builder").newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return _internalBuilder.newGraph(_storage, _readOnly, _scheduler, _factories, _gc, _offHeap, _memorySize, _saveBatchSize);
    }

}
