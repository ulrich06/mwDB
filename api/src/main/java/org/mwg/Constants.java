package org.mwg;

/**
 * Class that contains static constants used in Many World Graph
 */
public class Constants {

    // Limit long lengths to 53 bits because of JS limitation

    public static final int LONG_SIZE = 53;

    public static final int PREFIX_SIZE = 16;

    public static final long BEGINNING_OF_TIME = -0x001FFFFFFFFFFFFEl;

    public static final long END_OF_TIME = 0x001FFFFFFFFFFFFEl;

    public static final long NULL_LONG = 0x001FFFFFFFFFFFFFl;

    // Limit limit local index to LONG limit - prefix size
    public static final long KEY_PREFIX_MASK = 0x0000001FFFFFFFFFl;

    public static final String CACHE_MISS_ERROR = "Cache miss error";

    public static final char QUERY_SEP = ',';

    public static final char QUERY_KV_SEP = '=';

    public static final char TASK_SEP = '.';

    public static final char TASK_PARAM_OPEN = '(';

    public static final char TASK_PARAM_CLOSE = ')';

    /**
     * @native ts
     * return param != undefined && param != null;
     */
    public static boolean isDefined(Object param) {
        return param != null;
    }

    /**
     * @native ts
     * return src === other
     */
    public static boolean equals(String src, String other) {
        return src.equals(other);
    }

    /**
     * @native ts
     * public static BUFFER_SEP : number = "#".charCodeAt(0);
     */
    public static final byte BUFFER_SEP = '#';

    /**
     * Chunk Save/Load special chars
     */
    /**
     * @native ts
     * public static KEY_SEP : number = ";".charCodeAt(0);
     */
    public static final byte KEY_SEP = ';';

}

