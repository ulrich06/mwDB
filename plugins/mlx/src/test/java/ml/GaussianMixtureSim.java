package ml;

import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Callback;
import org.mwg.ml.algorithm.profiling.GaussianGmmNode;
import org.mwg.core.scheduler.NoopScheduler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Scanner;

/**
 * Created by assaad on 04/04/16.
 */
public class GaussianMixtureSim {
    public static void main(String[] arg) {
        Graph graph = new GraphBuilder().addNodeType(new GaussianGmmNode.Factory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                boolean exit = false;
                String command;

                GaussianGmmNode node1 = (GaussianGmmNode) graph.newTypedNode(0, 0, "GaussianGmm");
                node1.set(GaussianGmmNode.LEVEL_KEY,2);
                node1.set(GaussianGmmNode.WIDTH_KEY,3);

                while (!exit) {
                    Scanner scanIn = new Scanner(System.in);
                    command = scanIn.nextLine();
                    String[] args = command.split(" ");
                    if (args[0].equals("exit")) {
                        exit = true;
                    }
                    if (args[0].equals("add")) {
                        double[] data = new double[args.length - 1];
                        for (int i = 0; i < data.length; i++) {
                            data[i] = Double.parseDouble(args[i + 1]);
                        }
                        //to set data here
                        node1.learnVector(data,new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {

                            }
                        });

                        long[] sublev = node1.getSubGraph();
                        for (int i = 0; i < sublev.length; i++) {
                            System.out.println("->" + sublev[i]);
                        }
                    }
                    if (args[0].equals("avg")) {
                        print(node1.getAvg());
                    }
                    if (args[0].equals("min")) {
                        print(node1.getMin());
                    }
                    if (args[0].equals("max")) {
                        print(node1.getMax());
                    }
                    if (args[0].equals("draw")) {
                        long[] sublev = node1.getSubGraph();
                        for (int i = 0; i < sublev.length; i++) {
                            System.out.println("->" + sublev[i]);
                        }
                    }


                }
            }
        });
    }

    public static void print(double[] val) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        if (val == null) {
            return;
        }

        for (int i = 0; i < val.length; i++) {
            System.out.print(formatter.format(val[i]) + " ");
        }
        System.out.println();

    }
}