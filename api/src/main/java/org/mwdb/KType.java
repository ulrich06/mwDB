package org.mwdb;

/**
 * Defines the constants used in mwDB.
 */
public class KType {

    /**
     * Primitive Types
     */
    public static final byte BOOL = 1;
    public static final byte STRING = 2;
    public static final byte LONG = 3;
    public static final byte INT = 4;
    public static final byte DOUBLE = 5;

    /*
     * Primitive Arrays
     * SHOULD NOT BE USED OUTSIDE THE CORE IMPLEMENTATION.
     */
    public static final byte DOUBLE_ARRAY = 6;
    public static final byte LONG_ARRAY = 7;
    public static final byte INT_ARRAY = 8;

    /*
     * Primitive Maps
     * SHOULD NOT BE USED OUTSIDE THE CORE IMPLEMENTATION.
     */
    public static final byte LONG_LONG_MAP = 9;
    public static final byte LONG_LONG_ARRAY_MAP = 10;
    public static final byte STRING_LONG_MAP = 11;

}
