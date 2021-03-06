package org.mwg.core;

public class CoreConstants extends org.mwg.Constants {

    /**
     * @native ts
     * public static CHUNK_SEP : number = "|".charCodeAt(0);
     */
    public static final byte CHUNK_SEP = '|';

    /**
     * @native ts
     * public static CHUNK_SUB_SEP : number = ",".charCodeAt(0);
     */
    public static final byte CHUNK_SUB_SEP = ',';

    /**
     * @native ts
     * public static CHUNK_SUB_SUB_SEP : number = ":".charCodeAt(0);
     */
    public static final byte CHUNK_SUB_SUB_SEP = ':';

    /**
     * @native ts
     * public static CHUNK_SUB_SUB_SUB_SEP : number = "%".charCodeAt(0);
     */
    public static final byte CHUNK_SUB_SUB_SUB_SEP = '%';

    /**
     * ChunkFlags
     */
    //public static final short DIRTY_BIT_INDEX = 0;
    public static final int DIRTY_BIT = 0x01;

    //public static final short REMOVED_BIT_INDEX = 1;
    //public static final int REMOVED_BIT = 1 << REMOVED_BIT_INDEX;

    /**
     * Node constants
     **/
    public static final int PREVIOUS_RESOLVED_WORLD_INDEX = 0;
    public static final int PREVIOUS_RESOLVED_SUPER_TIME_INDEX = 1;
    public static final int PREVIOUS_RESOLVED_TIME_INDEX = 2;

    public static final int PREVIOUS_RESOLVED_WORLD_MAGIC = 3;
    public static final int PREVIOUS_RESOLVED_SUPER_TIME_MAGIC = 4;
    public static final int PREVIOUS_RESOLVED_TIME_MAGIC = 5;

    /**
     * Keys constants
     */
    //public static final int KEYS_SIZE = 3;

    public static final int PREFIX_TO_SAVE_SIZE = 2;

    public static final long[] NULL_KEY = new long[]{END_OF_TIME, END_OF_TIME, END_OF_TIME};

    public static final long[] GLOBAL_UNIVERSE_KEY = new long[]{NULL_LONG, NULL_LONG, NULL_LONG};

    public static final long[] GLOBAL_DICTIONARY_KEY = new long[]{NULL_LONG, 0, 0};

    public static final long[] GLOBAL_INDEX_KEY = new long[]{NULL_LONG, 1, 0};

    public static final String INDEX_ATTRIBUTE = "index";

    /**
     * Map constants
     */
    public static final int MAP_INITIAL_CAPACITY = 16;

    public static final float MAP_LOAD_FACTOR = ((float) 75 / (float) 100);

    /**
     * Error messages
     */
    public static final String DISCONNECTED_ERROR = "Please connect your graph, prior to any usage of it";

    /**
     * OffHeap
     */
    public static final int OFFHEAP_NULL_PTR = -1;
    public static final int OFFHEAP_CHUNK_INDEX_WORLD = 0;
    public static final int OFFHEAP_CHUNK_INDEX_TIME = 1;
    public static final int OFFHEAP_CHUNK_INDEX_ID = 2;
    public static final int OFFHEAP_CHUNK_INDEX_TYPE = 3;
    public static final int OFFHEAP_CHUNK_INDEX_FLAGS = 4;
    public static final int OFFHEAP_CHUNK_INDEX_MARKS = 5;


    /**
     * SuperTimeTree
     */
    public static final long SCALE_1 = 1000;
    public static final long SCALE_2 = 10000;
    public static final long SCALE_3 = 100000;
    public static final long SCALE_4 = 1000000;


    /**
     * Error messages
     */
    public static String DEAD_NODE_ERROR = "This Node has been tagged destroyed, please don't use it anymore!";

    /**
     * @native ts
     * public static BOOL_TRUE : number = "1".charCodeAt(0);
     */
    public static byte BOOL_TRUE = (byte) '1';
    /**
     * @native ts
     * public static BOOL_FALSE : number = "0".charCodeAt(0);
     */
    public static byte BOOL_FALSE = (byte) '0';

}
