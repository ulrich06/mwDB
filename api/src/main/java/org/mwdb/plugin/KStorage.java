package org.mwdb.plugin;

import org.mwdb.KCallback;

/**
 * KStorage defines the interface any storage solution must comply with to be plugged to mwDB.
 */
public interface KStorage {

    /**
     * Used to retrieve objects from the storage.<br>
     * The {@code keys} array is a sequential list of &lt;world, timepoint, id&gt; tuples organized as follows:<br>
     * Say you wanna retrieve objects &lt;1, 2, 3&gt; and &lt;1, 5, 6&gt;, the array will be: [1,2,3,1,5,6]
     *
     * @param keys     The array of keys as specified above.
     * @param callback Is called when all objects are collected.
     *                 The size of the array in parameter is always 1/3 of the keys array size.
     *                 Objects in the result array are in the same order as the keys.
     */
    void get(long[] keys, KCallback<KBuffer[]> callback);

    void atomicGetIncrement(long[] key, KCallback<Short> callback);

    /**
     * Used to push objects to the storage.<br>
     * The {@code keys} array is a sequential list of &lt;world, timepoint, id&gt; tuples organized as follows:<br>
     * Say you wanna save objects &lt;1, 2, 3&gt; and &lt;1, 5, 6&gt;, the array will be: [1,2,3,1,5,6]
     *
     * @param keys            The array of keys as specified above.
     * @param values          The objects to store in a String format, and in the same order as the keys.
     * @param callback        Called when the operation is complete. The parameter will be true if the operation succeeded, false otherwise.
     * @param excludeListener Specifies whether or not the listeners must be notified.
     */
    void put(long[] keys, KBuffer[] values, KCallback<Boolean> callback, int excludeListener);

    /**
     * Called to remove objects from the storage.
     * The {@code keys} array is a sequential list of &lt;world, timepoint, id&gt; tuples organized as follows:<br>
     * Say you wanna remove objects &lt;1, 2, 3&gt; and &lt;1, 5, 6&gt;, the array will be: [1,2,3,1,5,6]
     *
     * @param keys     The array of keys as specified above.
     * @param callback Is called when all keys are deleted.
     */
    void remove(long[] keys, KCallback<Boolean> callback);

    /**
     * Connects the storage
     *
     * @param callback Called when the connection process is complete. The parameter will be true if the operation succeeded, false otherwise.
     */
    void connect(KCallback<Boolean> callback);

    /**
     * Disconnects the storage
     *
     * @param callback Called when the disconnection process is complete. The parameter will be true if the operation succeeded, false otherwise.
     */
    void disconnect(KCallback<Boolean> callback);

    /**
     * KBuffer defines the interface to exchange byte[] (payload), between KStorage and the various KChunk
     */
    interface KBuffer {

        /**
         * Append a byte to the buffer
         *
         * @param b
         */
        void write(Byte b);

        /**
         * Read the buffer at a precise position
         *
         * @param position index in the buffer
         * @return read byte
         */
        byte read(long position);

        /**
         * Size of the buffer
         *
         * @return length of the buffer
         */
        long size();

        /**
         * Free the buffer from memory, this method should be the last called
         */
        void free();

    }

    KBuffer newBuffer();

}