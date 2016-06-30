package org.mwg.core;

import org.mwg.GraphBuilder;
import org.mwg.core.chunk.heap.HeapChunkSpace;
import org.mwg.core.chunk.offheap.OffHeapChunkSpace;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.task.CoreTask;
import org.mwg.core.utility.ReadOnlyStorage;
import org.mwg.plugin.ChunkSpace;
import org.mwg.plugin.Plugin;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.Storage;
import org.mwg.task.Task;

public class Builder implements GraphBuilder.InternalBuilder {

    @Override
    public org.mwg.Graph newGraph(Storage p_storage, boolean p_readOnly, Scheduler p_scheduler, Plugin[] p_plugins, boolean p_usingGC, boolean p_usingOffHeapMemory, long p_memorySize, long p_autoSaveSize) {
        Storage storage = p_storage;
        if (storage == null) {
            storage = new BlackHoleStorage();
        }
        if (p_readOnly) {
            storage = new ReadOnlyStorage(storage);
        }
        Scheduler scheduler = p_scheduler;
        if (scheduler == null) {
            scheduler = new NoopScheduler();
        }
        NodeTracker nodeTracker;
        if (p_usingGC) {
            throw new RuntimeException("Not implemented yet !!!");
        } else {
            nodeTracker = new NoopNodeTracker();
        }
        ChunkSpace space;
        long memorySize = p_memorySize;
        if (memorySize == -1) {
            memorySize = 100000;
        }
        long autoSaveSize = p_autoSaveSize;
        if (p_autoSaveSize == -1) {
            autoSaveSize = memorySize;
        }
        space = createSpace(p_usingOffHeapMemory, memorySize, autoSaveSize);
        org.mwg.core.CoreGraph graph = new org.mwg.core.CoreGraph(storage, space, scheduler, new MWGResolver(storage, space, nodeTracker, scheduler), p_plugins);
        if (p_usingOffHeapMemory) {
            graph.offHeapBuffer = true;
        }
        return graph;
    }

    @Override
    public Task newTask() {
        return new CoreTask();
    }

    /**
     * @native ts
     * return new org.mwg.core.chunk.heap.HeapChunkSpace(memorySize,autoSaveSize);
     */
    private ChunkSpace createSpace(boolean usingOffHeapMemory, long memorySize, long autoSaveSize) {
        if (usingOffHeapMemory) {
            return new OffHeapChunkSpace(memorySize, autoSaveSize);
        } else {
            return new HeapChunkSpace((int) memorySize, (int) autoSaveSize);
        }
    }

}

