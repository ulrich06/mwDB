package org.mwg.core.scheduler;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;

import static org.mwg.task.Actions.print;
import static org.mwg.task.Actions.repeatPar;

/**
 * @ignore ts
 */
public class ExecutorSchedulerTest {

    //@Test
    public void test() {
        Graph g = new GraphBuilder().withScheduler(new ExecutorScheduler()).build();
        g.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                repeatPar(100, print("{{result}}")).execute(g, new Callback<Object>() {
                    @Override
                    public void on(Object result) {
                        System.out.println();
                    }
                });

            }
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
