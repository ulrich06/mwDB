package ml;

import org.mwdb.*;
import org.mwdb.polynomial.KPolynomialNode;
import org.mwdb.polynomial.PolynomialNode;
import org.mwdb.task.NoopScheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by assaad on 08/04/16.
 */
public class TestDb {
    public static void main(String[] arg) {

        String loc = "/Users/assaad/work/github/eurusd/";

       /* Date d=new Date();
        d.setTime(Long.parseLong("991949460000"));*/

        long starttime;
        long endtime;
        double res;
        // final TreeMap<Long, Double> eurUsd = new TreeMap<Long, Double>();
        final ArrayList<Long> timestamps = new ArrayList<>();
        final ArrayList<Double> euros = new ArrayList<>();


        starttime = System.nanoTime();
        String csvFile = loc + "Eur USD database/EURUSD_";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            for (int year = 2000; year < 2016; year++) {

                br = new BufferedReader(new FileReader(csvFile + year + ".csv"));
                while ((line = br.readLine()) != null) {
                    // use comma as separator 2000.05.30,17:35
                    String[] values = line.split(cvsSplitBy);
                    Long timestamp = getTimeStamp(values[0]);
                    Double val = Double.parseDouble(values[1]);
                    //    eurUsd.put(timestamp, val);
                    timestamps.add(timestamp);
                    euros.add(val);
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }


        endtime = System.nanoTime();
        res = ((double) (endtime - starttime)) / (1000000000);
        System.out.println("Loaded :" + timestamps.size() + " values in " + res + " s!");
        // System.out.println("Loaded :" + size + " values in " + res + " s!");

        final KGraph graph = GraphBuilder.builder().withStorage(new RocksDBStorage("data")).withAutoSave(10000).withFactory(new PolynomialNodeFactory()).withScheduler(new NoopScheduler()).build();
        graph.connect(new KCallback<Boolean>() {
                          @Override
                          public void on(Boolean result) {

                              long starttime, endtime;
                              double d;

                              starttime = System.nanoTime();
                              // KNode normalNode = graph.newNode(0, timestamps.get(0));
                              KNode normalNode = graph.newNode(0, 0);
                              for (int i = 0; i < timestamps.size(); i++) {
                 /*   if(i%1000000==0){
                        System.out.println(i);
                    }*/
                                  final int i1 = i;
                                  final long t = timestamps.get(i);
                                  normalNode.jump(t, new KCallback<KNode>() {
                                      @Override
                                      public void on(KNode result) {
                                          try {
                                              result.attSet("euroUsd", KType.DOUBLE, euros.get(i1));
                                          } catch (Exception ex) {
                                              ex.printStackTrace();
                                          }
                                          result.free();
                                      }
                                  });
                              }
                              endtime = System.nanoTime();
                              d = (endtime - starttime);
                              d = d / 1000000000;
                              d = timestamps.size() / d;
                              System.out.println("KNode insert speed: " + d + " values/s");

                              normalNode.timepoints(KConstants.BEGINNING_OF_TIME, KConstants.END_OF_TIME, new KCallback<long[]>() {
                                  @Override
                                  public void on(long[] result) {
                                      System.out.println("KNode number of timepoints: " + result.length);
                                  }
                              });

                              starttime = System.nanoTime();
                              PolynomialNode polyNode = (PolynomialNode) graph.newNode(0, timestamps.get(0), "PolynomialNode");
                              final double precision = 0.05;
                              polyNode.setPrecision(precision);
                              for (int i = 0; i < timestamps.size(); i++) {
                                  final int i1 = i;
                                  final long t = timestamps.get(i);
                                  polyNode.jump(t, new KCallback<PolynomialNode>() {
                                      @Override
                                      public void on(PolynomialNode result) {
                                          result.set(euros.get(i1));
                                          result.free();
                                      }
                                  });
                              }
                              endtime = System.nanoTime();
                              d = (endtime - starttime);
                              d = d / 1000000000;
                              d = timestamps.size() / d;
                              System.out.println("Polynomial insert speed: " + d + " ms");

                              polyNode.timepoints(KConstants.BEGINNING_OF_TIME, KConstants.END_OF_TIME, new KCallback<long[]>() {
                                  @Override
                                  public void on(long[] result) {
                                      System.out.println("Polynomial number of timepoints: " + result.length);
                                  }
                              });

                              final int[] error2=new int[1];
                              error2[0]=0;
                              starttime = System.nanoTime();
                              for (int i = 0; i < timestamps.size(); i++) {
                 /*   if(i%1000000==0){
                        System.out.println(i);
                    }*/
                                  final int i1 = i;
                                  final long t = timestamps.get(i);
                                  normalNode.jump(t, new KCallback<KNode>() {
                                      @Override
                                      public void on(KNode result) {
                                          try {
                                              double d = (double) result.att("euroUsd");
                                              if (Math.abs(d - euros.get(i1)) > precision) {
                                                  error2[0]++;
                                                  //System.out.println("Error " + d + " " + euros.get(i1));
                                              }
                                          } catch (Exception ex) {
                                              ex.printStackTrace();
                                          }
                                          result.free();
                                      }
                                  });
                              }
                              endtime = System.nanoTime();
                              d = (endtime - starttime);
                              d = d / 1000000000;
                              d = timestamps.size() / d;
                              System.out.println("Normal read speed: " + d + " ms");
                              System.out.println(error2[0]);
                              System.out.println();



                              final int[] error=new int[1];
                              starttime = System.nanoTime();
                              for (int i = 0; i < timestamps.size(); i++) {
                                  final int i1 = i;
                                  final long t = timestamps.get(i);
                                  if(i1==2632074 ){
                                      int x=0;
                                  }
                                  polyNode.jump(t, new KCallback<KPolynomialNode>() {
                                      @Override
                                      public void on(KPolynomialNode result) {
                                          try {

                                              double d = result.get();
                                              if (Math.abs(d - euros.get(i1)) > precision) {
                                                  error[0]++;
                                              }
                                          } catch (Exception ex) {
                                              ex.printStackTrace();
                                          }
                                          result.free();
                                      }
                                  });
                              }
                              endtime = System.nanoTime();
                              d = (endtime - starttime);
                              d = d / 1000000000;
                              d = timestamps.size() / d;
                              System.out.println("Polynomial read speed: " + d + " ms");
                              System.out.println(error[0]);


                              graph.disconnect(new KCallback<Boolean>() {
                                  @Override
                                  public void on(Boolean result) {

                                  }
                              });
                          }
                      }

        );

    }

    public static long getTimeStamp(String s) {
        //2014.11.28 16:31
        SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat(
                "yyyy.MM.dd hh:mm");
        Date lFromDate1 = null;
        try {
            lFromDate1 = datetimeFormatter1.parse(s);
            return lFromDate1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
