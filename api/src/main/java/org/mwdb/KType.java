package org.mwdb;

/**
 * Defines the constants used in mwDB.
 */
public class KType {

    /**
     * Primitive Types
     */
    public static final short BOOL = 1;
    public static final short STRING = 2;
    public static final short LONG = 3;
    public static final short INT = 4;
    public static final short DOUBLE = 5;

    /**
     * Primitive Arrays
     * SHOULD NOT BE USED OUTSIDE THE CORE IMPLEMENTATION.
     */
    public static final short DOUBLE_ARRAY = 6;
    public static final short LONG_ARRAY = 7;
    public static final short INT_ARRAY = 8;

    /**
     * Primitive Maps
     * SHOULD NOT BE USED OUTSIDE THE CORE IMPLEMENTATION.
     */
    public static final short LONG_LONG_MAP = 9;
    public static final short LONG_LONG_ARRAY_MAP = 10;
    public static final short STRING_LONG_MAP = 11;

}
