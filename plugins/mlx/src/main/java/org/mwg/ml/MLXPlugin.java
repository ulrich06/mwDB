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
        declareNodeType(GaussianClassifierNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianClassifierNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(BatchDecisionTreeNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new BatchDecisionTreeNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(GaussianNaiveBayesianNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianNaiveBayesianNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LinearRegressionSGDNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LinearRegressionSGDNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LogisticRegressionClassifierNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LogisticRegressionClassifierNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LinearRegressionBatchGDNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LinearRegressionBatchGDNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LinearRegressionNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LinearRegressionNode(world, time, id, graph, initialResolution);
            }
        });
    }

}
