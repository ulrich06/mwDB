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
            if(GaussianGmmNode.distance(features,distributions[j].getAvg(),distributions[j].getCovDiag())<5){
                result+=distributions[j].density(features,false)*total[j]/global;
            }
        }
        return result;
    }


    public double[] calculateArray(double[][] features, ProgressReporter reporter){
        reporter.updateGraphInfo("Number of distributions: "+distributions.length+" , values: "+global);
        double result[]=new double[features.length];
        double calibration=0;
        for(int i=0;i<features.length;i++){
            result[i]=calculate(features[i]);
            calibration+=result[i];
            if(reporter!=null){
                double progress = i * (1.0 / (features.length));
                progress = progress * 50 + 50;
                reporter.updateProgress((int)progress);
                if(reporter.isCancelled()){
                    return null;
                }
            }
        }
        if(calibration!=0) {
            for (int i = 0; i < features.length; i++) {
                result[i] = result[i] / calibration;
            }
            //System.out.println("Calibration: "+calibration);
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
