package org.mwg.mlx;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.MLPlugin;
import org.mwg.mlx.algorithm.classifier.BatchDecisionTreeNode;
import org.mwg.mlx.algorithm.classifier.GaussianClassifierNode;
import org.mwg.mlx.algorithm.classifier.GaussianNaiveBayesianNode;
import org.mwg.mlx.algorithm.classifier.LogisticRegressionClassifierNode;
import org.mwg.mlx.algorithm.regression.LinearRegressionBatchGDNode;
import org.mwg.mlx.algorithm.regression.LinearRegressionNode;
import org.mwg.mlx.algorithm.regression.LinearRegressionSGDNode;
import org.mwg.plugin.NodeFactory;

public class MLXPlugin extends MLPlugin {

    public MLXPlugin() {
        super();

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
        //BatchDecisionTreeNode
        declareNodeType(BatchDecisionTreeNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new BatchDecisionTreeNode(world, time, id, graph, initialResolution);
            }
        });
        declareNodeType(GaussianClassifierNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new GaussianClassifierNode(world, time, id, graph, initialResolution);
            }
        });
    }

}
