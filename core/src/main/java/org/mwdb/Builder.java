package org.mwdb;

import org.mwdb.chunk.KChunkSpace;
import org.mwdb.chunk.heap.HeapChunkSpace;
import org.mwdb.chunk.offheap.OffHeapChunkSpace;
import org.mwdb.manager.KNodeTracker;
import org.mwdb.manager.MWGResolver;
import org.mwdb.manager.NoopNodeTracker;
import org.mwdb.manager.NoopStorage;
import org.mwdb.plugin.KFactory;
import org.mwdb.plugin.KScheduler;
import org.mwdb.plugin.KStorage;
import org.mwdb.task.NoopScheduler;

class Builder implements GraphBuilder.InternalBuilder {

    @Override
    public KGraph newGrap(KStorage p_storage, KScheduler p_scheduler, KFactory[] p_factories, boolean p_usingGC, boolean p_usingOffHeapMemory, long p_memorySize, long p_autoSaveSize) {
        KStorage storage = p_storage;
        if (storage == null) {
            storage = new NoopStorage();
        }
        KScheduler scheduler = p_scheduler;
        if (scheduler == null) {
            scheduler = new NoopScheduler();
        }
        KNodeTracker nodeTracker;
        if (p_usingGC) {
            throw new RuntimeException("Not implemented yet !!!");
        } else {
            nodeTracker = new NoopNodeTracker();
        }
        KChunkSpace space;
        long memorySize = p_memorySize;
        if (memorySize == -1) {
            memorySize = 100_000;
        }
        long autoSaveSize = p_autoSaveSize;
        if (p_autoSaveSize == -1) {
            autoSaveSize = memorySize;
        }
        if (p_usingOffHeapMemory) {
            space = new OffHeapChunkSpace(memorySize, autoSaveSize);
        } else {
            space = new HeapChunkSpace((int) memorySize, (int) autoSaveSize);
        }
        return new Graph(storage, space, scheduler, new MWGResolver(storage, space, nodeTracker, scheduler), p_factories);
    }
}
