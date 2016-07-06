package org.mwg.core.scheduler;

import org.junit.Test;
import org.mwg.*;

import static org.mwg.task.Actions.print;
import static org.mwg.task.Actions.repeat;
import static org.mwg.task.Actions.repeatPar;

/**
 * @ignore ts
 */
public class ExecutorSchedulerTest {

    @Test
    public void test() {
        Graph g = new GraphBuilder().withScheduler(new ExecutorScheduler()).build();
        DeferCounterSync waiter = g.newSyncCounter(1);
        g.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                repeatPar(100, print("{{result}}")).execute(g, new Callback<Object>() {
                    @Override
                    public void on(Object result) {
                        System.out.println("end");
                        g.disconnect(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("Disconnected");
                                waiter.count();
                            }
                        });
                    }
                });
            }
        });
        waiter.waitResult();
        System.out.println("Result are here...");

/*
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
    }

}
