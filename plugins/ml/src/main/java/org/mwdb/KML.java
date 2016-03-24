package org.mwdb;

import org.mwdb.ml.StatNode;
import org.mwdb.ml.PolynomialNode;

public class KML {

    public static KPolynomialNode polynomialNode(KNode node) {
        return new PolynomialNode(node);
    }

    public static KStatNode meanNode(KNode node) {
        return new StatNode(node);
    }

}