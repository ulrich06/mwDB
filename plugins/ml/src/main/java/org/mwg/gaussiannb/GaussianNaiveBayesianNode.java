package org.mwg.gaussiannb;

import org.mwg.*;
import org.mwg.util.matrix.operation.Gaussian1D;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.NodeState;

import java.util.*;

/**
 * Created by Andrey Boytsov on 4/14/2016.
 */
public class GaussianNaiveBayesianNode extends AbstractNode implements KGaussianNaiveBayesianNode {

    //TODO Any synchronization?

    //TODO Try out changing parameters on the fly

    //NOT final
    private NodeState currentState = null;

    /**
     * Internal keys - those attributes are only for internal use within the node.
     * They are not supposed to be accessed from outside (although it is not banned).
     */

    /**
     * Prefix for sum attribute. For each class its class label will be appended to
     * this key prefix.
     */
    private static final String INTERNAL_SUM_KEY_PREFIX = "_sum_";

    /**
     * Prefix for sum of squares attribute. For each class its class label will be appended to
     * this key prefix.
     */
    private static final String INTERNAL_SUMSQUARE_KEY_PREFIX = "_sumSquare_";

    /**
     * Prefix for number of measurements attribute. For each class its class label will be appended to
     * this key prefix.
     */
    private static final String INTERNAL_TOTAL_KEY_PREFIX = "_total_";

    /**
     * Attribute key - whether the node is in bootstrap (re-learning) mode
     */
    private static final String INTERNAL_BOOTSTRAP_MODE_KEY = "_bootstrapMode";
    /**
     * Attribute key - sliding window of values
     */
    private static final String INTERNAL_VALUE_BUFFER_KEY = "_valueBuffer";

    //TODO Use it in round 2 of implementation
    /**
     * Attribute key - prediction results for values in the buffer. Used only outside of bootstrap.
     */
    private static final String INTERNAL_PREDICTION_RESULT_BUFFER_KEY = "_predictionResultBuffer";
    /**
     * Attribute key - List of known classes
     */
    private static final String INTERNAL_KNOWN_CLASSES_LIST = "_knownClassesList";

    //TODO How to keep that one? It is an object. Try some serialization?
    /**
     * Prefix for distribution objects. For each class its class label will be appended to
     * this key prefix.
     */
    //private static final String INTERNAL_DISTRIBUTION_KEY_PREFIX = "_distributions_";

    //TODO Not allow setting?

    //TODO Remove these comments when completely done
    //private final Map<Integer, MultivariateNormalDistribution> distributions = new HashMap<>();


    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    private int getClassIndex(){
        Object objClassIndex = currentState.get(_resolver.stringToLongKey(CLASS_INDEX_KEY));
        Objects.requireNonNull(objClassIndex, "Class index must be not null");
        return ((Integer)objClassIndex).intValue();
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    private int getBufferSize(){
        Object objClassIndex = currentState.get(_resolver.stringToLongKey(BUFFER_SIZE_KEY));
        Objects.requireNonNull(objClassIndex, "Buffer size must be not null");
        return ((Integer)objClassIndex).intValue();
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    private int getInputDimensions(){
        Object objClassIndex = currentState.get(_resolver.stringToLongKey(INPUT_DIM_KEY));
        Objects.requireNonNull(objClassIndex, "Input dimensions must be not null");
        return ((Integer)objClassIndex).intValue();
    }

    /**
     * Asserts that condition is true. If not - throws {@code IllegalArgumentException} with a specified error message
     *
     * @throws IllegalArgumentException if condition is false
     * @param condition Condition to test
     * @param errorMessage Error message thrown with {@code IllegalArgumentException} (if thrown)
     */
    private void illegalArgumentIfFalse(boolean condition, String errorMessage){
        assert errorMessage != null;
        if (!condition){
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    public GaussianNaiveBayesianNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution){
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void index(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {
        // Nothing for now
    }

    @Override
    public void unindex(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {
        // Nothing for now
    }

    @Override
    public void set(String attributeName, Object attributeValue) {
        //TODO Changed class index? Need to recalculate everything
        //TODO Changed buffer size? Might also need recalculation
        //TODO Class index should be positive
        //TODO Input dimensions should be positive

        if (attributeName.equals(VALUE_KEY)) {
            addValue((double[]) attributeValue);
        } else {
            super.set(attributeName, attributeValue);
        }
    }

    private void addToKnownClassesList(int classLabel){
        int[] knownClasses = getKnownClasses();
        int[] newKnownClasses = new int[knownClasses.length+1];
        for (int i=0;i<knownClasses.length;i++){
            if (classLabel==knownClasses[i]){
                return ; //Already known. No need to add
            }
            newKnownClasses[i] = knownClasses[i];
        }
        newKnownClasses[knownClasses.length] = classLabel;
        currentState.set(_resolver.stringToLongKey(INTERNAL_KNOWN_CLASSES_LIST), Type.INT_ARRAY, newKnownClasses);
    }

    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    public void addValue(double value[]){
        illegalArgumentIfFalse(value != null, "Value must be not null");
        illegalArgumentIfFalse(value.length == getInputDimensions(), "Class index is not included in the value");

        if (isInBootstrapMode()){
            addValueBootstrap(value);
        }else{
            addValueNoBootstrap(value);
        }
    }

    @Override
    public void initialize(int inputDimension, int classIndex, int bufferSize, double highErrorThreshold, double lowErrorThreshold){
        illegalArgumentIfFalse(currentState == null, "Already initialized before");
        illegalArgumentIfFalse(inputDimension > 0, "Input should have at least dimension");
        illegalArgumentIfFalse(classIndex < inputDimension, "Class index should be within dimensions");
        illegalArgumentIfFalse((highErrorThreshold>=0)&&(highErrorThreshold<=1), "Higher error threshold should be within [0;1]");
        illegalArgumentIfFalse((lowErrorThreshold>=0)&&(lowErrorThreshold<=1), "Lower error threshold should be within [0;1]");
        illegalArgumentIfFalse(highErrorThreshold>=lowErrorThreshold, "High error threshold should be above or equal to lower");
        illegalArgumentIfFalse(bufferSize > 0, "Buffer size should be positive");

        currentState = graph().resolver().resolveState(this, true);

        //Set the attributes
        currentState.set(_resolver.stringToLongKey(KGaussianNaiveBayesianNode.CLASS_INDEX_KEY), Type.INT, classIndex);
        currentState.set(_resolver.stringToLongKey(KGaussianNaiveBayesianNode.INPUT_DIM_KEY), Type.INT, inputDimension);
        currentState.set(_resolver.stringToLongKey(KGaussianNaiveBayesianNode.BUFFER_SIZE_KEY), Type.INT, bufferSize);
        currentState.set(_resolver.stringToLongKey(KGaussianNaiveBayesianNode.LOW_ERROR_THRESH_KEY), Type.DOUBLE, lowErrorThreshold);
        currentState.set(_resolver.stringToLongKey(KGaussianNaiveBayesianNode.HIGH_ERROR_THRESH_KEY), Type.DOUBLE, highErrorThreshold);
    }

    private final void setTotal(int classNum, int val){
        assert val >= 0;
        currentState.set(_resolver.stringToLongKey(INTERNAL_TOTAL_KEY_PREFIX+classNum), Type.INT, val);
    }

    private final void setSums(int classNum, double[] vals){
        assert vals != null;
        currentState.set(_resolver.stringToLongKey(INTERNAL_SUM_KEY_PREFIX+classNum), Type.DOUBLE_ARRAY, vals);
    }

    private final void setValueBuffer(double[] valueBuffer){
        assert valueBuffer != null;
        currentState.set(_resolver.stringToLongKey(INTERNAL_VALUE_BUFFER_KEY), Type.DOUBLE_ARRAY, valueBuffer);
    }

    private final void setSumsSquared(int classNum, double[] vals){
        assert vals != null;
        currentState.set(_resolver.stringToLongKey(INTERNAL_SUMSQUARE_KEY_PREFIX+classNum), Type.DOUBLE_ARRAY, vals);
    }

    private final int getClassTotal(int classNum){
        Object objClassTotal = currentState.get(_resolver.stringToLongKey(INTERNAL_TOTAL_KEY_PREFIX+classNum));
        Objects.requireNonNull(objClassTotal, "Class total must be not null (class "+classNum+")");
        return ((Integer)objClassTotal).intValue();
    }

    private double[] getSums(int classNum){
        Object objSum = currentState.get(_resolver.stringToLongKey(INTERNAL_SUM_KEY_PREFIX+classNum));
        Objects.requireNonNull(objSum, "Sums must be not null (class "+classNum+")");
        return (double [])objSum;
    }

    private double getHigherErrorThreshold(){
        Object objHET = currentState.get(_resolver.stringToLongKey(HIGH_ERROR_THRESH_KEY));
        Objects.requireNonNull(objHET, "Higher error threshold must be not null");
        return (double)objHET;
    }

    private double getLowerErrorThreshold(){
        Object objLET = currentState.get(_resolver.stringToLongKey(LOW_ERROR_THRESH_KEY));
        Objects.requireNonNull(objLET, "Lower error threshold must be not null");
        return (double)objLET;
    }

    private double[] getValueBuffer(){
        Object objValueBuffer = currentState.get(_resolver.stringToLongKey(INTERNAL_VALUE_BUFFER_KEY));
        if (objValueBuffer==null){
            double emptyValueBuffer[] = new double[0];
            currentState.set(_resolver.stringToLongKey(INTERNAL_VALUE_BUFFER_KEY), Type.DOUBLE_ARRAY, emptyValueBuffer); //Value buffer, starts empty
            return emptyValueBuffer;
        }
        return (double [])objValueBuffer;
    }

    private double[] getSumSquares(int classNum){
        Object objSumSq = currentState.get(_resolver.stringToLongKey(INTERNAL_SUMSQUARE_KEY_PREFIX+classNum));
        Objects.requireNonNull(objSumSq, "Sums of squares must be not null (class "+classNum+")");
        return (double [])objSumSq;
    }

    /**
     * Initializes map values for class num: sum, sum of squares and total.
     * @param classNum Number of class
     */
    private void initializeClassIfNecessary(int classNum){
        Object oldSumsObj = currentState.get(_resolver.stringToLongKey(INTERNAL_SUM_KEY_PREFIX+classNum));
        if (oldSumsObj!=null){
            //Is there, but could be deleted
            double oldSums[] = (double[])oldSumsObj;
            if (oldSums.length>0){ //Is the class deleted?
                //Already initialized
                return;
            }
        }

        addToKnownClassesList(classNum);
        setTotal(classNum, 0);
        final int dimensions = getInputDimensions();
        setSums(classNum, new double[dimensions]);
        setSumsSquared(classNum, new double[dimensions]);

        //Model can stay uninitialized until total is at least <TODO 2? 1?>
    }

    /**
     * Adds value's contribution to total, sum and sum of squares of new model.
     * Does NOT build model yet.
     *
     * @param value New value
     */
    private void updateModelParameters(double value[]){
        final int classIndex = getClassIndex();
        final int classNum = (int)value[classIndex];
        //Rebuild Gaussian for mentioned class
        //Update sum, sum of squares and total
        initializeClassIfNecessary(classNum);
        setTotal(classNum, getClassTotal(classNum)+1);

        double currentSum[] = getSums(classNum);
        double currentSumSquares[] = getSumSquares(classNum);
        for (int i=0;i<value.length;i++) {
            currentSum[i] += value[i];
            currentSumSquares[i] += value[i]*value[i];
        }
        //Value at class index - force 0 (or it will be the ultimate predictor)
        currentSum[classIndex] = 0;
        currentSumSquares[classIndex] = 0;
        setSums(classNum, currentSum);
        setSumsSquared(classNum, currentSumSquares);
        //Need to re-put
    }

    private void addValueToBuffer(double[] value){
        double valueBuffer[] = getValueBuffer();
        double newBuffer[] = new double[valueBuffer.length+value.length];
        for (int i = 0; i< valueBuffer.length; i++){
            newBuffer[i] = valueBuffer[i];
        }
        for (int i=valueBuffer.length;i<newBuffer.length;i++){
            newBuffer[i] = value[i-valueBuffer.length];
        }
        setValueBuffer(newBuffer);
    }

    private void removeFirstValueFromBuffer(){
        final int dims = getInputDimensions();
        double valueBuffer[] = getValueBuffer();
        if (valueBuffer.length==0){
            return ;
        }
        double newBuffer[] = new double[valueBuffer.length-dims];
        for (int i = 0; i< newBuffer.length; i++){
            newBuffer[i] = valueBuffer[i+dims];
        }
        setValueBuffer(newBuffer);
    }

    /**
     * Adds new value to the buffer. Gaussian model is regenerated.
     *
     * @param value New value to add; {@code null} disallowed
     */
    private void addValueBootstrap(double value[]){
        addValueToBuffer(value); //In bootstrap - no need to account for length

        if (getNumValuesInBuffer() >= getBufferSize()){
            //Predict for each value in the buffer. Calculate percentage of errors.
            double errorInBuffer = getBufferErrorFraction();
            if (errorInBuffer <= getLowerErrorThreshold()){
                setBootstrapMode(false); //If number of errors is below lower threshold, get out of bootstrap
            }
        }

        updateModelParameters(value);
    }

    private int getNumValuesInBuffer(){
        final int valLength = getValueBuffer().length;
        return valLength / getInputDimensions();
    }

    //TODO Later use cached distributions
    private double getLikelihoodForClass(double value[], int classNum){
        //It is assumed that real class is removed and replaces with 0
        initializeClassIfNecessary(classNum); //TODO should not be necessary. Double-check.
        int total = getClassTotal(classNum);
        if (total < 2){
            //Not enough data to build model for that class.
            return 0;
        }

        //For each dimension
        //Step 1. Get sum
        //Step 2. Get sum of squares.
        //Step 3. Multiply for each dimension
        double likelihood = 1;
        double sums[] = getSums(classNum);
        double sumSquares[] = getSumSquares(classNum);
        final int classIndex = getClassIndex();
        for (int i=0;i<getInputDimensions();i++){
            if (i!=classIndex) {
                likelihood *= Gaussian1D.getDensity(sums[i], sumSquares[i], total, value[i]);
            }
        }
        //TODO Use log likelihood? Can be better for underflows.
        return likelihood;
    }

    private int predictValue(double value[]){
        int kk[] = getKnownClasses();
        if (kk.length==1){
            return kk[0];
        }
        double valueWithClassRemoved[] = Arrays.copyOf(value, value.length);
        valueWithClassRemoved[getClassIndex()] = 0; //Do NOT use real class for prediction
        int classes[] = getKnownClasses();
        double curMaxLikelihood = Double.NEGATIVE_INFINITY; //Even likelihood 0 should surpass it
        int curMaxLikelihoodClass = -1;
        for (int curClass : classes){
            double curLikelihood = getLikelihoodForClass(valueWithClassRemoved, curClass);
            if (curLikelihood > curMaxLikelihood){
                curMaxLikelihood = curLikelihood;
                curMaxLikelihoodClass = curClass;
            }
        }
        return curMaxLikelihoodClass;
    }

    private int[] getKnownClasses() {
        Object objKnownClasses = currentState.get(_resolver.stringToLongKey(INTERNAL_KNOWN_CLASSES_LIST));
        if (objKnownClasses!=null) {
            return (int[]) objKnownClasses;
        }
        int emptyClassList[] = new int[0];
        currentState.set(_resolver.stringToLongKey(INTERNAL_KNOWN_CLASSES_LIST), Type.INT_ARRAY, emptyClassList);
        return emptyClassList;
    }


    @Override
    public int[] getPredictedBufferClasses(){
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues =  valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0){
            return new int[0];
        }

        int result[] = new int[numValues];

        final int clIndex = getClassIndex();
        int i = 0;
        while (startIndex+dims < valueBuffer.length){
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex+dims);
            result[i] = predictValue(curValue);
            //Continue the loop
            startIndex += dims;
            i++;
        }
        return result;
    }

    @Override
    public int[] getRealBufferClasses(){
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues =  valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0){
            return new int[0];
        }

        int result[] = new int[numValues];

        final int clIndex = getClassIndex();
        int i = 0;
        while (startIndex+dims < valueBuffer.length){
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex+dims);
            result[i] = (int)curValue[clIndex];

            //Continue the loop
            startIndex += dims;
            i++;
        }
        return result;
    }

    @Override
    public int getBufferErrorCount(){
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues =  valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0){
            return 0;
        }

        final int clIndex = getClassIndex();
        int errorCount = 0;
        while (startIndex+dims < valueBuffer.length){
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex+dims);
            int realClass = (int)curValue[clIndex];
            int predictedClass = predictValue(curValue);
            errorCount += (realClass!=predictedClass)?1:0;

            //Continue the loop
            startIndex += dims;
        }
        return errorCount;
    }

    @Override
    public int getCurrentBufferLength(){
        double valueBuffer[] = getValueBuffer();
        final int dims = getInputDimensions();
        return valueBuffer.length / dims;
    }

    /**
     * @return Prediction accuracy for data in the buffer. {@code NaN} if not applicable.
     */
    @Override
    public double getBufferErrorFraction(){
        return ((double)getBufferErrorCount())/getCurrentBufferLength();
    }

    private void addValueNoBootstrap(double value[]){
        addValueToBuffer(value);
        while (getCurrentBufferLength() > getBufferSize()) {
            removeFirstValueFromBuffer();
        }

        //Predict for each value in the buffer. Calculate percentage of errors.
        double errorInBuffer = getBufferErrorFraction();
        if (errorInBuffer > getHigherErrorThreshold()){
            setBootstrapMode(true); //If number of errors is above higher threshold, get into the bootstrap
        }
    }

    @Override
    public boolean isInBootstrapMode() {
        Object objBootstrapMode = currentState.get(_resolver.stringToLongKey(INTERNAL_BOOTSTRAP_MODE_KEY));
        if (objBootstrapMode!=null){
            return ((Boolean)objBootstrapMode).booleanValue();
        }
        currentState.set(_resolver.stringToLongKey(INTERNAL_BOOTSTRAP_MODE_KEY), Type.BOOL, true); //Start in bootstrap mode
        return true;
    }

    public void setBootstrapMode(boolean newBootstrapMode) {
        if (newBootstrapMode){
            //New state starts now
            currentState = graph().resolver().resolveState(this, true);

            //It would have been easy if not for keeping the buffers
            removeAllClasses();

            //Now step-by-step build new models
            double valueBuffer[] = getValueBuffer();
            int startIndex = 0;
            final int dims = getInputDimensions();
            while (startIndex+dims < valueBuffer.length) {
                double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex+dims);
                updateModelParameters(curValue);
                startIndex += dims;
            }
        }
        currentState.set(_resolver.stringToLongKey(INTERNAL_BOOTSTRAP_MODE_KEY), Type.BOOL, newBootstrapMode);
    }

    private void removeAllClasses(){
        int classes[] = getKnownClasses();
        for (int curClass : classes){
            currentState.set(_resolver.stringToLongKey(INTERNAL_TOTAL_KEY_PREFIX+curClass), Type.INT, 0);
            currentState.set(_resolver.stringToLongKey(INTERNAL_SUM_KEY_PREFIX+curClass), Type.DOUBLE_ARRAY, new double[0]);
            currentState.set(_resolver.stringToLongKey(INTERNAL_SUMSQUARE_KEY_PREFIX+curClass), Type.DOUBLE_ARRAY, new double[0]);
        }
        currentState.set(_resolver.stringToLongKey(INTERNAL_KNOWN_CLASSES_LIST), Type.INT_ARRAY, new int[0]);
    }

    public String allDistributionsToString(){
        String result = "";
        int allClasses[] = getKnownClasses();
        if (allClasses.length==0){
            return "No classes";
        }
        for (int classNum : allClasses) {
            initializeClassIfNecessary(classNum); //TODO should not be necessary. Double-check.
            int total = getClassTotal(classNum);
            if (total < 2){
                //Not enough data to build model for that class.
                result += classNum+": Not enough data("+total+")\n";
            }else{
                double sums[] = getSums(classNum);
                double means[] = getSums(classNum);
                for (int i=0;i<means.length;i++){
                    means[i] = means[i]/total;
                }
                double sumSquares[] = getSumSquares(classNum);
                result += classNum+": mean = ["; //TODO For now - cannot report variance from distribution
                for (int i=0;i<means.length;i++){
                    result += means[i]+", ";
                }
                result += "]\nCovariance:\n";
                result += "[";
                for (int j=0;j<means.length;j++){
                    result += Gaussian1D.getCovariance(sums[j], sumSquares[j], total)+", ";
                }
                result += "]\n";
            }
        }
        return result;//TODO Normalize on average?
    }
}
