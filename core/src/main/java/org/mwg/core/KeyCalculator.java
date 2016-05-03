package org.mwg.core;

import org.mwg.Constants;

import java.util.concurrent.atomic.AtomicLong;

class KeyCalculator {

    /**
     * @native ts
     * private _prefix: string;
     */
    private final long _prefix;
    /**
     * @native ts
     * private _currentIndex: number;
     */
    private final AtomicLong _currentIndex;

    /**
     * @native ts
     * this._prefix = "0x" + prefix.toString(org.mwg.Constants.PREFIX_SIZE);
     * this._currentIndex = currentIndex;
     */
    public KeyCalculator(short prefix, long currentIndex) {
        this._prefix = ((long) prefix) << Constants.LONG_SIZE - Constants.PREFIX_SIZE;
        this._currentIndex = new AtomicLong(currentIndex);
    }

    /**
     * @native ts
     * if (this._currentIndex == org.mwg.Constants.KEY_PREFIX_MASK) {
     * throw new Error("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
     * }
     * this._currentIndex++;
     * var indexHex = this._currentIndex.toString(org.mwg.Constants.PREFIX_SIZE);
     * var objectKey = parseInt(this._prefix + "000000000".substring(0,9-indexHex.length) + indexHex, org.mwg.Constants.PREFIX_SIZE);
     * if (objectKey >= org.mwg.Constants.NULL_LONG) {
     * throw new Error("Object Index exceeds teh maximum JavaScript number capacity. (2^"+org.mwg.Constants.LONG_SIZE+")");
     * }
     * return objectKey;
     */
    long nextKey() {
        long nextIndex = _currentIndex.incrementAndGet();
        if (_currentIndex.get() == Constants.KEY_PREFIX_MASK) {
            throw new IndexOutOfBoundsException("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
        }
        //moves the prefix 53-size(short) times to the left;
        long objectKey = _prefix + nextIndex;
        if (objectKey >= Constants.END_OF_TIME) {
            throw new IndexOutOfBoundsException("Object Index exceeds teh maximum JavaScript number capacity. (2^" + Constants.LONG_SIZE + ")");
        }
        return objectKey;
    }

    /**
     * @native ts
     * return this._currentIndex;
     */
    long lastComputedIndex() {
        return _currentIndex.get();
    }

    /**
     * @native ts
     * return parseInt(this._prefix,org.mwg.Constants.PREFIX_SIZE);
     */
    public short prefix() {
        return (short) (_prefix >> Constants.LONG_SIZE - Constants.PREFIX_SIZE);
    }

}
