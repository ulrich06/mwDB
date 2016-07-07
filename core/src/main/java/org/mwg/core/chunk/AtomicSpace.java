package org.mwg.core.chunk;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.plugin.Chunk;
import org.mwg.plugin.ChunkIterator;
import org.mwg.plugin.ChunkSpace;
import org.mwg.struct.Buffer;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @ignore ts
 */
public class AtomicSpace implements ChunkSpace {

    private final ChunkSpace space;
    private final ReentrantLock lock = new ReentrantLock();

    public AtomicSpace(ChunkSpace space) {
        this.space = space;
    }

    @Override
    public Chunk create(byte type, long world, long time, long id, Buffer initialPayload, Chunk origin) {
        return this.space.create(type, world, time, id, initialPayload, origin);
    }

    @Override
    public Chunk getAndMark(byte type, long world, long time, long id) {
        lock.lock();
        try {
            return space.getAndMark(type, world, time, id);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Chunk putAndMark(Chunk elem) {
        lock.lock();
        try {
            return space.putAndMark(elem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void getOrLoadAndMark(byte type, long world, long time, long id, Callback<Chunk> callback) {
        lock.lock();
        space.getOrLoadAndMark(type, world, time, id, new Callback<Chunk>() {
            @Override
            public void on(Chunk result) {
                lock.unlock();
                callback.on(result);
            }
        });
    }

    @Override
    public void unmark(byte type, long world, long time, long id) {
        lock.lock();
        try {
            space.unmark(type, world, time, id);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unmarkChunk(Chunk chunk) {
        lock.lock();
        try {
            space.unmarkChunk(chunk);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void freeChunk(Chunk chunk) {
        lock.lock();
        try {
            space.freeChunk(chunk);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void declareDirty(Chunk elem) {
        lock.lock();
        try {
            space.declareDirty(elem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void declareClean(Chunk elem) {
        lock.lock();
        try {
            space.declareClean(elem);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setGraph(Graph graph) {
        space.setGraph(graph);
    }

    @Override
    public Graph graph() {
        return space.graph();
    }

    @Override
    public void clear() {
        space.clear();
    }

    @Override
    public void free() {
        space.clear();
    }

    @Override
    public long size() {
        return space.size();
    }

    @Override
    public long available() {
        return space.available();
    }

    @Override
    public ChunkIterator detachDirties() {
        return space.detachDirties();
    }
}
