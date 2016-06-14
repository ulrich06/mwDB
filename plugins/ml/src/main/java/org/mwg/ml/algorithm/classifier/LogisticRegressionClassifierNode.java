package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.ml.ClassificationNode;
import org.mwg.ml.common.AbstractClassifierSlidingWindowManagingNode;
import org.mwg.plugin.NodeFactory;

/**
 * Created by andrey.boytsov on 17/05/16.
 */
public class LogisticRegressionClassifierNode extends AbstractClassifierSlidingWindowManagingNode implements ClassificationNode{

    /**
     * Regression coefficients
     */
    public static final String COEFFICIENTS_KEY = "regressionCoefficients";
    /**
     * Regression coefficients - default
     */
    public static final double[] COEFFICIENTS_DEF = new double[0];
    /**
     * Regression intercept
     */
    public static final String INTERCEPT_KEY = "regressionIntercept";
    /**
     * Regression intercept - default
     */
    public static final double INTERCEPT_DEF = 0.0;

    /**
     * L2 regularization coefficient
     */
    public static final String L2_COEF_KEY = "L2Coefficient";
    /**
     * L2 regularization coefficient - default
     */
    public static final double L2_COEF_DEF = 0.0;

    public static final String NAME = "LogisticRegressionBatch";

    public LogisticRegressionClassifierNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            LogisticRegressionClassifierNode newNode = new LogisticRegressionClassifierNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }

    public double[] getCoefficients(int classNum){
        return unphasedState().getFromKeyWithDefault(COEFFICIENTS_KEY+classNum, COEFFICIENTS_DEF);
    }

    public double getIntercept(int classNum){
        return unphasedState().getFromKeyWithDefault(INTERCEPT_KEY+classNum, INTERCEPT_DEF);
    }

    protected void setIntercept(double intercept, int classNum){
        unphasedState().setFromKey(INTERCEPT_KEY+classNum, Type.DOUBLE, intercept);
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if (L2_COEF_KEY.equals(propertyName)) {
            illegalArgumentIfFalse( (propertyValue instanceof Double)||(propertyValue instanceof Integer),
                    "L2 regularization coefficient should be of type double or integer");
            if (propertyValue instanceof Double){
                illegalArgumentIfFalse((double)propertyValue >= 0, "L2 regularization coefficient should be non-negative");
                setL2Regularization((double)propertyValue);
            }else{
                illegalArgumentIfFalse((int)propertyValue >= 0, "L2 regularization coefficient should be non-negative");
                setL2Regularization((double)((int)propertyValue));
            }
        }else if (propertyName.startsWith(COEFFICIENTS_KEY) || propertyName.startsWith(INTERCEPT_KEY)) {
            //Nothing. Those cannot be set.
        }else if (GD_DIFFERENCE_THRESH_KEY.equals(propertyName)){
            setIterationDifferenceThreshold((double)propertyValue);
        }else if (GD_ITERATION_THRESH_KEY.equals(propertyName)){
            setIterationCountThreshold((int)propertyValue);
        }else if (INTERNAL_VALUE_LEARNING_RATE_KEY.equals(propertyName)){
            setLearningRate((double)propertyValue);
        }else{
            super.setProperty(propertyName, propertyType, propertyValue);
        }
    }

    @Override
    public Object get(String propertyName){
        if(propertyName.startsWith(COEFFICIENTS_KEY)) {
            return unphasedState().getFromKeyWithDefault(propertyName, COEFFICIENTS_DEF);
        }else if (propertyName.startsWith(INTERCEPT_KEY)){
            return unphasedState().getFromKeyWithDefault(propertyName, INTERCEPT_DEF);
        }else if(L2_COEF_KEY.equals(propertyName)){
            return getL2Regularization();
        }else if (GD_DIFFERENCE_THRESH_KEY.equals(propertyName)){
            return getIterationDifferenceThreshold();
        }else if (GD_ITERATION_THRESH_KEY.equals(propertyName)){
            return getIterationCountThreshold();
        }else if (INTERNAL_VALUE_LEARNING_RATE_KEY.equals(propertyName)){
            return getLearningRate();
        }
        return super.get(propertyName);
    }

    protected void setCoefficients(double[] coefficients, int classNum) {
        LogisticRegressionClassifierNode.requireNotNull(coefficients,"Regression coefficients must be not null");
        unphasedState().setFromKey(COEFFICIENTS_KEY+classNum, Type.DOUBLE_ARRAY, coefficients);
    }

    public double getL2Regularization(){
        return unphasedState().getFromKeyWithDefault(L2_COEF_KEY, L2_COEF_DEF);
    }

    public void setL2Regularization(double l2) {
        illegalArgumentIfFalse(l2>=0,"L2 coefficients must be non-negative");
        unphasedState().setFromKey(L2_COEF_KEY, Type.DOUBLE, l2);
    }


    public static final String GD_DIFFERENCE_THRESH_KEY = "gdDifferenceThreshold";
    public static final String GD_ITERATION_THRESH_KEY = "gdIterationThreshold";

    public static final int DEFAULT_GD_ITERATIONS_COUNT = 10000;

    public static final double DEFAULT_LEARNING_RATE = 0.0001;

    /**
     * Attribute key - Learning rate
     */
    protected static final String INTERNAL_VALUE_LEARNING_RATE_KEY = "_LearningRate";

    private static double sigmoid(double x){
        return 1.0/(1.0+Math.exp(-x));
    }

    private static double dot(double vector1[], double vector2[]){
        assert vector1 != null : "vector1 must be not null";
        assert vector2 != null : "vector2 must be not null";
        assert vector1.length == vector2.length : "vectors must be of equal length";
        double result = 0;
        for (int i=0;i<vector1.length;i++){
            result += vector1[i]*vector2[i];
        }
        return result;
    }

    @Override
    protected int predictValue(double[] value) {
        int classes[] = getKnownClasses();
        double maxLikelihood = -1.0; //Guaranteed to change. No real likelihood is less than 0.
        int maxClass = 0;
        for (int cl : classes){
            double likelihoodPerClass = getLikelihoodForClass(value, cl);
            if (likelihoodPerClass > maxLikelihood){
                maxLikelihood = likelihoodPerClass;
                maxClass = cl;
            }
        }
        return maxClass;
    }

    @Override
    protected double getLikelihoodForClass(double[] value, int classNum) {
        return sigmoid(dot(value, getCoefficients(classNum))+getIntercept(classNum));
    }

    @Override
    protected void updateModelParameters(double[] value, int classNumber) {
        if (getInputDimensions()==INPUT_DIM_UNKNOWN){
            setInputDimensions(value.length);
        }
        initializeClassIfNecessary(classNumber);
        final int dims = getInputDimensions();

        final double valueBuffer[] = getValueBuffer();
        final int resultBuffer[] = getRealBufferClasses();

        final double gdDifferenceThresh = getIterationDifferenceThreshold();
        final int gdIterThresh = getIterationCountThreshold();

        final double alpha = getLearningRate();
        final double lambda = getL2Regularization();

        final int bufferLength = getCurrentBufferLength();

        int classes[] = getKnownClasses();
        for (final int cl : classes){
            double coefs[] = getCoefficients(cl);
            double intercept = getIntercept(cl);
            if (coefs.length==0){ //TODO should not happen? If necessary, remove unnecessary check & replace with assertion
                coefs = new double[dims];
                setCoefficients(coefs, cl);
            }

            int iterCount = 0;
            boolean exitCase = false;
            while (!exitCase){
                iterCount++;

                int startIndex = 0;
                int index = 0;
                double oldCoefs[] = new double[coefs.length];
                System.arraycopy(coefs, 0, oldCoefs, 0, coefs.length);
                final double oldIntercept = intercept;
                while (startIndex + dims <= valueBuffer.length){
                    double curValue[] = new double[dims];
                    System.arraycopy(valueBuffer, startIndex, curValue, 0, dims);

                    double h = sigmoid(dot(value, oldCoefs)+intercept);
                    int y = (resultBuffer[index]==cl)?1:0;

                    for (int j=0;j<dims;j++){
                        coefs[j] += alpha * ((y - h)*curValue[j])/bufferLength;
                    }
                    intercept += alpha * (y - h) / bufferLength;

                    startIndex += dims;
                    index++;
                }
                for (int j=0;j<dims;j++){
                    coefs[j] -= alpha * lambda * oldCoefs[j];
                }


                double maxDiff = 0.0;
                for (int j=0;j<dims;j++){
                    maxDiff = Math.max(Math.abs(coefs[j]-oldCoefs[j]), maxDiff);
                }
                maxDiff = Math.max(Math.abs(intercept-oldIntercept), maxDiff);

                setCoefficients(coefs, cl);
                setIntercept(intercept, cl);
                if (gdDifferenceThresh>0){
                    exitCase = exitCase || maxDiff<gdDifferenceThresh;
                }

                if (gdIterThresh>0){
                    exitCase = exitCase || (iterCount>=gdIterThresh);
                }

                if ((!(gdDifferenceThresh>0))&&(!(gdIterThresh>0))) {
                    //Protection against infinite loops. If neither error threshold, nor iteration thresholds are used,
                    //run loops once, do not go infinite.
                    exitCase = true;
                }
            }

        }

    }

    @Override
    protected void removeAllClassesHook() {
        int classes[] = getKnownClasses();
        for (int curClass : classes) {
            unphasedState().setFromKey(INTERCEPT_KEY + curClass, Type.DOUBLE, INTERCEPT_DEF);
            unphasedState().setFromKey(COEFFICIENTS_KEY + curClass, Type.DOUBLE_ARRAY, COEFFICIENTS_DEF);
        }
    }

    protected void initializeClassIfNecessary(int classNum) {
        Object oldCoefsObj = unphasedState().getFromKey(COEFFICIENTS_KEY + classNum);
        if (oldCoefsObj != null) {
            //Is there, but could be deleted
            double oldCoefs[] = (double[]) oldCoefsObj;
            if (oldCoefs.length > 0) { //Is the class deleted?
                //Already initialized
                return;
            }
        }
        int dims = getInputDimensions();

        addToKnownClassesList(classNum);
        setCoefficients(new double[dims], classNum);
        setIntercept(INTERCEPT_DEF, classNum);
    }

    public double getIterationDifferenceThreshold() {
        return unphasedState().getFromKeyWithDefault(GD_DIFFERENCE_THRESH_KEY, Double.NaN);
    }

    public void setIterationDifferenceThreshold(double errorThreshold) {
        unphasedState().setFromKey(GD_DIFFERENCE_THRESH_KEY, Type.DOUBLE, errorThreshold);
    }

    public void removeIterationDifferenceThreshold() {
        unphasedState().setFromKey(GD_DIFFERENCE_THRESH_KEY, Type.DOUBLE, Double.NaN);
    }

    public int getIterationCountThreshold() {
        return unphasedState().getFromKeyWithDefault(GD_ITERATION_THRESH_KEY, DEFAULT_GD_ITERATIONS_COUNT);
    }

    public void setIterationCountThreshold(int iterationCountThreshold) {
        //Any value is acceptable.
        unphasedState().setFromKey(GD_ITERATION_THRESH_KEY, Type.INT, iterationCountThreshold);
    }

    public void removeIterationCountThreshold() {
        unphasedState().setFromKey(GD_ITERATION_THRESH_KEY, Type.INT, -1);
    }

    public double getLearningRate(){
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_LEARNING_RATE_KEY, DEFAULT_LEARNING_RATE);
    }

    public void setLearningRate(double newLearningRate){
        illegalArgumentIfFalse(newLearningRate > 0, "Learning rate should be positive");
        unphasedState().setFromKey(INTERNAL_VALUE_LEARNING_RATE_KEY, Type.DOUBLE, newLearningRate);
    }

}
