package ml.classifier;

import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.ml.algorithm.AbstractClassifierSlidingWindowManagingNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by andrey.boytsov on 20/06/16.
 */
public class Generator {
    public static void main(String[] args) {
        Random rng = new Random(1);

        List<Double> f1 = new ArrayList<Double>();
        //List<Double> f2 = new ArrayList<Double>();

        for (int i = 0; i <= 1000; i++) {
            f1.add(rng.nextDouble()*10);
            //f2.add(rng.nextDouble());
        }
        //Now starting to make errors:
        //for (int i = 1001; i < 1019; i++) {
            //f1.add(rng.nextDouble());
            //f2.add(rng.nextDouble());
        //}
        //This should be the last drop: 19 errors of 60 value buffer (0.31...>0.3) should push us back into bootstrap
        //f1.add(rng.nextDouble());
        //f2.add(rng.nextDouble());

        //System.out.println(""+f1.size()+"  "+f2.size());

        System.out.print("double f1Array[] = new double[]{");
        for (int i=0;i<f1.size();i++){
            System.out.print(f1.get(i)+", ");
            if (i % 6 == 0){
                System.out.println();
            }
        }
        System.out.println("};");

        /*System.out.print("double f2Array[] = new double[]{");
        for (int i=0;i<f1.size();i++){
            System.out.print(f2.get(i)+", ");
            if (i % 6 == 0){
                System.out.println();
            }
        }
        System.out.println("};");*/
    }
}
