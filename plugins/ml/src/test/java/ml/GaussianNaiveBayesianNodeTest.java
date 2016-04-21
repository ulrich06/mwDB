package ml;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.gaussiannb.KGaussianNaiveBayesianNode;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.gmm.KGaussianNode;
import org.mwdb.task.NoopScheduler;

import javax.sound.midi.Soundbank;

import static org.junit.Assert.assertTrue;

/**
 * Created by Andrey Boytsov on 4/18/2016.
 */
public class GaussianNaiveBayesianNodeTest {
    //TODO Changing parameters on the fly

    double dummyDataset1[][] = new double[][]{{18.1875,2},{18.1875,0},{18.1875,0},{18.1875,2},{18.1875,2},{18.1875,0},{18.1875,0},{18.1875,0},{21.66,1},{21.276,1},
            {21.1987,0},{21.1987,1},{21.0933,0},{21.0933,0},{21.0,0},{20.9053,0},{20.9053,0},{20.816,0},{20.816,0},{20.43,0},{16.61,2},{16.282,2},{16.282,2},{16.1087,2},
            {16.1087,2},{16.1087,2},{16.1087,2},{22.156,1},{21.82,1},{21.6973,1},{21.4453,1},{21.304,0},{21.304,1},{21.304,1},{20.7747,1},{20.652,0},{20.652,0},{20.264,0},
            {20.136,0},{20.0227,0},{17.8847,0},{17.8847,2},{17.7133,0},{21.0627,1},{20.62,0},{20.512,0},{20.278,0},{20.278,0},{20.278,0},{20.172,0},{20.0567,0},{19.5873,0},
            {19.5873,0},{19.342,0},{19.342,0},{19.1,0},{18.9687,0},{18.8733,0},{18.7487,0},{18.7487,0},{14.8233,2},{20.166,0},{20.166,0},{20.0807,0},{19.9993,0},{19.9007,0},
            {19.9007,0},{19.9007,0},{19.732,0},{19.732,0},{19.732,0},{19.6387,0},{19.4633,0},{19.35,0},{19.35,0},{19.2593,0},{19.2593,0},{19.162,0},{19.162,0},{19.046,0},
            {15.3673,2},{15.5667,2},{15.912,2},{16.7087,2},{17.0047,2},{18.2773,2},{18.9313,0},{19.6307,0},{20.2887,0},{20.5953,0},{21.1547,0},{21.4453,0},{21.4453,1},{21.7227,1},
            {22.6267,1},{23.2293,1},{23.2533,1},{23.008,1},{22.5013,1},{21.5867,1},{18.278,0},{19.196,0},{19.668,0},{20.7053,0},{21.5107,1},{22.04,1},{22.284,1},{23.6413,1},
            {23.9027,1},{24.192,1},{23.7573,1},{23.6547,1},{23.444,1},{23.3667,1},{22.992,1},{22.9067,1},{22.8147,1},{22.508,1},{22.2853,1},{21.6267,1},{17.694,2},{17.5847,2},
            {17.4973,2},{17.428,2},{17.378,2},{17.378,2},{20.2747,0},{20.2747,0},{20.05,0},{19.8027,0},{19.8027,0},{19.8027,0},{19.8027,0},{19.8027,0},{19.6587,0},{19.2573,0},
            {19.2573,0},{18.526,0},{18.3987,0},{18.29,0},{15.256,2},{15.0933,2},{14.7393,2},{14.6693,2},{13.2193,2},{13.104,2},{13.02,2},{12.822,2},{12.7767,2},{12.7767,2},{12.7533,2},
            {12.7533,2},{12.54,2},{12.4807,2},{12.45,2},{12.45,2},{12.39,2},{12.39,2},{12.3793,2},{12.3793,2},{11.954,2},{11.922,2},{11.9247,2},{16.778,2},{16.778,2},{16.67,2},
            {16.67,2},{16.2967,2},{16.0707,2},{15.904,2},{15.904,2},{15.904,2},{15.7933,2},{15.7933,2},{15.572,2},{15.4493,2},{15.4493,2},{15.3173,2},{15.016,2},{14.7013,2},{11.3867,2},
            {11.39,2},{18.5233,0},{18.3633,0},{18.2787,0},{18.19,0},{18.1127,0},{18.1127,0},{18.1127,0},{18.0147,0},{18.0147,0},{17.9193,2},{17.9193,0},{17.9193,0},{17.65,2},{17.54,2},
            {17.4687,2},{17.3727,2},{17.3727,2},{17.16,2},{13.916,2},{13.8427,2},{13.8427,2},{13.7767,2},{13.77,2},{13.8347,2},{20.0467,0},{19.89,0},{19.89,0},{19.734,0},{19.5793,0},
            {19.4047,0},{19.4047,0},{19.3147,0},{18.9693,0},{18.8967,0},{18.8167,0},{18.8167,0},{18.6413,0},{18.2853,0},{15.244,2},{15.2413,2},{15.4493,2},{15.6053,2},{15.7587,2},
            {17.5047,2},{17.5047,2},{18.6807,0},{19.9587,0},{19.9893,0},{19.9893,0},{20.0267,0},{19.982,0},{19.7433,0},{19.336,0},{18.7047,0},{18.6213,0},{18.5413,0},{18.1667,0},
            {18.0813,0},{15.3487,2},{16.5513,2},{17.0853,2},{18.2433,0},{18.2433,0},{20.1013,0},{20.572,0},{20.7627,0},{21.344,0},{21.5,1},{21.6333,1},{21.812,1},{21.796,1},{21.656,1},
            {21.1987,0},{20.5147,0},{19.9907,0},{19.866,0},{18.9833,0},{18.9833,0},{15.8907,2},{15.8907,2},{15.6987,2},{15.6367,2},{15.5613,2},{22.5787,1},{22.472,1},{22.364,1},
            {22.1413,1},{21.8173,1},{21.6947,1},{21.5787,1},{21.4613,1},{21.4613,1},{21.3533,1},{21.2187,0},{21.2187,1},{20.8533,0},{20.7373,0},{19.9907,0},{16.8433,2},{16.7447,2},
            {16.7447,2},{16.6733,2},{16.6733,2},{16.5927,2},{16.5927,2},{16.5613,2},{16.55,2},{20.452,0},{20.452,0},{20.1107,0},{19.6267,0},{19.314,0},{19.314,0},{19.186,0},{19.186,0},
            {19.0833,0},{18.7867,0},{18.6273,0},{14.65,2},{14.65,2},{14.61,2},{14.7347,2},{14.8073,2},{14.8073,2},{22.804,1},{22.4933,1},{22.2733,1},{22.1787,1},{21.86,1},{21.7507,1},
            {21.7507,1},{21.628,1},{21.4187,1},{21.3,0},{21.3,1},{21.3,1},{20.7907,0},{20.6773,0},{16.9627,2},{16.9627,2},{16.908,2},{16.8433,2},{16.8433,2},{16.8,2},{23.3707,1},
            {22.992,1},{22.5413,1},{22.432,1},{22.216,1},{22.216,1},{22.216,1},{22.216,1},{22.104,1},{21.9733,1},{21.9733,1},{21.84,1},{21.5613,0},{21.4453,0},{17.472,2},{17.472,2},
            {17.2833,2},{17.2833,2},{17.2833,2},{24.992,1},{24.992,1},{24.992,1},{24.916,1},{24.8373,1},{24.8373,1},{24.8373,1},{24.76,1},{24.56,1},{24.4853,1},{24.2813,1},{24.2813,1},
            {24.1707,1},{24.0587,1},{23.74,1},{19.7433,0},{19.9073,0},{20.544,0},{20.984,1},{21.712,0},{21.944,1},{23.3147,1},{23.3147,1},{23.628,1},{23.9667,1},{24.0747,1},{24.032,1},
            {23.5893,1},{23.5893,1},{23.364,1},{23.2053,1},{23.1173,1},{22.9213,1},{22.4053,1},{21.832,1},{20.9027,0},{21.1853,0},{22.4333,1},{23.748,1},{23.9027,1},{24.2733,1},
            {24.2733,1},{24.2507,1},{24.016,1},{24.016,1},{24.016,1},{24.016,1},{23.7467,1},{23.1427,1},{23.0307,1},{23.0307,1},{22.8213,1},{22.716,1},{22.5933,1},{22.0493,1},{19.5267,0},
            {19.476,0},{19.3933,0},{19.3933,0},{19.3633,0},{21.1693,1},{21.304,1},{21.0467,0},{21.0467,1},{20.988,0},{20.9227,0},{20.8867,0},{20.8867,1},{20.8867,1},{20.8867,0},{20.832,0},
            {20.7733,0},{20.7173,0},{20.6707,1},{20.624,1},{18.51,2},{18.496,0},{18.4653,0},{18.5033,0},{18.5033,2},{18.5273,0},{21.84,1},{21.64,1},{21.564,1},{21.24,1},{21.152,1},
            {21.152,0},{21.0573,1},{20.988,0},{20.988,1},{20.844,0},{20.844,1},{20.7507,0},{20.4233,1},{20.2667,0},{18.6493,0},{18.6493,0},{18.6307,0},{18.6307,2},{18.6307,0},{18.6207,0},
            {18.6113,0},{20.568,0},{20.4033,0},{20.21,0},{20.1367,0},{19.9807,0},{19.8313,0},{19.8313,0},{19.8313,0},{19.6787,0},{19.606,0},{19.4787,0},{19.4787,0},{19.166,0},{17.8433,2},
            {17.8433,2},{17.8433,2},{17.772,2},{17.772,2},{18.0607,0},{21.2933,1},{20.976,1},{20.7747,0},{20.7747,1},{20.688,0},{20.688,0},{20.688,1},{20.688,0},{20.5853,0},{20.5853,0},
            {20.47,0},{20.47,0},{20.3067,0},{20.168,0},{16.874,2},{16.7107,2},{20.1413,0},{20.0667,0},{19.684,0},{19.684,0},{19.598,0},{19.5,0},{19.5,0},{19.5,0},{19.4193,0},{19.4193,0},
            {19.3333,0},{19.2493,0},{19.1473,0},{19.04,0},{18.85,0},{18.85,0},{18.4487,2},{18.4487,0},{16.2613,2},{17.7693,2},{17.982,0},{18.18,0},{18.3353,0},{19.2253,0},{19.6227,0},
            {19.788,0},{19.788,0},{20.4133,0},{20.458,0},{20.4767,0},{20.1593,0},{20.1593,0},{19.3733,0},{19.29,0},{19.2167,0},{19.122,0},{18.8027,0},{18.634,0},{15.9593,2},{16.5987,2},
            {16.5987,2},{19.3307,0},{20.4893,0},{21.28,1},{21.7333,1},{22.292,1},{22.468,1},{22.984,1},{22.8867,1},{22.5707,1},{22.5707,1},{22.484,1},{22.4187,1},{21.984,1},{21.4987,1},
            {21.2693,0},{20.7973,0},{20.704,0},{17.65,2},{17.5767,2},{17.568,0},{17.568,0},{23.0413,1},{22.5853,1},{22.36,1},{22.2293,1},{22.008,1},{22.008,1},{21.784,1},{21.664,1},
            {21.664,1},{21.664,1},{21.5347,1},{21.4373,1},{21.3133,1},{21.3133,0},{20.9573,0},{20.7493,0},{18.6233,0},{18.6233,0},{18.6233,0},{18.5613,0},{18.5167,0},{18.5067,2},{24.812,1},
            {24.756,1},{24.6173,1},{24.5373,1},{24.4867,1},{24.4867,1},{24.4333,1},{24.3,1},{24.228,1},{24.228,1},{24.0573,1},{23.9733,1},{23.9,1},{23.8067,1},{20.7627,1}};

    boolean bootstraps1[] = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, };

    @Test
    public void test() {
        KGraph graph = GraphBuilder.builder().withFactory(new GaussianNaiveBayesianNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KGaussianNaiveBayesianNode gaussianNBNode = (KGaussianNaiveBayesianNode) graph.newNode(0, 0, "GaussianNaiveBayesianNode");

                int errors = 0;

                //TODO Those should not be necessary
                gaussianNBNode.attSet("_knownClassesList", KType.INT_ARRAY, new int[0]);
                gaussianNBNode.attSet("_bootstrapMode", KType.BOOL, true); //Start in bootstrap mode
                gaussianNBNode.attSet("_valueBuffer", KType.DOUBLE_ARRAY, new double[0]); //Value buffer, starts empty

                //Set the attributes
                gaussianNBNode.attSet(KGaussianNaiveBayesianNode.CLASS_INDEX_KEY, KType.INT, 1);
                gaussianNBNode.attSet(KGaussianNaiveBayesianNode.INPUT_DIM_KEY, KType.INT, 2);
                gaussianNBNode.attSet(KGaussianNaiveBayesianNode.BUFFER_SIZE_KEY, KType.INT, 60);
                gaussianNBNode.attSet(KGaussianNaiveBayesianNode.LOW_ERROR_THRESH_KEY, KType.DOUBLE, 0.2);
                gaussianNBNode.attSet(KGaussianNaiveBayesianNode.HIGH_ERROR_THRESH_KEY, KType.DOUBLE, 0.3);

                for (int i = 0; i < dummyDataset1.length; i++) {
                    gaussianNBNode.attSet("value", KType.DOUBLE_ARRAY, dummyDataset1[i]);
                    if (gaussianNBNode.isInBootstrapMode()!=bootstraps1[i]){
                        System.out.println(i+" EXPECTED:"+bootstraps1[i]+"\t"+
                                gaussianNBNode.getBufferErrorCount()+"/"+gaussianNBNode.getCurrentBufferLength()+"="+gaussianNBNode.getBufferErrorFraction());
                        errors++;
                    }else{
                        System.out.println(i+" CORRECT:"+bootstraps1[i]+"\t"+
                                gaussianNBNode.getBufferErrorCount()+"/"+gaussianNBNode.getCurrentBufferLength()+"="+gaussianNBNode.getBufferErrorFraction());
                    }
                    int rbc[] = gaussianNBNode.getRealBufferClasses();
                    System.out.print("[");
                    for (int j=0;j<rbc.length;j++) {
                        System.out.print(rbc[j]+", ");
                    }
                    System.out.println("]");
                    int pbc[] = gaussianNBNode.getPredictedBufferClasses();
                    System.out.print("[");
                    for (int j=0;j<pbc.length;j++) {
                        System.out.print(pbc[j]+", ");
                    }
                    System.out.println("]");
                    //assertTrue(gaussianNBNode.isInBootstrapMode()==bootstraps1[i]);
                }

                gaussianNBNode.free();

                graph.disconnect(null);

                System.out.println("Errors: "+errors);
            }
        });
    }

}
