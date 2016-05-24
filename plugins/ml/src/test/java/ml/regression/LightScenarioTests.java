package ml.regression;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.NoopScheduler;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;
import org.mwg.ml.common.AbstractMLNode;

import java.util.Random;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 5/24/2016.
 */
public class LightScenarioTests extends AbstractLinearRegressionTest{

    public static final int NUM_SWITCHES = 4;

    public static final String SWITCHES_STRING;

    public static final String FEATURES[] = new String[NUM_SWITCHES];

    static{
        //Will generate a string like "s0;s1;s2;s3"
        StringBuilder switchesStringBuilder = new StringBuilder();
        for (int i=0;i<NUM_SWITCHES;i++){
            if (i!=0){
                switchesStringBuilder.append(AbstractMLNode.FROM_SEPARATOR);
            }
            FEATURES[i] = "s"+i;
            switchesStringBuilder.append("s");
            switchesStringBuilder.append(i);
        }
        SWITCHES_STRING = switchesStringBuilder.toString();
    }

    //TODO use light switch scenario from the dataset as well?

    /**
     * Simplest available test for "guess the switch" scenario.
     *
     * Simplifications:
     * - When the light is off, the light level is 0.
     * - When the light is on, the light level is strictly 500 Lx.
     * - There is no lag. Once switch is on, the light turns on immediately.
     * - User turns some switch on, then off. No two switches are on at the same time.
     *
     * One loop over the switches should be enough to detect the proper configuration.
     *
     * Later tests will add more realistic assumptions.
     */
    @Test
    public void noDelaySwitchResponse() {
        Random rng = new Random(1);

        final int correctSwitch = rng.nextInt(NUM_SWITCHES);
        final int LIGHT_ON_LX = 500;
        final int LIGHT_OFF_LX = 0;
        final int NUM_OF_TRIALS = NUM_SWITCHES; //One swipe

        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, NUM_OF_TRIALS);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.set(AbstractMLNode.FROM, SWITCHES_STRING);

                double switchValues[] = new double[NUM_SWITCHES];
                for (int i=0;i<NUM_SWITCHES;i++){
                    switchValues[i] = 0;
                }

                RegressionJumpCallback rjc = new RegressionJumpCallback(FEATURES);
                for (int i = 0; i < NUM_OF_TRIALS; i++) {
                    int chosenSwitch = i % NUM_SWITCHES;
                    switchValues[chosenSwitch] = 1;
                    rjc.value = switchValues;
                    rjc.response = (chosenSwitch==correctSwitch)?500:0;
                    lrNode.jump(2*i, rjc);
                    switchValues[chosenSwitch] = 0;
                    rjc.value = switchValues;
                    rjc.response = 0;
                    lrNode.jump(2*i+1, rjc);
                    //We don't care about bootstrap mode here
                }
                String errorString = ""+rjc.intercept;
                for (int i=0;i<NUM_SWITCHES;i++){
                    errorString += " +"+rjc.coefs[i]+"*s"+i;
                }
                errorString += "\t"+rjc.bufferError+"\t"+rjc.l2Reg;
                System.out.println(errorString);
                assertFalse(errorString, rjc.bootstrapMode);

                lrNode.free();
                graph.disconnect(null);

                for (int i=0;i<NUM_SWITCHES;i++) {
                    assertTrue(errorString,Math.abs(rjc.coefs[i] - ((i==correctSwitch)?500:0)) < 1e-4);
                }
                assertTrue(errorString,Math.abs(rjc.intercept) < 1e-4);
                assertTrue(errorString,rjc.bufferError < 1e-4);
                assertTrue(errorString,rjc.l2Reg < 1e-4);
            }
        });
    }


    /**
     * Just slightly more complicated version. Now the switches do not go back.
     *
     * Simplifications:
     * - When the light is off, the light level is 0.
     * - When the light is on, the light level is strictly 500 Lx.
     * - There is no lag. Once switch is on, the light turns on immediately.
     * - User turn switches on or off randomly. Initially all switches are off.
     *
     * When correct switch is tried at least once, it should be enough to detect the proper configuration.
     *
     * Later tests will add more realistic assumptions.
     */
    @Test
    public void noDelayRandomizedSwitchResponse() {
        Random rng = new Random(1);

        final int correctSwitch = rng.nextInt(NUM_SWITCHES);
        System.out.println("Correct switch: "+correctSwitch);
        final int LIGHT_ON_LX = 500;
        final int LIGHT_OFF_LX = 0;

        Graph graph = GraphBuilder.builder().withFactory(new LinearRegressionNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, 2*NUM_SWITCHES);
                lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                lrNode.set(AbstractMLNode.FROM, SWITCHES_STRING);

                double switchValues[] = new double[NUM_SWITCHES];
                for (int i=0;i<NUM_SWITCHES;i++){
                    switchValues[i] = 0;
                }

                RegressionJumpCallback rjc = new RegressionJumpCallback(FEATURES);
                boolean triedCorrectSwitch = false;
                int index = 0;
                while(!triedCorrectSwitch) {
                    int chosenSwitch = rng.nextInt(NUM_SWITCHES);
                    triedCorrectSwitch = (chosenSwitch==correctSwitch);
                    switchValues[chosenSwitch] = (switchValues[chosenSwitch]==0)?1:0;
                    rjc.value = switchValues;
                    rjc.response = (switchValues[correctSwitch] == 1)?500:0;
                    System.out.println(switchValues[chosenSwitch]+"\t"+chosenSwitch+"\t"+rjc.response);
                    lrNode.jump(index, rjc);
                    index++;
                }
                String errorString = ""+rjc.intercept;
                for (int i=0;i<NUM_SWITCHES;i++){
                    errorString += " + "+rjc.coefs[i]+"*s"+i;
                }
                errorString += "\t"+rjc.bufferError+"\t"+rjc.l2Reg;
                System.out.println(errorString);
                //TODO Should I implement jumping in and out of bootstrap mode at the same time?
                //lrNode.jump(index, rjc);
                //assertFalse(errorString, rjc.bootstrapMode);

                lrNode.free();
                graph.disconnect(null);

                for (int i=0;i<NUM_SWITCHES;i++) {
                    assertTrue(errorString,Math.abs(rjc.coefs[i] - ((i==correctSwitch)?500:0)) < 1e-4);
                }
                assertTrue(errorString,Math.abs(rjc.intercept) < 1e-4);
                assertTrue(errorString,rjc.bufferError < 1e-4);
                assertTrue(errorString,rjc.l2Reg < 1e-4);
            }
        });
    }
}
