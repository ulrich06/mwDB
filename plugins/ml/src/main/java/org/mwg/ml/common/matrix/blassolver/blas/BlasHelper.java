package org.mwg.ml.common.matrix.blassolver.blas;

import org.mwg.ml.common.matrix.TransposeType;

public class BlasHelper {

    private static final String TRANSPOSE_TYPE_CONJUCATE = "c";

    private static final String TRANSPOSE_TYPE_NOTRANSPOSE = "n";

    private static final String TRANSPOSE_TYPE_TRANSPOSE = "t";

    public static String transTypeToChar(TransposeType type) {
        if (type.equals(TransposeType.NOTRANSPOSE)) {
            return TRANSPOSE_TYPE_NOTRANSPOSE;
        } else if (type.equals(TransposeType.TRANSPOSE)) {
            return TRANSPOSE_TYPE_TRANSPOSE;
        }
        return null;
    }

}
