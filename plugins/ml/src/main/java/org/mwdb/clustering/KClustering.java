package org.mwdb.clustering;

/**
 * Created by assaad on 01/04/16.
 */
public interface KClustering {

    int[][] getClusterIds(double[][] data, int numOfCluster, int numOfIterations);

}
