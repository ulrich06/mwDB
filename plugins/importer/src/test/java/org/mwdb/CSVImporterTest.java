package org.mwdb;

import org.junit.Test;
import org.mwdb.csv.CSVImporter;
import org.mwdb.csv.KField;
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

    @Test
    public void multiNodeTest2() {

        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                KNode house = graph.newNode(0, 0);
                house.attSet("ID", KType.STRING, "ABC123");
                graph.index("houses", house, new String[]{"ID"}, null);

                CSVImporter importer = new CSVImporter();
                importer.setVerbose();
                importer.setSeparator("\t");
                importer.mapper().extractTime("{Start time          }", "yyyy-MM-d HH:mm:ss");
                importer.mapper().field("Type").isInt().rename("active").transformFunction(new KField.KTransformFunction() {
                    @Override
                    public Object transform(String value) {
                        return 1;
                    }
                });
                importer.mapper().nodeResolver(new KNodeResolver() {
                    @Override
                    public void resolve(KGraph graph, Map<String, Integer> headers, String[] values, long toResolveworld, long toResolveTime, KCallback<KNode> callback) {
                        final String roomName = values[headers.get("Place")];
                        final String locationName = values[headers.get("Location")];
                        house.find("sensors", "room=" + roomName + ",sensor=" + locationName, previouslyDefinedSensors -> {
                            if (previouslyDefinedSensors.length == 1) {
                                callback.on(previouslyDefinedSensors[0]);
                            } else {
                                KNode sensorNode = graph.newNode(toResolveworld, toResolveTime);
                                sensorNode.attSet("room", KType.STRING, roomName);
                                sensorNode.attSet("sensor", KType.STRING, locationName);
                                house.index("rooms", sensorNode, new String[]{"room", "sensor"}, new KCallback<Boolean>() {
                                    @Override
                                    public void on(Boolean result) {
                                        if (result) {
                                            callback.on(sensorNode);
                                        } else {
                                            callback.on(null);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });


                try {
                    File toImport = new File(CSVImporter.class.getClassLoader().getResource("activity/OrdonezB_Sensors.txt").getPath());
                    importer.importToGraph(toImport, graph,0, new KCallback<Boolean>() {
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
