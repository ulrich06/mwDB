package org.mwg.core.scheduler;

import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.task.TaskResult;

import static org.mwg.task.Actions.print;
import static org.mwg.task.Actions.repeat;
import static org.mwg.task.Actions.repeatPar;

/**
 * @ignore ts
 */
public class HybridSchedulerTest {

  //  @Test
    public void test() {
        Graph g = new GraphBuilder().withScheduler(new HybridScheduler()).build();
        g.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                repeatPar("100", repeat("100",print("{{result}}"))).execute(g, new Callback<TaskResult>() {
                    @Override
                    public void on(TaskResult result) {
                        System.out.println();

                    }
                });
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
