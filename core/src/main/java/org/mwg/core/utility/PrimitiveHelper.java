package org.mwg.core.utility;

import org.mwg.Callback;
import org.mwg.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PrimitiveHelper {

    /**
     * @native ts
     * public static PRIME1 : Long = Long.fromNumber(2654435761, true);
     */
    private static final long PRIME1 = 2654435761L;

    /**
     * @native ts
     * public static PRIME2 : Long = Long.fromNumber(2246822519, true);
     */
    private static final long PRIME2 = 2246822519L;

    /**
     * @native ts
     * public static PRIME3 : Long = Long.fromNumber(3266489917, true);
     */
    private static final long PRIME3 = 3266489917L;

    /**
     * @native ts
     * public static PRIME4 : Long = Long.fromNumber(668265263, true);
     */
    private static final long PRIME4 = 668265263L;

    /**
     * @native ts
     * public static PRIME5 : Long = Long.fromNumber(0x165667b1, true);
     */
    private static final long PRIME5 = 0x165667b1;

    private static final int len = 24;


    /**
     * @native ts
     * if (max <= 0) {
     * throw new Error("Max must be > 0");
     * }
     * var crc = org.mwg.core.utility.PrimitiveHelper.PRIME5;
     * crc = crc.add(number);
     * crc = crc.add(crc.shiftLeft(17));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME4);
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME1);
     * crc = crc.add(number);
     * crc = crc.add(crc.shiftLeft(17));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME4);
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME1);
     * crc = crc.add(org.mwg.core.utility.PrimitiveHelper.len);
     * crc = crc.xor(crc.shiftRightUnsigned(15));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME2);
     * crc = crc.add(number);
     * crc = crc.xor(crc.shiftRightUnsigned(13));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME3);
     * crc = crc.xor(crc.shiftRightUnsigned(16));
     * return (crc.getLowBits() & 0x7FFFFFFF) % max;
     */
    public static int intHash(int number, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("Max must be > 0");
        }

        long crc = PRIME5;
        crc += number;
        crc += crc << 17;
        crc *= PRIME4;
        crc *= PRIME1;
        crc += number;
        crc += crc << 17;
        crc *= PRIME4;
        crc *= PRIME1;
        crc += len;
        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc += number;
        crc ^= crc >>> 13;
        crc *= PRIME3;
        crc ^= crc >>> 16;
        crc = crc & 0x7FFFFFFF; //convert positive
        crc = crc % max;        // return between 0 and max

        return (int) crc;
    }

    /**
     * @native ts
     * if (max <= 0) {
     * throw new Error("Max must be > 0");
     * }
     * var crc = org.mwg.core.utility.PrimitiveHelper.PRIME5;
     * crc = crc.add(number);
     * crc = crc.add(crc.shiftLeft(17));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME4);
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME1);
     * crc = crc.add(number);
     * crc = crc.add(crc.shiftLeft(17));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME4);
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME1);
     * crc = crc.add(org.mwg.core.utility.PrimitiveHelper.len);
     * crc = crc.xor(crc.shiftRightUnsigned(15));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME2);
     * crc = crc.add(number);
     * crc = crc.xor(crc.shiftRightUnsigned(13));
     * crc = crc.mul(org.mwg.core.utility.PrimitiveHelper.PRIME3);
     * crc = crc.xor(crc.shiftRightUnsigned(16));
     * var res = crc.toNumber();
     * res = res % max;
     * return res;
     */
    public static long longHash(long number, long max) {
        if (max <= 0) {
            throw new IllegalArgumentException("Max must be > 0");
        }
        long crc = PRIME5;
        crc += number;
        crc += crc << 17;
        crc *= PRIME4;
        crc *= PRIME1;
        crc += number;
        crc += crc << 17;
        crc *= PRIME4;
        crc *= PRIME1;
        crc += len;
        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc += number;
        crc ^= crc >>> 13;
        crc *= PRIME3;
        crc ^= crc >>> 16;

        //To check later if we can replace by somthing better
        crc = crc & 0x7FFFFFFFFFFFFFFFL; //convert positive
        crc = crc % max;           // return between 0 and max

        return crc;
    }

    /**
     * @native ts
     * return Long.UZERO.toNumber();
     */
    public static long tripleHash(byte p0, long p1, long p2, long p3, long max) {
        if (max <= 0) {
            throw new IllegalArgumentException("Max must be > 0");
        }

        long v1 = PRIME5;
        long v2 = v1 * PRIME2 + len;
        long v3 = v2 * PRIME3;
        long v4 = v3 * PRIME4;

        long crc;

        v1 = ((v1 << 13) | (v1 >>> 51)) + p1;
        v2 = ((v2 << 11) | (v2 >>> 53)) + p2;
        v3 = ((v3 << 17) | (v3 >>> 47)) + p3;
        v4 = ((v4 << 19) | (v4 >>> 45)) + p0;

        v1 += ((v1 << 17) | (v1 >>> 47));
        v2 += ((v2 << 19) | (v2 >>> 45));
        v3 += ((v3 << 13) | (v3 >>> 51));
        v4 += ((v4 << 11) | (v4 >>> 53));

        v1 *= PRIME1;
        v2 *= PRIME1;
        v3 *= PRIME1;
        v4 *= PRIME1;

        v1 += p1;
        v2 += p2;
        v3 += p3;
        v4 += PRIME5;

        v1 *= PRIME2;
        v2 *= PRIME2;
        v3 *= PRIME2;
        v4 *= PRIME2;

        v1 += ((v1 << 11) | (v1 >>> 53));
        v2 += ((v2 << 17) | (v2 >>> 47));
        v3 += ((v3 << 19) | (v3 >>> 45));
        v4 += ((v4 << 13) | (v4 >>> 51));

        v1 *= PRIME3;
        v2 *= PRIME3;
        v3 *= PRIME3;
        v4 *= PRIME3;

        crc = v1 + ((v2 << 3) | (v2 >>> 61)) + ((v3 << 6) | (v3 >>> 58)) + ((v4 << 9) | (v4 >>> 55));
        crc ^= crc >>> 11;
        crc += (PRIME4 + len) * PRIME1;
        crc ^= crc >>> 15;
        crc *= PRIME2;
        crc ^= crc >>> 13;

        //To check later if we can replace by somthing better
        crc = crc & 0x7FFFFFFFFFFFFFFFL; //convert positive
        crc = crc % max;           // return between 0 and max

        return crc;
    }


    /**
     * @native ts
     * return Math.random() * 1000000
     */
    public static long rand() {
        return (long) (Math.random() * Constants.END_OF_TIME);
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
     * return Number.MIN_VALUE;
     */
    public static double DOUBLE_MIN_VALUE() {
        return Double.MIN_VALUE;
    }

    /**
     * @native ts
     * return Number.MAX_VALUE;
     */
    public static double DOUBLE_MAX_VALUE() {
        return Double.MAX_VALUE;
    }

    /**
     * @native ts
     * var hash = 0;
     * if (target.length == 0) return hash;
     * for (var i = 0; i < target.length; i++) {
     * var charC = target.charCodeAt(i);
     * hash = ((hash * 32) - hash) + charC;
     * hash = hash & hash; // Convert to 32bit integer
     * }
     * return hash;
     */
    public static int stringHash(String target) {
        return target.hashCode();
    }

    /**
     * @native ts
     * return param != undefined && param != null;
     */
    public static boolean isDefined(Object param) {
        return param != null;
    }

    /**
     * @native ts
     * if(Array.isArray(elem)){
     * for(var p in elem){
     * callback.on(p);
     * }
     * return true;
     * }
     * return false;
     */
    public static boolean iterate(Object elem, Callback<Object> callback) {
        if (elem instanceof Object[]) {
            Object[] castedObjArray = (Object[]) elem;
            for (int i = 0; i < castedObjArray.length; i++) {
                callback.on(castedObjArray[i]);
            }
            return true;
        } else if (elem instanceof boolean[]) {
            boolean[] castedBoolArray = (boolean[]) elem;
            for (int i = 0; i < castedBoolArray.length; i++) {
                callback.on(castedBoolArray[i]);
            }
            return true;
        } else if (elem instanceof byte[]) {
            byte[] casted = (byte[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof short[]) {
            short[] casted = (short[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof char[]) {
            char[] casted = (char[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof int[]) {
            int[] casted = (int[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof long[]) {
            long[] casted = (long[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof float[]) {
            float[] casted = (float[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof double[]) {
            double[] casted = (double[]) elem;
            for (int i = 0; i < casted.length; i++) {
                callback.on(casted[i]);
            }
            return true;
        } else if (elem instanceof Iterable) {
            Iterator it = ((Iterable) elem).iterator();
            while (it.hasNext()) {
                callback.on(it.next());
            }
            return true;
        } else {
            return false;
        }
    }

}
