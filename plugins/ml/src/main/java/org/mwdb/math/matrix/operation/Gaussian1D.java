package org.mwdb.math.matrix.operation;

/**
 * Created by assaad on 22/04/16.
 */
public class Gaussian1D {

    public static double getCovariance(double sum, double sumSq, int total){
        return (sumSq-(sum*sum)/total)/(total-1);
    }
    public static double getDensity(double sum, double sumSq, int total, double feature){

        if(total<2){
            return 0;
        }
        double avg=sum/total;
        double cov=getCovariance(sum,sumSq,total);

        double proba=1/Math.sqrt(2*Math.PI*cov)*Math.exp(-(feature-avg)*(feature-avg)/(2*cov));
        return proba;

    }
    public static double[] getDensity(double sum, double sumSq, int total, double[] feature){
        if(total<2){
            return null;
        }
        double avg=sum/total;
        double cov=getCovariance(sum,sumSq,total);
        double exp=1/Math.sqrt(2*Math.PI*cov);

        double[] proba= new double[feature.length];
        for(int i=0;i<feature.length;i++){
           proba[i]=exp*Math.exp(-(feature[i]-avg)*(feature[i]-avg)/(2*cov));
        }
        return proba;
    }
}
