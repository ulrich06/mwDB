package org.mwg.ml.algorithm.profiling;

import org.mwg.ml.ProgressReporter;
import org.mwg.ml.common.matrix.operation.MultivariateNormalDistribution;


public class ProbaDistribution {
    
    public MultivariateNormalDistribution[] distributions;
    public int total[];
    public int global;

    public ProbaDistribution(int total[], MultivariateNormalDistribution[] distributions, int global) {
        this.total = total;
        this.distributions = distributions;
        this.global = global;
    }

    public double calculate(double[] features) {
        double result = 0;
        if (total != null) {
            for (int j = 0; j < distributions.length; j++) {
                if (KMeans.distance(features, distributions[j].getAvg(), distributions[j].getCovDiag()) < 5) {
                    result += distributions[j].density(features, false) * total[j] / global;
                }
            }
        } else {
            for (int j = 0; j < distributions.length; j++) {
                if (KMeans.distance(features, distributions[j].getAvg(), distributions[j].getCovDiag()) < 5) {
                    result += distributions[j].density(features, false) / global;
                }
            }
        }
        if (result > 1) {
            result = 1;
        }
        return result;
    }


    public double[] calculateArray(double[][] features, ProgressReporter reporter) {
        if (reporter != null) {
            reporter.updateInformation("Number of distributions: " + distributions.length + " , values: " + global);
        }

        double result[] = new double[features.length];
        double calibration = 0;
        for (int i = 0; i < features.length; i++) {
            result[i] = calculate(features[i]);
            calibration += result[i];
            if (reporter != null) {
                double progress = i * (1.0 / (features.length));
                progress = progress * 50 + 50;
                reporter.updateProgress((int) progress);
                if (reporter.isCancelled()) {
                    return null;
                }
            }
        }

        // double deviation=0;
        // double equidist=1.0/features.length;

        if (calibration != 0) {
            for (int i = 0; i < features.length; i++) {
                result[i] = result[i] / calibration;
                // deviation+=Math.abs(result[i]-equidist);
            }
            //deviation=deviation/features.length;
            //double percent =deviation*100.0/equidist;
            //System.out.println("Number of distributions: "+distributions.length+" Deviation avg: "+deviation+" over "+ equidist+" percent: "+percent+" %");
        }
        return result;
    }


    public double addUpProbabilities(double[][] features) {
        double res = 0;
        for (int i = 0; i < features.length; i++) {
            res += calculate(features[i]);
        }
        return res;
    }


    public double[] compareProbaDistribution(ProbaDistribution other, double[][] features) {
        double[] error = new double[2];
        double[] res1 = this.calculateArray(features, null);
        double[] res2 = other.calculateArray(features, null);

        double temp = 0;

        for (int i = 0; i < res1.length; i++) {
            temp = Math.abs(res1[i] - res2[i]);
            error[0] += temp;
            if (temp > error[1]) {
                error[1] = temp;
            }
        }
        error[0] = error[0] / res1.length;
        return error;
    }
}
