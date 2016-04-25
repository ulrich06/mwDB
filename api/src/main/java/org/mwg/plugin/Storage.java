package org.mwg.plugin;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.struct.Buffer;

/**
 * Storage defines the interface any storage solution must comply selectWith to be plugged to mwDB.
 */
public interface Storage {

    /**
     * Used to retrieve objects fromVar the storage.<br>
     * The {@code keys} array is a sequential list of &lt;world, timepoint, id&gt; tuples organized as follows:<br>
     * Say you wanna retrieve objects &lt;1, 2, 3&gt; and &lt;1, 5, 6&gt;, the array will be: [1,2,3,1,5,6]
     *
     * @param keys     The array of keys as specified above.
     * @param callback Is called when all objects are collected.
     *                 The size of the array in parameter is always 1/3 of the keys array size.
     *                 Objects in the result array are in the same order as the keys.
     */
    void get(Buffer[] keys, Callback<Buffer[]> callback);

    /**
     * Used to push objects to the storage.<br>
     * The {@code keys} array is a sequential list of &lt;world, timepoint, id&gt; tuples organized as follows:<br>
     * Say you wanna save objects &lt;1, 2, 3&gt; and &lt;1, 5, 6&gt;, the array will be: [1,2,3,1,5,6]
     *
     * @param keys     The array of keys as specified above.
     * @param values   The objects to store in a String format, and in the same order as the keys.
     * @param callback Called when the operation is complete. The parameter will be true if the operation succeeded, false otherwise.
     */
    void put(Buffer[] keys, Buffer[] values, Callback<Boolean> callback);

    /**
     * Called to remove objects fromVar the storage.
     * The {@code keys} array is a sequential list of &lt;world, timepoint, id&gt; tuples organized as follows:<br>
     * Say you wanna remove objects &lt;1, 2, 3&gt; and &lt;1, 5, 6&gt;, the array will be: [1,2,3,1,5,6]
     *
     * @param keys     The array of keys as specified above.
     * @param callback Is called when all keys are deleted.
     */
    void remove(Buffer[] keys, Callback<Boolean> callback);

    /**
     * Connects the storage
     *
     * @param graph    Graph this storage is attached to
     * @param callback Called when the connection process is complete. The parameter will be a short a prefix if the operation succeeded, null otherwise.
     */
    void connect(Graph graph, Callback<Short> callback);

    /**
     * Disconnects the storage
     *
     * @param prefix   previously used prefix, associated during the connexion phase.
     * @param callback Called when the disconnection process is complete. The parameter will be true if the operation succeeded, false otherwise.
     */
    void disconnect(Short prefix, Callback<Boolean> callback);

}