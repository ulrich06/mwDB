package org.mwdb;

import org.junit.Test;
import org.mwdb.csv.CSVImporter;
import org.mwdb.csv.KNodeResolver;
import org.mwdb.task.NoopScheduler;

import java.io.File;
import java.util.Map;

public class CSVImporterTest {

    @Test
    public void singleNodeTest() {

        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                KNode house = graph.newNode(0, 0);

                CSVImporter importer = new CSVImporter();
                importer.setSeparator(";");
                importer.mapper().extractTime("{Date}|{Time}", "d/MM/yyyy|HH:mm:ss");
                importer.mapper().field("Global_active_power").isDouble().rename("Active_power");
                importer.mapper().field("Global_reactive_power").isDouble().rename("Reactive_power");
                importer.mapper().field("Voltage").isDouble();
                importer.mapper().field("Global_intensity").isDouble().rename("Intensity");
                importer.mapper().field("Sub_metering_1").isDouble();
                importer.mapper().field("Sub_metering_2").isDouble();
                importer.mapper().field("Sub_metering_3").isDouble();
                importer.mapper().globallyIgnoreIfValueEquals("?");

                try {
                    File toImport = new File(CSVImporter.class.getClassLoader().getResource("smart_meter_06_07.txt").getPath());
                    importer.importToNode(toImport, house, new KCallback<Boolean>() {
                        @Override
                        public void on(Boolean result) {
                            System.out.println("Import Finished");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void multiNode() {

        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                KNode house = graph.newNode(0, 0);
                house.attSet("ID", KType.STRING, "ABC123");
                graph.index("houses", house, new String[]{"ID"}, null);

                CSVImporter importer = new CSVImporter();
                importer.setSeparator(";");
                importer.mapper().extractTime("{Date}|{Time}", "d/MM/yyyy|HH:mm:ss");
                importer.mapper().field("Global_active_power").isDouble().rename("Active_power");
                importer.mapper().field("Global_reactive_power").isDouble().rename("Reactive_power");
                importer.mapper().field("Voltage").isDouble();
                importer.mapper().field("Global_intensity").isDouble().rename("Intensity");
                importer.mapper().field("Sub_metering_1").isDouble();
                importer.mapper().field("Sub_metering_2").isDouble();
                importer.mapper().field("Sub_metering_3").isDouble();
                importer.mapper().globallyIgnoreIfValueEquals("?");
                importer.mapper().nodeResolver(new KNodeResolver() {
                    @Override
                    public void resolve(KGraph graph, Map<String, Integer> headers, String[] values, long toResolveTime, KCallback<KNode> callback) {

                    }
                });

                try {
                    File toImport = new File(CSVImporter.class.getClassLoader().getResource("smart_meter_06_07.txt").getPath());
                    importer.importToNode(toImport, house, new KCallback<Boolean>() {
                        @Override
                        public void on(Boolean result) {
                            System.out.println("Import Finished");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

                graph.disconnect(null);
            }
        });
    }


}
