package org.mwg.ml.algorithm.profiling;

import org.mwg.ml.common.NDimentionalArray;
import org.mwg.ml.common.matrix.operation.MultivariateNormalDistribution;


/**
 * Created by assaad on 11/05/16.
 */
public class ProbaDistribution {
    MultivariateNormalDistribution[] distributions;
    int total[];
    int global;
    public ProbaDistribution(int total[], MultivariateNormalDistribution[] distributions, int global){
        this.total=total;
        this.distributions=distributions;
        this.global=global;
    }

    public double calculate(double[] features){
        double result=0;
        for(int j=0;j<distributions.length;j++){
            result+=distributions[j].density(features,true)*total[j]/global;
        }
        return result;
    }

    public NDimentionalArray ParallelCalculate(double[][] space, ProgressReporter progressReporter){
        NDimentionalArray result = new NDimentionalArray();

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
