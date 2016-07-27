package org.mwg.core;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.core.scheduler.ExecutorScheduler;
import org.mwg.core.scheduler.HybridScheduler;
import org.mwg.core.scheduler.TrampolineScheduler;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import java.util.Random;

import static org.mwg.task.Actions.*;

/**
 * @ignore ts
 */
public class Benchmark5Test {

    public static void main(String[] args) {
        Graph g = new GraphBuilder()
                //.withScheduler(new HybridScheduler())
                  //.withScheduler(new TrampolineScheduler())
                   .withScheduler(new ExecutorScheduler())
                .build();
        g.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                final long previous = System.currentTimeMillis();

                repeatPar("1000", repeat("1000", newTask().then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Random random = new Random();
                        for(int i=0;i<100;i++){
                            random.nextFloat();
                        }
                        context.continueTask();
                    }
                }))).then(new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        System.out.println("End " + (System.currentTimeMillis() - previous) + " ms");
                        g.disconnect(null);
                    }
                }).execute(g, new Callback<TaskResult>() {
                    @Override
                    public void on(TaskResult result) {
                        System.out.println();
                    }
                });
            }
        });
        /*
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

}
