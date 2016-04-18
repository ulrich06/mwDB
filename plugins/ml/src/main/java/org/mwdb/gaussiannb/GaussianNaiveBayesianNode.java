package org.mwdb.gaussiannb;

import org.mwdb.*;
import org.mwdb.math.matrix.operation.MultivariateNormalDistribution;

import java.util.*;

/**
 * Created by Andrey Boytsov on 4/14/2016.
 */
public class GaussianNaiveBayesianNode extends AbstractNode implements KGaussianNaiveBayesianNode {

    //TODO Any synchronization?

    //TODO Drop class value to 0 before any prediction/learning?

    //TODO Try out changing parameters on the fly

    /**
     * Public keys - node parameters, values, etc.
     */
    public static final String VALUE_KEY = "value";
    public static final String CLASS_INDEX_KEY = "classIndex";
    public static final String BUFFER_SIZE_KEY = "bufferSize";
    public static final String INPUT_DIM_KEY = "inputDimensions";
    public static final String LOW_ERROR_THRESH_KEY = "lowerErrorThreshold";
    public static final String HIGH_ERROR_THRESH_KEY = "higherErrorThreshold";

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
    private final Map<Integer, MultivariateNormalDistribution> distributions = new HashMap<>();


    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    private int getClassIndex(){
        Object objClassIndex = att(CLASS_INDEX_KEY);
        Objects.requireNonNull(objClassIndex, "Class index must be not null");
        return ((Integer)objClassIndex).intValue();
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    private int getInputDimensions(){
        Object objClassIndex = att(INPUT_DIM_KEY);
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
     * @param clIndex Index of class attribute. That attribute will not participate in Gaussian creation.
     * @param bufSize Size of buffer is the minimum size for bootstrap and a size of sliding window for accuracy estimation
     * @param dims Expected dimensionality of value vector
     */
    public GaussianNaiveBayesianNode(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution, int clIndex,
                                     int bufSize, int dims, double lowerErrorThreshold, double higherErrorThreshold){
        super(p_world, p_time, p_id, p_graph, currentResolution);
        //TODO use assertions?
        illegalArgumentIfFalse(clIndex >= 0, "Class index should be non-negative");
        illegalArgumentIfFalse(dims > clIndex, "Class index should be less than number of dimensions");
        illegalArgumentIfFalse(bufSize > 0, "Sliding window size should be positive");
        illegalArgumentIfFalse( (lowerErrorThreshold >= 0)&&(lowerErrorThreshold <= 1),
                "Lower error threshold must be within [0;1] interval");
        illegalArgumentIfFalse( (higherErrorThreshold >= 0)&&(higherErrorThreshold <= 1),
                "Higher error threshold must be within [0;1] interval");
        illegalArgumentIfFalse(higherErrorThreshold>=lowerErrorThreshold,
                "Higher error threshold must be above or equal to lower error threshold");

        attSet(CLASS_INDEX_KEY, KType.INT, clIndex);
        attSet(INPUT_DIM_KEY, KType.INT, dims);
        attSet(BUFFER_SIZE_KEY, KType.INT, bufSize);
        attSet(INTERNAL_BOOTSTRAP_MODE_KEY, KType.BOOL, true); //Start in bootstrap mode
        attSet(LOW_ERROR_THRESH_KEY, KType.DOUBLE, lowerErrorThreshold);
        attSet(HIGH_ERROR_THRESH_KEY, KType.DOUBLE, higherErrorThreshold);
        attSet(INTERNAL_KNOWN_CLASSES_LIST, KType.INT_ARRAY, new int[0]);

        //TODO initialize attributes like sums, etc. ? For now - not necessary.

        setValueBuffer(new double[0]); //Value buffer, starts empty
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {
        // Nothing for now
    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {
        // Nothing for now
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        //TODO Changed class index? Need to recalculate everything
        //TODO Changed buffer size? Might also need recalculation
        //TODO Class index should be positive
        //TODO Input dimensions should be positive

        if (attributeName.equals(VALUE_KEY) && attributeType == KType.DOUBLE_ARRAY) {
            addValue((double[]) attributeValue);
        } else {
            super.attSet(attributeName, attributeType, attributeValue);
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
        attSet(INTERNAL_KNOWN_CLASSES_LIST, KType.INT_ARRAY, newKnownClasses);
    }

    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    private void addValue(double value[]){
        illegalArgumentIfFalse(value != null, "Value must be not null");
        illegalArgumentIfFalse(value.length != getInputDimensions(), "Class index is not included in the value");

        if (isInBootstrapMode()){
            addValueBootstrap(value);
        }else{
            addValueNoBootstrap(value);
        }
    }

    private final void setTotal(int classNum, int val){
        assert val >= 0;
        attSet(INTERNAL_TOTAL_KEY_PREFIX+classNum, KType.INT, val);
    }

    private final void setSums(int classNum, double[] vals){
        assert vals != null;
        attSet(INTERNAL_SUM_KEY_PREFIX+classNum, KType.DOUBLE_ARRAY, vals);
    }

    private final void setValueBuffer(double[] valueBuffer){
        assert valueBuffer != null;
        attSet(INTERNAL_VALUE_BUFFER_KEY, KType.DOUBLE_ARRAY, valueBuffer);
    }

    private final void setSumsSquared(int classNum, double[] vals){
        assert vals != null;
        attSet(INTERNAL_SUMSQUARE_KEY_PREFIX+classNum, KType.DOUBLE_ARRAY, vals);
    }

    private final int getClassTotal(int classNum){
        Object objClassTotal = att(INTERNAL_TOTAL_KEY_PREFIX+classNum);
        Objects.requireNonNull(objClassTotal, "Class total must be not null (class "+classNum+")");
        return ((Integer)objClassTotal).intValue();
    }

    private double[] getSums(int classNum){
        Object objSum = att(INTERNAL_SUM_KEY_PREFIX+classNum);
        Objects.requireNonNull(objSum, "Sums must be not null (class "+classNum+")");
        return (double [])objSum;
    }

    private double getHigherErrorThreshold(){
        Object objHET = att(HIGH_ERROR_THRESH_KEY);
        Objects.requireNonNull(objHET, "Higher error threshold must be not null");
        return (double)objHET;
    }

    private double getLowerErrorThreshold(){
        Object objLET = att(LOW_ERROR_THRESH_KEY);
        Objects.requireNonNull(objLET, "Lower error threshold must be not null");
        return (double)objLET;
    }

    private double[] getValueBuffer(){
        Object objValueBuffer = att(INTERNAL_VALUE_BUFFER_KEY);
        Objects.requireNonNull(objValueBuffer, "Value buffer must be not null");
        return (double [])objValueBuffer;
    }

    private double[] getSumSquares(int classNum){
        Object objSumSq = att(INTERNAL_SUMSQUARE_KEY_PREFIX+classNum);
        Objects.requireNonNull(objSumSq, "Sums of squares must be not null (class "+classNum+")");
        return (double [])objSumSq;
    }

    /**
     * Initializes map values for class num: sum, sum of squares and total.
     * @param classNum Number of class
     */
    private void initializeClassIfNecessary(int classNum){
        if (att(INTERNAL_TOTAL_KEY_PREFIX+classNum)!=null){
            return;
        }

        addToKnownClassesList(classNum);
        setTotal(classNum, 0);
        final int dimensions = getInputDimensions();
        setSums(classNum, new double[dimensions]);
        setSumsSquared(classNum, new double[dimensions*(dimensions+1)/2]);

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
        for (int i=0;i<value.length;i++) {
            currentSum[i] += value[i];
        }
        //Value at class index - force 0 (or it will be the ultimate predictor)
        currentSum[classIndex] = 0;
        setSums(classNum, currentSum);
        //TODO No need to put? Depends on whether att returns a copy. Just in case, re-put

        double currentSumSquares[] = getSumSquares(classNum);
        int k = 0;
        for (int i=0;i<value.length;i++){
            for (int j=i;j<value.length;j++){
                //Value at class index - force 0 (or it will be the ultimate predictor)
                if ((i!=classIndex)&&(j!=classIndex)) {
                    currentSumSquares[k] += value[i]*value[j];
                }
            }
            k++;
        }
        setSums(classNum, currentSumSquares);
        //TODO No need to put? Depends on whether att returns a copy. Just in case, re-put
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
        updateModelParameters(value);

        //Predict for each value in the buffer. Calculate percentage of errors.
        double errorInBuffer = calculateErrorInBuffer();
        if (errorInBuffer < getLowerErrorThreshold()){
            setBootstrapMode(false); //If number of errors is below lower threshold, get out of bootstrap
        }
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
        double sums[] = getSums(classNum);
        double sumSquares[] = getSumSquares(classNum);
        MultivariateNormalDistribution distr =
                MultivariateNormalDistribution.getDistribution(sums, sumSquares, total);
        return distr.density(value, true);//TODO Normalize on average?
    }

    private int predictValue(double value[]){
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
        Object objKnownClasses = att(INTERNAL_KNOWN_CLASSES_LIST);
        Objects.requireNonNull(objKnownClasses, "Known classes list must be not null");
        return (int [])objKnownClasses;
    }


    /**
     * @return Prediction accuracy for data in the buffer. {@code NaN} if not applicable.
     */
    private double calculateErrorInBuffer(){
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues =  valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0){
            return Double.NaN;
        }

        final int clIndex = getClassIndex();
        int errorCount = 0;
        while (startIndex+dims < valueBuffer.length){
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex+dims);
            int realClass = (int)curValue[clIndex];
            int predictedClass = predictValue(curValue);
            errorCount += (realClass==predictedClass)?1:0;

            //Continue the loop
            startIndex += dims;
        }
        return ((double)errorCount)/numValues;
    }

    private void addValueNoBootstrap(double value[]){
        addValueToBuffer(value);
        removeFirstValueFromBuffer();

        //Predict for each value in the buffer. Calculate percentage of errors.
        double errorInBuffer = calculateErrorInBuffer();
        if (errorInBuffer > getHigherErrorThreshold()){
            setBootstrapMode(true); //If number of errors is above higher threshold, get into the bootstrap
        }
    }

    @Override
    public boolean isInBootstrapMode() {
        Object objBootstrapMode = att(INTERNAL_BOOTSTRAP_MODE_KEY);
        Objects.requireNonNull(objBootstrapMode, "Class index must be not null");
        return ((Boolean)objBootstrapMode).booleanValue();
    }

    public void setBootstrapMode(boolean newBootstrapMode) {
        attSet(INTERNAL_BOOTSTRAP_MODE_KEY, KType.BOOL, newBootstrapMode);

        if (newBootstrapMode){
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
    }

    private void removeAllClasses(){
        int classes[] = getKnownClasses();
        for (int curClass : classes){
            attSet(INTERNAL_TOTAL_KEY_PREFIX+curClass, KType.INT, null);
            attSet(INTERNAL_SUM_KEY_PREFIX+curClass, KType.INT, null);
            attSet(INTERNAL_SUMSQUARE_KEY_PREFIX+curClass, KType.INT, null);
        }
        attSet(INTERNAL_KNOWN_CLASSES_LIST, KType.INT_ARRAY, new int[0]);
    }
}
