package org.mwg;

import java.io.File;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.mwg.struct.Buffer;
import org.mwg.plugin.Storage;
import org.mwg.struct.BufferIterator;

public class LevelDBStorage implements Storage {

    private static final String _connectedError = "PLEASE CONNECT YOUR DATABASE FIRST";
    private static final byte[] prefixKey = "prefix".getBytes();

    private final String storagePath;

    private DB db;
    private boolean isConnected;
    private Graph graph;

    public LevelDBStorage(String storagePath) {
        this.isConnected = false;
        this.storagePath = storagePath;
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        if (!isConnected) {
            throw new RuntimeException(_connectedError);
        }
        Buffer result = graph.newBuffer();
        BufferIterator it = keys.iterator();
        boolean isFirst = true;
        while (it.hasNext()) {
            Buffer view = it.next();
            try {
                if (!isFirst) {
                    result.write(Constants.BUFFER_SEP);
                } else {
                    isFirst = false;
                }
                byte[] res = db.get(view.data());
                if (res != null) {
                    result.writeAll(res);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (callback != null) {
            callback.on(result);
        }
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        if (!isConnected) {
            throw new RuntimeException(_connectedError);
        }
        try {
            WriteBatch batch = db.createWriteBatch();
            BufferIterator it = stream.iterator();
            while (it.hasNext()) {
                Buffer keyView = it.next();
                Buffer valueView = it.next();
                if (valueView != null) {
                    batch.put(keyView.data(), valueView.data());
                }
            }
            db.write(batch);
            if (callback != null) {
                callback.on(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(false);
            }
        }
    }


    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        if (!isConnected) {
            throw new RuntimeException(_connectedError);
        }
        try {
            BufferIterator it = keys.iterator();
            while (it.hasNext()) {
                Buffer view = it.next();
                db.delete(view.data());
            }
            if (callback != null) {
                callback.on(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(false);
            }
        }
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        try {
            db.close();
            db = null;
            if (callback != null) {
                callback.on(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(false);
            }
        }
    }


    @Override
    public void connect(Graph graph, Callback<Short> callback) {
        if (isConnected) {
            if (callback != null) {
                callback.on(null);
            }
            return;
        }
        this.graph = graph;
        //by default activate snappy compression of bytes
        Options options = new Options().createIfMissing(true).compressionType(CompressionType.SNAPPY);
        File location = new File(storagePath);
        if (!location.exists()) {
            location.mkdirs();
        }
        File targetDB = new File(location, "data");
        targetDB.mkdirs();
        try {
            db = JniDBFactory.factory.open(targetDB, options);
            isConnected = true;
            byte[] current = db.get(prefixKey);
            if (current == null) {
                current = new String("0").getBytes();
            }
            Short currentPrefix = Short.parseShort(new String(current));
            db.put(prefixKey, ((currentPrefix + 1) + "").getBytes());
            if (callback != null) {
                callback.on(currentPrefix);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(null);
            }
        }
    }

}
