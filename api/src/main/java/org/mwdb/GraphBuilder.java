package org.mwdb;

import org.mwdb.plugin.KFactory;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;

public class GraphBuilder {

    public static GraphBuilder builder() {
        return new GraphBuilder();
    }

    private KStorage _storage = null;
    private KScheduler _scheduler = null;
    private KFactory[] _factories = null;
    private boolean _offHeap = false;
    private boolean _gc = false;
    private long _memorySize = -1;
    private long _saveBatchSize = -1;

    protected static InternalBuilder internalBuilder = null;

    protected interface InternalBuilder {
        KGraph newGrap(KStorage storage, KScheduler scheduler, KFactory[] factories, boolean usingGC, boolean usingOffHeapMemory, long memorySize, long autoSaveSize);
    }

    private GraphBuilder() {
        //NOOP
    }

    public GraphBuilder withStorage(KStorage p_storage) {
        this._storage = p_storage;
        return this;
    }

    public GraphBuilder withScheduler(KScheduler p_scheduler) {
        this._scheduler = p_scheduler;
        return this;
    }

    public GraphBuilder withFactory(KFactory p_factory) {
        if (_factories == null) {
            _factories = new KFactory[1];
            _factories[0] = p_factory;
        } else {
            KFactory[] _factories2 = new KFactory[_factories.length + 1];
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

    public KGraph build() {
        if (internalBuilder == null) {
            synchronized (this) {
                try {
                    internalBuilder = (InternalBuilder) getClass().getClassLoader().loadClass("org.mwdb.Builder").newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return internalBuilder.newGrap(_storage, _scheduler, _factories, _gc, _offHeap, _memorySize, _saveBatchSize);
    }

}
