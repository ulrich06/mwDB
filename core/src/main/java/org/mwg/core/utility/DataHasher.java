package org.mwg.core.utility;

import org.mwg.Constants;

public class DataHasher {

    /**
     * @native ts
     * private static byteTable = function(){
     * var table = [];
     * var h = Long.fromBits(0xCAAF1684, 0x544B2FBA);
     * for (var i = 0; i < 256; i++) {
     * for (var j = 0; j < 31; j++) {
     * h = h.shiftRightUnsigned(7).xor(h);
     * h = h.shiftLeft(11).xor(h);
     * h = h.shiftRightUnsigned(10).xor(h);
     * }
     * table[i] = h.toSigned();
     * }
     * return table;
     * }();
     */
    private static final long[] byteTable = createLookupTable();

    /**
     * @native ts
     * private static HSTART : Long = Long.fromBits(0xA205B064, 0xBB40E64D);
     */
    private static final long HSTART = 0xBB40E64DA205B064L;
    //0xBB40E64DA205B064

    /**
     * @native ts
     * private static HMULT : Long = Long.fromBits(0xE116586D,0x6A5D39EA);
     */
    private static final long HMULT = 7664345821815920749L;
    //0x6A5D39EAE116586D


    /**
     * @native ts
     * var h = org.mwg.core.utility.DataHasher.HSTART;
     * var dataLength = data.length;
     * for (var i = 0; i < dataLength; i++) {
     * h = h.mul(org.mwg.core.utility.DataHasher.HMULT).xor(org.mwg.core.utility.DataHasher.byteTable[data.charCodeAt(i) & 0xff]);
     * }
     * return h.mod(org.mwg.core.CoreConstants.END_OF_TIME).toNumber();
     */
    public static long hash(String data) {
        long h = HSTART;
        final long hmult = HMULT;
        final long[] ht = byteTable;
        int dataLength = data.length();
        for (int i = 0; i < dataLength; i++) {
            h = (h * hmult) ^ ht[data.codePointAt(i) & 0xff];
        }
        return h % Constants.END_OF_TIME;
    }

    /**
     * @native ts
     * var h = org.mwg.core.utility.DataHasher.HSTART;
     * var dataLength = data.length;
     * for (var i = 0; i < dataLength; i++) {
     * h = h.mul(org.mwg.core.utility.DataHasher.HMULT).xor(org.mwg.core.utility.DataHasher.byteTable[data[i] & 0xff]);
     * }
     * return h.mod(org.mwg.core.CoreConstants.END_OF_TIME).toNumber();
     */
    public static long hashBytes(byte[] data) {
        long h = HSTART;
        final long hmult = HMULT;
        final long[] ht = byteTable;
        int dataLength = data.length;
        for (int i = 0; i < dataLength; i++) {
            h = (h * hmult) ^ ht[data[i] & 0xff];
        }
        return h % Constants.END_OF_TIME;
    }


    /**
     * @ignore ts
     */
    private static final long[] createLookupTable() {
        long[] byteTable = new long[256];
        long h = 0x544B2FBACAAF1684L;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 31; j++) {
                h = (h >>> 7) ^ h;
                h = (h << 11) ^ h;
                h = (h >>> 10) ^ h;
            }
            byteTable[i] = h;
        }
        return byteTable;
    }

}