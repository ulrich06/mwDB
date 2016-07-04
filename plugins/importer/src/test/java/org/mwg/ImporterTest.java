package org.mwg;

import org.junit.Test;
import org.mwg.importer.ImporterPlugin;
import org.mwg.task.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mwg.importer.ImporterActions.readLines;
import static org.mwg.task.Actions.*;

public class ImporterTest {

    @Test
    public void test() {

        final SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy|HH:mm");
        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            Node newNode = g.newNode(0, 0);
            //final Task t = readLines("/Users/duke/dev/mwDB/plugins/importer/src/test/resources/smarthome/smarthome_1.T15.txt")
            final Task t = readLines("smarthome/smarthome_mini_1.T15.txt")
                    .foreach(
                            ifThen(ctx -> !ctx.resultAsString().startsWith("1:Date"),
                                    then(context -> {
                                        String[] line = context.resultAsString().split(" ");
                                        try {
                                            long time = dateFormat.parse(line[0] + "|" + line[1]).getTime();
                                            double value = Double.parseDouble(line[2]);
                                            newNode.jump(time, timedNode -> {
                                                timedNode.setProperty("value", Type.DOUBLE, value);
                                                context.setResult(timedNode);
                                            });
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            context.setResult(null);
                                        }
                                    }).printResult()
                            ));
            t.execute(g, null);
        });


    }

}
