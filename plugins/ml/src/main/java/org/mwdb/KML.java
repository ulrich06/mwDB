package org.mwdb;

import org.mwdb.ml.MeanNode;
import org.mwdb.ml.PolynomialNode;

public class KML {

    public static KPolynomialNode polynomialNode(KNode node) {
        return new PolynomialNode(node);
    }

    public static KMeanNode meanNode(KNode node) {
        return new MeanNode(node);
    }

}
