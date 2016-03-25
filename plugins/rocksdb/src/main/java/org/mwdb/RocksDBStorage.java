package org.mwdb;

import org.mwdb.chunk.KBuffer;
import org.mwdb.plugin.KStorage;
import org.rocksdb.*;

import java.io.File;

public class RocksDBStorage implements KStorage {

    private Options _options;

    private RocksDB _db;

    private KGraph _graph;

    private static final String _connectedError = "PLEASE CONNECT YOUR DATABASE FIRST";

    private boolean _isConnected = false;

    private final String _storagePath;

    public RocksDBStorage(String storagePath) {
        RocksDB.loadLibrary();
        this._storagePath = storagePath;
    }

    @Override
    public void get(KBuffer[] keys, KCallback<KBuffer[]> callback) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = keys.length;
        KBuffer[] result = new KBuffer[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            try {
                byte[] res = _db.get(keys[i].data());
                if (res != null) {
                    KBuffer newBuf = _graph.newBuffer();
                    int ii = 0;
                    while (ii < res.length) {
                        newBuf.write(res[ii]);
                        ii++;
                    }
                    result[i] = newBuf;
                } else {
                    result[i] = null;
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
    public void put(KBuffer[] p_keys, KBuffer[] p_values, KCallback<Boolean> p_callback) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = p_keys.length;
        WriteBatch batch = new WriteBatch();
        for (int i = 0; i < nbKeys; i++) {
            batch.put(p_keys[i].data(), p_values[i].data());
        }
        WriteOptions options = new WriteOptions();
        options.setSync(false);
        try {
            _db.write(options, batch);
            if (p_callback != null) {
                p_callback.on(true);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
            if (p_callback != null) {
                p_callback.on(false);
            }
        }
    }

    @Override
    public void remove(KBuffer[] keys, KCallback<Boolean> callback) {
        if (!_isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = keys.length;
        try {
            for (int i = 0; i < nbKeys; i++) {
                _db.remove(keys[i].data());
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
    public void disconnect(Short prefix, KCallback<Boolean> callback) {
        //TODO write the prefix
        try {
            WriteOptions options = new WriteOptions();
            options.sync();
            _db.write(options, new WriteBatch());
            _db.close();
            _options.dispose();
            _options = null;
            _db = null;
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

    private static final byte[] prefixKey = "prefix".getBytes();

    @Override
    public void connect(KGraph graph, KCallback<Short> callback) {
        if (_isConnected) {
            if (callback != null) {
                callback.on(null);
            }
            return;
        }
        _graph = graph;
        //by default activate snappy compression of bytes
        _options = new Options()
                .setCreateIfMissing(true)
                .setCompressionType(CompressionType.SNAPPY_COMPRESSION);
        File location = new File(_storagePath);
        if (!location.exists()) {
            location.mkdirs();
        }
        File targetDB = new File(location, "data");
        targetDB.mkdirs();
        try {
            _db = RocksDB.open(_options, targetDB.getAbsolutePath());
            _isConnected = true;

            byte[] current = _db.get(prefixKey);
            if (current == null) {
                current = new String("0").getBytes();
            }
            Short currentPrefix = Short.parseShort(new String(current));
            _db.put(prefixKey, ((currentPrefix + 1) + "").getBytes());

            if (callback != null) {
                callback.on(currentPrefix);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(null);
            }
        }
    }

}
