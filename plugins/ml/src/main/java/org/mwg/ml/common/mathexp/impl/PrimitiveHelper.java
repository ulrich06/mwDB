package org.mwg.ml.common.mathexp.impl;

/**
 * Created by assaad on 23/03/16.
 */
public class PrimitiveHelper {
    public static boolean equals(String src, String other) {
        return src.equals(other);
    }

    /**
     * @native ts
     * return parseFloat(val);
     */
    public static double parseDouble(String val) {
        return Double.parseDouble(val);
    }
}
