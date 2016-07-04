package org.mwg;

import org.junit.Test;
import org.mwg.importer.ImporterPlugin;
import org.mwg.task.Task;

import static org.mwg.importer.ImporterActions.readLines;
import static org.mwg.task.Actions.printResult;

public class ImporterTest {

    @Test
    public void test() {
        Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        Task t = readLines("/Users/duke/dev/mwDB/plugins/importer/src/test/resources/smarthome/smarthome_1.T15.txt").foreach(printResult());
        t.execute(g, null);
    }

}
