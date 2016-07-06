package org.mwg.core.scheduler;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;

import static org.mwg.task.Actions.*;

/**
 * @ignore ts
 */
public class TrampolineSchedulerTest {

    //@Test
    public void test() {
        Graph g = new GraphBuilder().withScheduler(new TrampolineScheduler()).build();
        g.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                repeatPar(100, printResult()).execute(g, new Callback<Object>() {
                    @Override
                    public void on(Object result) {
                        System.out.println();
                    }
                });
            }
        });
    }

}
