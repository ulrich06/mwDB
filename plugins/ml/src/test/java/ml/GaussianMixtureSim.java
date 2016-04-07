package ml;

import org.mwdb.GaussianNodeFactory;
import org.mwdb.GraphBuilder;
import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.gmm.KGaussianNode;
import org.mwdb.task.NoopScheduler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Scanner;

/**
 * Created by assaad on 04/04/16.
 */
public class GaussianMixtureSim {
   public static void main(String[] arg){
       KGraph graph = GraphBuilder.builder().withFactory(new GaussianNodeFactory()).withScheduler(new NoopScheduler()).build();
       graph.connect(new KCallback<Boolean>() {
           @Override
           public void on(Boolean result) {
               boolean exit=false;
               String command;

               KGaussianNode node1 =  (KGaussianNode) graph.newNode(0,0,"GaussianNode");
               node1.configMixture(2,3);

               while(!exit){
                   Scanner scanIn = new Scanner(System.in);
                   command = scanIn.nextLine();
                   String[] args=command.split(" ");
                   if(args[0].equals("exit")){
                       exit=true;
                   }
                   if(args[0].equals("add")){
                       double[] data= new double[args.length-1];
                       for(int i=0;i<data.length;i++){
                           data[i]=Double.parseDouble(args[i+1]);
                       }
                       node1.learn(data);

                       long[] sublev=node1.getSubGraph();
                       for(int i=0;i<sublev.length;i++){
                           System.out.println("->"+ sublev[i]);
                       }
                   }
                   if(args[0].equals("avg")){
                       print(node1.getAvg());
                   }
                   if(args[0].equals("min")){
                       print(node1.getMin());
                   }
                   if(args[0].equals("max")){
                       print(node1.getMax());
                   }
                   if(args[0].equals("draw")){
                       long[] sublev=node1.getSubGraph();
                       for(int i=0;i<sublev.length;i++){
                           System.out.println("->"+ sublev[i]);
                       }
                   }


               }
           }
       });
   }

    public static void print(double[] val){
        NumberFormat formatter = new DecimalFormat("#0.00");
        if(val==null){
            return;
        }

        for(int i=0;i<val.length;i++){
            System.out.print(formatter.format(val[i])+" ");
        }
        System.out.println();

    }
}