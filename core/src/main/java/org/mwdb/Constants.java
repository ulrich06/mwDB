package org.mwdb;

public class Constants {

    /**
     * Configuration constants
     */
    public static final int CAS_MAX_TRY = 1000;

    public static final int CALLBACK_HISTORY = 1000;

    // Limit long lengths to 53 bits because of JS limitation

    public static final int LONG_SIZE = 53;

    public static final int PREFIX_SIZE = 16;

    public static final long BEGINNING_OF_TIME = -0x001FFFFFFFFFFFFEl;

    public static final long END_OF_TIME = 0x001FFFFFFFFFFFFEl;

    public static final long NULL_LONG = 0x001FFFFFFFFFFFFFl;

    // Limit limit local index to LONG limit - prefix size
    public static final long KEY_PREFIX_MASK = 0x0000001FFFFFFFFFl;

    public static final char KEY_SEP = '|';

    public static final char ELEM_SEP = ';';

    public static final char VAL_SEP = ',';

    public static final char CHUNK_ELEM_SEP = '%';

    public static final char CHUNK_VAL_SEP = '$';

    public static final int CACHE_INIT_SIZE = 16;

    public static final float CACHE_LOAD_FACTOR = ((float) 75 / (float) 100);

    /**
     * ChunkFlags
     */
    public static final short DIRTY_BIT_INDEX = 0;

    public static final int DIRTY_BIT = 1 << DIRTY_BIT_INDEX;

    public static final short REMOVED_BIT_INDEX = 1;

    public static final int REMOVED_BIT = 1 << REMOVED_BIT_INDEX;

    /**
     * ChunkTypes
     */
    public static final short STATE_CHUNK = 0;

    public static final short INDEX_STATE_CHUNK = 1;

    public static final short LONG_TREE = 2;

    public static final short LONG_LONG_MAP = 3;

}
