package org.mwg.profiling;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.NodeState;

/**
 * Created by assaad on 27/04/16.
 */
public class GaussianSlotProfiling extends AbstractNode {


    //Properties
    public static final String SLOTSNUMBER="SLOTS_NUMBER";
    public static final int SLOTSNUMBER_DEF=1;

    //Properties
    public static final String FEATURESNUMBER="FEATURES_NUMBER";
    public static final int FEATURESNUMBER_DEF=1;

    //Internal Gaussian keys
    private static final String INTERNAL_TOTAL_KEY = "_total";
    private static final String INTERNAL_MIN_KEY = "_min";
    private static final String INTERNAL_MAX_KEY = "_max";
    private static final String INTERNAL_SUM_KEY = "_sum";
    private static final String INTERNAL_SUMSQUARE_KEY = "_sumSquare";


    @Override
    public byte type(String attributeName) {
        return super.type(attributeName);
    }

    @Override
    public Object get(String attributeName) {
        return super.get(attributeName);
    }


    public GaussianSlotProfiling(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }


    public void learn(int slot, double[] values){
        NodeState resolved = this._resolver.resolveState(this, true);
        int numOfSlot= resolved.getFromKeyWithDefault(SLOTSNUMBER,SLOTSNUMBER_DEF);

        double[] total;
        double[] min;
        double[] max;
        double[] sum;
        double[] sumSquare;
        int features=values.length;
        int index=slot*features;
        int indexSquare=slot*features*(features+1)/2;

        total=(double[])resolved.getFromKey(INTERNAL_TOTAL_KEY);
        if(total==null){
            resolved.setFromKey(FEATURESNUMBER,Type.INT,features);
            total=new double[numOfSlot];
            min=new double[numOfSlot*features];
            max=new double[numOfSlot*features];
            sum=new double[numOfSlot*features];
            sumSquare=new double[numOfSlot*features*(features+1)/2];
        }
        else {
            min=(double[])resolved.getFromKey(INTERNAL_MIN_KEY);
            max=(double[])resolved.getFromKey(INTERNAL_MAX_KEY);
            sum=(double[])resolved.getFromKey(INTERNAL_SUM_KEY);
            sumSquare=(double[])resolved.getFromKey(INTERNAL_SUMSQUARE_KEY);
        }

        //update the profile
        total[slot]+=1;

        if(total[slot]==1){
            int count=0;
            for(int i=0;i<features;i++){
                min[index+i]=values[i];
                max[index+i]=values[i];
                sum[index+i]=values[i];
                for (int j = i; j < features; j++) {
                    sumSquare[indexSquare+count] = values[i] * values[j];
                    count++;
                }
            }
        }
        else{
            int count=0;
            for(int i=0;i<features;i++){
                if(values[i]<min[index+i]){
                    min[index+i]=values[i];
                }
                if(values[i]>max[index+i]) {
                    max[index + i] = values[i];
                }
                sum[index+i]+=values[i];
                for (int j = i; j < features; j++) {
                    sumSquare[indexSquare+count] += values[i] * values[j];
                    count++;
                }
            }
        }

        //Save the state
        resolved.setFromKey(INTERNAL_TOTAL_KEY,Type.DOUBLE_ARRAY,total);
        resolved.setFromKey(INTERNAL_MIN_KEY,Type.DOUBLE_ARRAY,min);
        resolved.setFromKey(INTERNAL_MAX_KEY,Type.DOUBLE_ARRAY,max);
        resolved.setFromKey(INTERNAL_SUM_KEY,Type.DOUBLE_ARRAY,sum);
        resolved.setFromKey(INTERNAL_SUMSQUARE_KEY,Type.DOUBLE_ARRAY,sumSquare);
    }

    public double[] getAvg(){
        NodeState resolved = this._resolver.resolveState(this, true);
        int numOfSlot= resolved.getFromKeyWithDefault(SLOTSNUMBER,SLOTSNUMBER_DEF);
        int features= resolved.getFromKeyWithDefault(FEATURESNUMBER,FEATURESNUMBER_DEF);

        double[] total=(double[])resolved.getFromKey(INTERNAL_TOTAL_KEY);
        double[] sum=(double[])resolved.getFromKey(INTERNAL_SUM_KEY);

        double[] result = new double[numOfSlot*features];
        if(total!=null){
            int count=0;
            for(int i=0;i<numOfSlot;i++){
                if(total[i]!=0) {
                    for (int j = 0; j < features; j++) {
                        result[count] = sum[count] / total[i];
                        count++;
                    }
                }
                else{
                    count+=features;
                }
            }
        }
        return result;
    }
}
