package org.mwg.utils;


public class BytesIntConversion {
    private BytesIntConversion(){}

    public static byte[] toBytes(int integer) {
        byte[] result = new byte[4];
        result[0] = (byte) (integer >> 24);
        result[1] = (byte) (integer >> 16);
        result[2] = (byte) (integer >> 8);
        result[3] = (byte) (integer);

        return result;
    }

    public static int toInt(byte... bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
