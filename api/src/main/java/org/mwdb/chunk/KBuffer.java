package org.mwdb.chunk;

/**
 * KBuffer defines the interface to exchange byte[] (payload), between KStorage and the various KChunk
 */
public interface KBuffer {

    /**
     * Append a byte to the buffer
     *
     * @param b byte to append
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
