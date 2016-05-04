package org.mwg.ws;

/**
 * Created by ludovicmouline on 02/05/16.
 */
public class WSMessageType {
    public static final byte RQST_GET = 0;
    public static final byte RESP_GET = 1;

    public static final byte RQST_PUT = 2;
    public static final byte RESP_PUT = 3;

    public static final byte RQST_REMOVE = 3;
    public static final byte RESP_REMOVE = 4;

    public static final byte RQST_FORCE_RELOAD = 5;

    public static final byte RQST_PREFIX = 6;
    public static final byte RESP_PREFIX = 7;
}
