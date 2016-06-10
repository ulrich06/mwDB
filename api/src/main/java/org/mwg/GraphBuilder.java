package org.mwg;

import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.Storage;

/**
 * Creates an instance of a Graph, with several customizable features.
 */
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

    /**
     * Activates the use of Off-Heap memory mechanisms
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder withOffHeapMemory() {
        this._offHeap = true;
        return this;
    }

    /**
     * Sets the storage system to the given parameter.
     *
     * @param storage the storage system to be used by the graph
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder withStorage(Storage storage) {
        this._storage = storage;
        return this;
    }

    /**
     * Sets the storage system to the given parameter, in read-only mode.
     *
     * @param storage the storage system to be used by the graph in read-only mode
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder withReadOnlyStorage(Storage storage) {
        this._storage = storage;
        _readOnly = true;
        return this;
    }

    /**
     * Sets the maximum size of the memory that can be used before automated unload.
     * @param numberOfElements the number of elements in memory before unloading
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder withMemorySize(long numberOfElements) {
        this._memorySize = numberOfElements;
        return this;
    }

    /**
     * Triggers a serialization of teh graph every time the memory reaches the {@code numberOfElements}.
     * @param numberOfElements the serialization trigger level
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder saveEvery(long numberOfElements) {
        this._saveBatchSize = numberOfElements;
        return this;
    }

    /**
     * Sets the scheduler to be used by the graph
     * @param scheduler an instance of scheduler
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder withScheduler(Scheduler scheduler) {
        this._scheduler = scheduler;
        return this;
    }

    /**
     * Adds a new type of node that can be hosted in the graph, by setting a factory for them.
     * @param nodeFactory the factory for a new kind of nodes
     * @return the {@link GraphBuilder}, for a fluent API
     */
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

    /**
     * Adds new types of node that can be hosted in the graph, by providing factories for them.
     * @param nodeFactories an array of node factories.
     * @return the {@link GraphBuilder}, for a fluent API
     */
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

    /**
     * Activates the use of garbage collection mechanism.
     * @return the {@link GraphBuilder}, for a fluent API
     */
    public GraphBuilder withGC() {
        this._gc = true;
        return this;
    }



    /**
     * To call oce all options have been set, to actually create a graph instance.
     * @return the {@link Graph}
     *
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
