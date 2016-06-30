package org.mwg.ml.common.structure;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.ml.common.distance.EuclideanDistance;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Enforcer;
import org.mwg.plugin.NodeState;

/**
 * Created by assaad on 29/06/16.
 */
public class KDNode extends AbstractNode {

    public static final String NAME = "KDNode";

    private static final String INTERNAL_LEFT = "_left";            //to navigate left
    private static final String INTERNAL_RIGHT = "_right";           //to navigate right

    private static final String INTERNAL_KEY = "_key";                //Values of the node
    private static final String INTERNAL_VALUE = "_value";            //Values of the node
    public static final String NUM_NODES = "_num";            //Values of the node

    private static final String INTERNAL_DIM = "_dim";                //Values of the node

    public static final String DISTANCE_THRESHOLD = "dist";          //Distance threshold
    public static final double DISTANCE_THRESHOLD_DEF = 1e-10;

    public KDNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    private static final Enforcer enforcer = new Enforcer()
            .asPositiveDouble(DISTANCE_THRESHOLD);

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        enforcer.check(propertyName, propertyType, propertyValue);
        super.setProperty(propertyName, propertyType, propertyValue);
    }


    private final void internalInsert(final KDNode node, final KDNode root, final double[] key, final int lev, final int dim, final double err, final Node value, final Callback<Boolean> callback) {
        NodeState state = node.unphasedState();
        double[] tk = (double[]) state.getFromKey(INTERNAL_KEY);
        if (tk == null) {
            state.setFromKey(INTERNAL_KEY, Type.DOUBLE_ARRAY, key);
            state.setFromKey(INTERNAL_VALUE, Type.RELATION, new long[]{value.id()});
            if (node == root) {
                state.setFromKey(NUM_NODES, Type.INT, 1);
            }
            if (node != root) {
                node.free();
            }
            if (callback != null) {
                callback.on(true);
            }

        } else if (distance(key, tk) < err) {
            state.setFromKey(INTERNAL_VALUE, Type.RELATION, new long[]{value.id()});
            if (node != root) {
                node.free();
            }
            if (callback != null) {
                callback.on(true);
            }
        } else if (key[lev] > tk[lev]) {
            //check right
            long[] right = (long[]) state.getFromKey(INTERNAL_RIGHT);
            if (right == null) {
                KDNode rightNode = (KDNode) graph().newTypedNode(world(), time(), NAME);
                rightNode.set(INTERNAL_KEY, key);
                rightNode.add(INTERNAL_VALUE, value);
                state.setFromKey(INTERNAL_RIGHT, Type.RELATION, new long[]{rightNode.id()});
                root.set(NUM_NODES, (Integer) root.get(NUM_NODES) + 1);
                rightNode.free();
                if (node != root) {
                    node.free();
                }
                if (callback != null) {
                    callback.on(true);
                }
            } else {
                node.rel(INTERNAL_RIGHT, new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result) {
                        if (node != root) {
                            node.free();
                        }
                        internalInsert((KDNode) result[0], root, key, (lev + 1) % dim, dim, err, value, callback);
                    }
                });
            }

        } else {
            long[] left = (long[]) state.getFromKey(INTERNAL_LEFT);
            if (left == null) {
                KDNode leftNode = (KDNode) graph().newTypedNode(world(), time(), NAME);
                leftNode.set(INTERNAL_KEY, key);
                leftNode.add(INTERNAL_VALUE, value);
                state.setFromKey(INTERNAL_LEFT, Type.RELATION, new long[]{leftNode.id()});
                root.set(NUM_NODES, (Integer) root.get(NUM_NODES) + 1);
                leftNode.free();
                if (node != root) {
                    node.free();
                }
                if (callback != null) {
                    callback.on(true);
                }
            } else {
                node.rel(INTERNAL_LEFT, new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result) {
                        if (node != root) {
                            node.free();
                        }
                        internalInsert((KDNode) result[0], root, key, (lev + 1) % dim, dim, err, value, callback);
                    }
                });
            }
        }
    }


    public void insert(final double[] key, final Node value, final Callback<Boolean> callback) {
        NodeState state = unphasedState();
        final int dim = state.getFromKeyWithDefault(INTERNAL_DIM, key.length);
        final double err = state.getFromKeyWithDefault(DISTANCE_THRESHOLD, DISTANCE_THRESHOLD_DEF);

        if (key.length != dim) {
            throw new RuntimeException("Key size should always be the same");
        }

        internalInsert(this, this, key, 0, dim, err, value, callback);
    }


    protected double distance(double[] key1, double[] key2) {
        return new EuclideanDistance().measure(key1, key2);
    }


}
