package org.mwg.ml;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.algorithm.anomalydetector.InterquartileRangeOutlierDetectorNode;
import org.mwg.ml.algorithm.classifier.BatchDecisionTreeNode;
import org.mwg.ml.algorithm.classifier.GaussianClassifierNode;
import org.mwg.ml.algorithm.classifier.GaussianNaiveBayesianNode;
import org.mwg.ml.algorithm.classifier.LogisticRegressionClassifierNode;
import org.mwg.ml.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.plugin.NodeFactory;

public class MLXPlugin extends MLPlugin {

    public MLXPlugin() {
        super();
    }

}
