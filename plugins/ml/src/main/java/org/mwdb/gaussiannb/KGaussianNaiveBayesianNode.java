package org.mwdb.gaussiannb;

/**
 * Created by Andrey Boytsov on 4/14/2016.
 */
public interface KGaussianNaiveBayesianNode {

    /**
     * @return Whether the node is in bootstrap (i.e.
     * re-learning) mode
     */
    boolean isInBootstrapMode();

}
