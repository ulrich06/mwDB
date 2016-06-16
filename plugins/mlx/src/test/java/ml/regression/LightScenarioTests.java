package ml.regression;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Type;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.MLXPlugin;
import org.mwg.ml.algorithm.regression.AbstractLinearRegressionNode;
import org.mwg.ml.AbstractMLNode;
import org.mwg.ml.algorithm.regression.LinearRegressionNode;

import java.util.Random;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 5/24/2016.
 */
public class LightScenarioTests extends AbstractLinearRegressionTest{

    public static final int NUM_SWITCHES = 10;

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
        for (int cs = 0; cs < NUM_SWITCHES; cs++){
            final int correctSwitch = cs; //Have to make it final for inner class access
            final int LIGHT_ON_LX = 500;
            final int LIGHT_OFF_LX = 0;
            final int NUM_OF_TRIALS = NUM_SWITCHES; //One swipe

            final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
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
                        rjc.response = (chosenSwitch==correctSwitch)?LIGHT_ON_LX:LIGHT_OFF_LX;
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
                    //System.out.println(errorString);
                    lrNode.jump(2*NUM_OF_TRIALS, rjc); //Just in case correct switch is the last one.
                    // In that case it will cause going to bootstrap. 1 timestamp to get out.
                    assertFalse(errorString, rjc.bootstrapMode);

                    lrNode.free();
                    graph.disconnect(null);

                    for (int i=0;i<NUM_SWITCHES;i++) {
                        assertTrue(errorString,Math.abs(rjc.coefs[i] - ((i==correctSwitch)?LIGHT_ON_LX:LIGHT_OFF_LX)) < 1e-4);
                    }
                    assertTrue(errorString,Math.abs(rjc.intercept) < 1e-4);
                    assertTrue(errorString,rjc.bufferError < 1e-4);
                    assertTrue(errorString,rjc.l2Reg < 1e-4);
                }
            });
        }
    }


    /**
     * Just slightly more complicated version. Now the switches do not go back to off.
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
    public void noDelayRandomClickingSwitchResponse() {
        final Random rng = new Random(1);

        final int LIGHT_ON_LX = 500;
        final int LIGHT_OFF_LX = 0;
        final int BUFF_SIZE = 10;

        for (int cs = 0; cs < NUM_SWITCHES; cs++) {
            final int correctSwitch = cs; //Have to make it final for inner class access
            //System.out.println("Correct switch: "+correctSwitch);

            final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
            graph.connect(new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                    lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFF_SIZE);
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
                    while(!triedCorrectSwitch || index<2) { //Need at least 2 samples. Otherwise we can't distinguish from intercept.
                        int chosenSwitch = rng.nextInt(NUM_SWITCHES);
                        triedCorrectSwitch = (chosenSwitch==correctSwitch);
                        switchValues[chosenSwitch] = (switchValues[chosenSwitch]==0)?1:0;
                        rjc.value = switchValues;
                        rjc.response = (switchValues[correctSwitch] == 1)?LIGHT_ON_LX:LIGHT_OFF_LX;
                        lrNode.jump(index, rjc);
                        index++;
                    }
                    String errorString = ""+rjc.intercept;
                    for (int i=0;i<NUM_SWITCHES;i++){
                        errorString += " + "+rjc.coefs[i]+"*s"+i;
                    }
                    errorString += "\t"+rjc.bufferError+"\t"+rjc.l2Reg;
                    //System.out.println(errorString);
                    lrNode.jump(index, rjc);
                    index++; //Make sure index contains
                    //TODO Should I implement jumping in and out of bootstrap mode at the same time?
                    if (index < BUFF_SIZE){
                        assertTrue(errorString, rjc.bootstrapMode);
                    }else{
                        assertFalse(errorString, rjc.bootstrapMode);
                    }


                    lrNode.free();
                    graph.disconnect(null);

                    for (int i=0;i<NUM_SWITCHES;i++) {
                        assertTrue(errorString,Math.abs(rjc.coefs[i] - ((i==correctSwitch)?LIGHT_ON_LX:LIGHT_OFF_LX)) < 1e-4);
                    }
                    assertTrue(errorString,Math.abs(rjc.intercept) < 1e-4);
                    assertTrue(errorString,rjc.bufferError < 1e-4);
                    assertTrue(errorString,rjc.l2Reg < 1e-4);
                }
            });
        }
    }


    /**
     * Adding more assumptions. Now light level is a bit randomized.
     *
     * Simplifications:
     * - When the light is off, the light level is uniformly distributed between 0-30 Lx.
     * - When the light is on, the light level is uniformly distributed between 450-550 Lx.
     * - There is no lag. Once switch is on, the light turns on immediately.
     * - User turn switches on or off randomly. Initially all switches are off.
     *
     * When correct switch is tried at least once, it should be enough to detect the proper configuration.
     *
     * Later tests will add more realistic assumptions.
     */
    @Test
    public void noDelayRandomLightLevelSwitchResponse() {
        final Random rng = new Random(2);

        final int BUFF_SIZE = 10;

        for (int cs = 0; cs < NUM_SWITCHES; cs++) {
            final int correctSwitch = cs; //Have to make it final for inner class access
            //System.out.println("Correct switch: "+correctSwitch);

            final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
            graph.connect(new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                    lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFF_SIZE);
                    lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                    lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                    lrNode.set(AbstractMLNode.FROM, SWITCHES_STRING);

                    double switchValues[] = new double[NUM_SWITCHES];
                    for (int i = 0; i < NUM_SWITCHES; i++) {
                        switchValues[i] = 0;
                    }

                    RegressionJumpCallback rjc = new RegressionJumpCallback(FEATURES);
                    boolean triedCorrectSwitch = false;
                    int index = 0;
                    while (!triedCorrectSwitch || index<2) {
                        int chosenSwitch = rng.nextInt(NUM_SWITCHES);
                        triedCorrectSwitch = (chosenSwitch == correctSwitch);
                        switchValues[chosenSwitch] = (switchValues[chosenSwitch] == 0) ? 1 : 0;
                        rjc.value = switchValues;
                        rjc.response = (switchValues[correctSwitch] == 1) ? (rng.nextDouble() * 100 + 450) : (rng.nextDouble() * 30);
                        //System.out.println(switchValues[0]+"\t"+switchValues[1]+"\t"+switchValues[2]+"\t"+switchValues[3]+"\t"+switchValues[4]+"\t"+switchValues[5]+"\t"+rjc.response);
                        lrNode.jump(index, rjc);
                        index++;
                    }
                    lrNode.jump(index, rjc); //For already established model this new data can throw it back into bootstrap. Need 1 more point to recover.
                    index++;
                    //TODO Should I implement jumping in and out of bootstrap mode at the same time?
                    //Might as well be bootstrap - too few in the window.
                    String errorString = "" + rjc.intercept;
                    for (int i = 0; i < NUM_SWITCHES; i++) {
                        errorString += " + " + rjc.coefs[i] + "*s" + i;
                    }
                    errorString += "\t" + rjc.bufferError + "\t" + rjc.l2Reg;
                    //index++;
                    if (index < BUFF_SIZE){
                        assertTrue(errorString, rjc.bootstrapMode);
                    }else{
                        assertFalse(errorString, rjc.bootstrapMode);
                    }

                    lrNode.free();
                    graph.disconnect(null);

                    //One coefficient needs to be dominating
                    for (int i = 0; i < NUM_SWITCHES; i++) {
                        if (i == correctSwitch){
                            assertTrue(errorString, rjc.coefs[i] > 400);
                        }else{
                            assertTrue(errorString, rjc.coefs[i] < 40);
                        }
                    }
                    assertTrue(errorString, Math.abs(rjc.intercept) < 40);
                    //assertTrue(errorString, rjc.bufferError < 1e-4);
                    assertTrue(errorString, rjc.l2Reg < 1e-4);
                }
            });
        }
    }

    /**
     * Adding even more assumptions.
     *
     * Simplifications:
     * - When the light is off, the light level is uniformly distributed between 0-30 Lx.
     * - When the light is on, the light level is uniformly distributed between 450-550 Lx.
     * - There is a lag of 1 timeframe between turning on the switch and light level being reported.
     * - User turn switches on or off randomly. Initially all switches are off.
     *
     * When correct switch is tried at least once, it should be enough to detect the proper configuration.
     *
     * Later tests will add more realistic assumptions.
     */
    @Test
    public void delayedRandomLightLevelSwitchResponse() {
        final Random rng = new Random(3);

        final int LAG = 1; //Timeframes between turning the switch and seeing light change
        final int AFTER_TIMEFRAMES = 7; //Timeframes when everything is working normally after light level is reported

        final int BUFF_SIZE = 10*(LAG+AFTER_TIMEFRAMES);

        for (int cs = 0; cs < NUM_SWITCHES; cs++) {
            final int correctSwitch = cs; //Have to make it final for inner class access

            final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
            graph.connect(new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {
                    LinearRegressionNode lrNode = (LinearRegressionNode) graph.newTypedNode(0, 0, LinearRegressionNode.NAME);
                    lrNode.setProperty(AbstractLinearRegressionNode.BUFFER_SIZE_KEY, Type.INT, BUFF_SIZE);
                    lrNode.setProperty(AbstractLinearRegressionNode.LOW_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                    lrNode.setProperty(AbstractLinearRegressionNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 100.0);
                    lrNode.set(AbstractMLNode.FROM, SWITCHES_STRING);

                    double switchValues[] = new double[NUM_SWITCHES];
                    for (int i = 0; i < NUM_SWITCHES; i++) {
                        switchValues[i] = 0;
                    }

                    RegressionJumpCallback rjc = new RegressionJumpCallback(FEATURES);
                    boolean triedCorrectSwitch = false;
                    int index = 0;
                    while (!triedCorrectSwitch || index<2*(LAG+AFTER_TIMEFRAMES)) {
                        int chosenSwitch = rng.nextInt(NUM_SWITCHES);
                        triedCorrectSwitch = (chosenSwitch == correctSwitch);
                        switchValues[chosenSwitch] = (switchValues[chosenSwitch] == 0) ? 1 : 0;
                        rjc.value = switchValues;
                        //New values, old response
                        for (int i=0;i<LAG;i++){
                            lrNode.jump(index, rjc);
                            index++;
                        }
                        rjc.response = (switchValues[correctSwitch] == 1) ? (rng.nextDouble() * 100 + 450) : (rng.nextDouble() * 30);
                        //New values & new response
                        for (int i=0;i<AFTER_TIMEFRAMES;i++){
                            lrNode.jump(index, rjc);
                            index++;
                        }
                    }
                    String errorString = "" + rjc.intercept;
                    for (int i = 0; i < NUM_SWITCHES; i++) {
                        errorString += " + " + rjc.coefs[i] + "*s" + i;
                    }
                    errorString += "\t" + rjc.bufferError + "\t" + rjc.l2Reg;
                    //System.out.println(errorString);
                    //TODO Should I implement jumping in and out of bootstrap mode at the same time?
                    //assertFalse(errorString, rjc.bootstrapMode);

                    lrNode.free();
                    graph.disconnect(null);

                    //One coefficient needs to be dominating
                    for (int i = 0; i < NUM_SWITCHES; i++) {
                        if (i == correctSwitch){
                            assertTrue(errorString, rjc.coefs[i] > 370);
                        }else{
                            assertTrue(errorString, rjc.coefs[i] < 71);
                        }
                    }
                    assertTrue(errorString, Math.abs(rjc.intercept) < 20);
                    //assertTrue(errorString, rjc.bufferError < 1e-4); //there can be quite a lot of buffer error, actually
                    assertTrue(errorString, rjc.l2Reg < 1e-4);
                }
            });
        }
    }
}
