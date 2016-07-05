package org.mwg.ml.algorithm.profiling;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.ml.ProfilingNode;
import org.mwg.ml.common.distance.Distance;
import org.mwg.ml.common.structure.KDNode;
import org.mwg.plugin.NodeState;

/**
 * Created by assaad on 04/07/16.
 */
public class KdGaussianNode extends GaussianNode implements ProfilingNode {

    public static String NAME = "KdGaussianNode";

    public static final String THRESHOLD = "_threshold";  //Factor of distance before check inside fail
    public static final double THRESHOLD_DEF = 1.01;


    private static String INTERNAL_KDROOT = "kdroot";


    public KdGaussianNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void learn(Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] values) {
                double[] features = new double[values.length - 1];
                System.arraycopy(values, 0, features, 0, values.length - 1);
                internalLearn(values, features, callback);
            }
        });
    }


    public void internalLearn(final double[] values, final double[] features, final Callback<Boolean> callback) {
        final NodeState resolved = this._resolver.resolveState(this, true);
        super.learnVector(values, new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final double threshold = resolved.getFromKeyWithDefault(THRESHOLD, THRESHOLD_DEF);
                final double[] precisions = (double[]) resolved.getFromKey(PRECISION);

                if (resolved.getFromKey(INTERNAL_KDROOT) == null) {
                    KDNode root = (KDNode) graph().newTypedNode(world(), time(), KDNode.NAME);
                    root.setProperty(KDNode.DISTANCE_TYPE, Type.INT, Distance.GAUSSIAN);
                    root.setProperty(KDNode.DISTANCE_THRESHOLD, Type.DOUBLE, threshold);
                    root.setProperty(KDNode.DISTANCE_PRECISION, Type.DOUBLE_ARRAY, precisions);
                    GaussianNode profile = (GaussianNode) graph().newTypedNode(world(), time(), GaussianNode.NAME);

                    profile.learnVector(values, new Callback<Boolean>() {
                        @Override
                        public void on(Boolean result) {
                            root.insert(features, profile, new Callback<Boolean>() {
                                @Override
                                public void on(Boolean result) {
                                    root.free();
                                    profile.free();
                                    callback.on(true);
                                }
                            });
                        }
                    });

                } else {
                    rel(INTERNAL_KDROOT, new Callback<Node[]>() {
                        @Override
                        public void on(Node[] result) {
                            KDNode root = (KDNode) result[0];
                            root.nearestWithinDistance(features, new Callback<Node>() {
                                @Override
                                public void on(Node result) {
                                    if(result!=null){
                                        GaussianNode profile = (GaussianNode) result;
                                        profile.learnVector(values, new Callback<Boolean>() {
                                            @Override
                                            public void on(Boolean result) {
                                                root.free();
                                                profile.free();
                                            }
                                        });
                                    }
                                    else{
                                        GaussianNode profile = (GaussianNode) graph().newTypedNode(world(), time(), GaussianNode.NAME);
                                        profile.learnVector(values, new Callback<Boolean>() {
                                            @Override
                                            public void on(Boolean result) {
                                                root.insert(features, profile, new Callback<Boolean>() {
                                                    @Override
                                                    public void on(Boolean result) {
                                                        root.free();
                                                        profile.free();
                                                    }
                                                });

                                            }
                                        });
                                    }
                                    callback.on(true);
                                }
                            });
                        }
                    });
                }

            }
        });
    }


    @Override
    public void predict(Callback<double[]> callback) {

    }
}
