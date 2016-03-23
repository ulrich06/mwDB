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
                    File toImport = new File(CSVImporter.class.getClassLoader().getResource("smartmeters/smart_meter_06_07.txt").getPath());
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
    public void singleNodeTest2() {

        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                KNode house = graph.newNode(0, 0);
                house.attSet("ID", KType.STRING, "ABC123");
                graph.index("houses", house, new String[]{"ID"}, null);

                CSVImporter importer = new CSVImporter();
                importer.setSeparator(" ");
                importer.mapper().extractTime("{1:Date}|{2:Time}", "d/MM/yyyy|HH:mm");
                importer.mapper().field("3:Temperature_Comedor_Sensor").isDouble().rename("Temperature_Dinning_Sensor");
                importer.mapper().field("4:Temperature_Habitacion_Sensor").isDouble().rename("Temperature_Room_Sensor");
                importer.mapper().field("6:CO2_Comedor_Sensor").isDouble().rename("CO2_Dinning_Sensor");
                importer.mapper().field("7:CO2_Habitacion_Sensor").isDouble().rename("CO2_Room_Sensor");

                try {
                    File toImport = new File(CSVImporter.class.getClassLoader().getResource("smarthome/smarthome_1.T15.txt").getPath());
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
