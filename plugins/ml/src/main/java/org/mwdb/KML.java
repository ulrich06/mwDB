package org.mwdb;

import org.mwdb.gmm.GaussianNode;
import org.mwdb.gmm.KGaussianNode;

public class KML {

    public static KPolynomialNode polynomialNode(KNode node) {
        return new PolynomialNode(node);
    }

    public static KGaussianNode gaussianNode(KNode node) {
        return new GaussianNode(node);

    }
}
