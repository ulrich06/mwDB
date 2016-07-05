package org.mwg;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.importer.ImporterActions;
import org.mwg.importer.ImporterPlugin;
import org.mwg.task.Task;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.mwg.importer.ImporterActions.readFiles;
import static org.mwg.importer.ImporterActions.readLines;
import static org.mwg.task.Actions.*;

public class ImporterTest {

    @Test
    public void testReadLines() {
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
                                    })
                            ));
            t.execute(g, null);
        });
    }

    @Test
    public void testReadFilesStaticMethod() {
        File fileChecked = new File(this.getClass().getClassLoader().getResource("smarthome").getPath());
        final File[] subFiles = fileChecked.listFiles();

        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            final int[] nbFile = new int[1];
            Task t = readFiles("smarthome").foreach(then(context -> {
                File file = (File) context.result();
                Assert.assertEquals(subFiles[nbFile[0]].getAbsolutePath(), file.getAbsolutePath());
                nbFile[0]++;
                context.setResult(null);
            }));
            t.execute(g, null);

            Assert.assertEquals(subFiles.length, nbFile[0]);
        });
    }

    @Test
    public void testReadFilesActionWithTemplate() {
        File fileChecked = new File(this.getClass().getClassLoader().getResource("smarthome").getPath());
        final File[] subFiles = fileChecked.listFiles();

        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            final int[] nbFile = new int[1];
            Task t = inject("smarthome")
                    .asVar("fileName")
                    .action(ImporterActions.READFILES, "{{fileName}}")
                    .foreach(then(context -> {
                        File file = (File) context.result();
                        Assert.assertEquals(subFiles[nbFile[0]].getAbsolutePath(), file.getAbsolutePath());
                        nbFile[0]++;
                        context.setResult(null);
                    }));
            t.execute(g, null);

            Assert.assertEquals(subFiles.length, nbFile[0]);
        });
    }

    @Test
    public void testReadFilesOnFile() {
        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            final int[] nbFile = new int[1];
            Task t = readFiles("smarthome/readme.md").foreach(then(context -> {
                File file = (File) context.result();
                Assert.assertEquals("readme.md", file.getName());
                nbFile[0]++;
                context.setResult(null);
            }));
            t.execute(g, null);

            Assert.assertEquals(1, nbFile[0]);
        });
    }

    @Test
    public void testReadFileOnUnknowFile() {
        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            final int[] nbFile = new int[1];
            Task t = readFiles("nonexistent-file.txt").foreach(then(context -> {
                File file = (File) context.result();
                Assert.assertEquals("readme.md", file.getName());
                nbFile[0]++;
                context.setResult(null);
            }));

            boolean exceptionCaught = false;
            try {
                t.execute(g, null);
            } catch (RuntimeException exception) {
                exceptionCaught = true;
            }

            Assert.assertTrue(exceptionCaught);
        });
    }

    @Test
    public void testReadFileOnIncorrectVar() {
        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            final int[] nbFile = new int[1];
            Task t = inject(5478)
                    .asVar("fileName")
                    .action(ImporterActions.READFILES, "{{incorrectVarName}}");

            boolean exceptionCaught = false;
            try {
                t.execute(g, null);
            } catch (RuntimeException ex) {
                exceptionCaught = true;
            }
            Assert.assertTrue(exceptionCaught);

        });
    }


    @Test
    public void testV2() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy|HH:mm");
        final Graph g = new GraphBuilder().withPlugin(new ImporterPlugin()).build();
        g.connect(connectionResult -> {
            Node newNode = g.newNode(0, 0);
            final Task t = readLines("smarthome/smarthome_mini_1.T15.txt")
                    .foreach(
                            ifThen(ctx -> !ctx.resultAsString().startsWith("1:Date"),
                                    split(" ")
                                            .then(context -> {
                                                String[] line = context.resultAsStringArray();
                                                try {
                                                    context.setVariable("time", dateFormat.parse(line[0] + "|" + line[1]).getTime());
                                                    context.setVariable("value", Double.parseDouble(line[2]));
                                                    context.setResult(null);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                    context.setResult(null);
                                                }
                                            })
                                            .setTime("{{time}}")
                                            .lookup("0", "{{time}}", "" + newNode.id())
                                            .setProperty("value", Type.DOUBLE, "{{value}}")
                            ));
            t.execute(g, null);
        });
    }

}
