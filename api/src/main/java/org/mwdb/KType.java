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

    public static String typeName(byte p_type) {
        switch (p_type) {
            /** Primitives */
            case KType.BOOL:
                return "boolean";
            case KType.STRING:
                return "string";
            case KType.LONG:
                return "long";
            case KType.INT:
                return "int";
            case KType.DOUBLE:
                return "double";
            /** Arrays */
            case KType.DOUBLE_ARRAY:
                return "double[]";
            case KType.LONG_ARRAY:
                return "long[]";
            case KType.INT_ARRAY:
                return "int[]";
            /** Maps */
            case KType.LONG_LONG_MAP:
                return "map(long->long)";
            case KType.LONG_LONG_ARRAY_MAP:
                return "map(long->long[])";
            case KType.STRING_LONG_MAP:
                return "map(string->long)";
            default:
                return "unknown";
        }
    }

}
