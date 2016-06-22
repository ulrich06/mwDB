package org.mwg.ml;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.algorithm.anomalydetector.InterquartileRangeOutlierDetectorNode;
import org.mwg.ml.algorithm.classifier.BatchDecisionTreeNode;
import org.mwg.ml.algorithm.classifier.GaussianClassifierNode;
import org.mwg.ml.algorithm.classifier.GaussianNaiveBayesianNode;
import org.mwg.ml.algorithm.classifier.LogisticRegressionClassifierNode;
import org.mwg.ml.algorithm.profiling.GaussianMixtureNode;
import org.mwg.ml.algorithm.profiling.GaussianSlotNode;
import org.mwg.ml.algorithm.regression.*;
import org.mwg.plugin.AbstractPlugin;
import org.mwg.plugin.NodeFactory;

public class MLPlugin extends AbstractPlugin {

    public MLPlugin() {
        super();
        //PolynomialNode
        declareNodeType(PolynomialNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new PolynomialNode(world, time, id, graph, initialResolution);
            }
        });
        //GaussianSlot
        declareNodeType(GaussianSlotNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianSlotNode(world, time, id, graph, initialResolution);
            }
        });
        //GaussianMixtureNode
        declareNodeType(GaussianMixtureNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianMixtureNode(world, time, id, graph, initialResolution);
            }
        });
        //LiveRegressionNode
        declareNodeType(LiveLinearRegressionNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LiveLinearRegressionNode(world, time, id, graph, initialResolution);
            }
        });
        //InterquartileRangeOutlierDetectorNode
        declareNodeType(InterquartileRangeOutlierDetectorNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new InterquartileRangeOutlierDetectorNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(GaussianClassifierNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianClassifierNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(GaussianNaiveBayesianNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianNaiveBayesianNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LogisticRegressionClassifierNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LogisticRegressionClassifierNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LinearRegressionNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LinearRegressionNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LinearRegressionBatchGDNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LinearRegressionBatchGDNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(LinearRegressionSGDNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new LinearRegressionSGDNode(world, time, id, graph, initialResolution);
            }
        });
        //BatchDecisionTreeNode
        declareNodeType(BatchDecisionTreeNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new BatchDecisionTreeNode(world, time, id, graph, initialResolution);
            }
        });
    }
}
