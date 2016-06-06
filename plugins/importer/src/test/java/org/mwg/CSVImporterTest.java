package org.mwg;

import org.junit.Test;
import org.mwg.csv.CSVImporter;
import org.mwg.csv.KField;
import org.mwg.csv.KNodeResolver;
import org.mwg.core.scheduler.NoopScheduler;

import java.io.File;
import java.util.Map;

public class CSVImporterTest {

    @Test
    public void singleNodeTest() {

        Graph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).withAutoSave(1000).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                Node house = graph.newNode(0, 0);

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
                    importer.importToNode(toImport, house, new Callback<Boolean>() {
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

        Graph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).withAutoSave(1000).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                Node house = graph.newNode(0, 0);
                house.setProperty("ID", Type.STRING, "ABC123");
                graph.index("houses", house, "ID", null);

                CSVImporter importer = new CSVImporter();
                importer.setSeparator(" ");
                importer.mapper().extractTime("{1:Date}|{2:Time}", "d/MM/yyyy|HH:mm");
                importer.mapper().field("3:Temperature_Comedor_Sensor").isDouble().rename("Temperature_Dinning_Sensor");
                importer.mapper().field("4:Temperature_Habitacion_Sensor").isDouble().rename("Temperature_Room_Sensor");
                importer.mapper().field("6:CO2_Comedor_Sensor").isDouble().rename("CO2_Dinning_Sensor");
                importer.mapper().field("7:CO2_Habitacion_Sensor").isDouble().rename("CO2_Room_Sensor");

                try {
                    File toImport = new File(CSVImporter.class.getClassLoader().getResource("smarthome/smarthome_1.T15.txt").getPath());
                    importer.importToNode(toImport, house, new Callback<Boolean>() {
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

        Graph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                //create an empty
                Node house = graph.newNode(0, 0);
                house.setProperty("ID", Type.STRING, "ABC123");
                graph.index("houses", house, "ID", null);

                CSVImporter importer = new CSVImporter();
                //  importer.setVerbose();
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
                    public void resolve(Graph graph, Map<String, Integer> headers, String[] values, long toResolveworld, long toResolveTime, Callback<Node> callback) {
                        final String roomName = values[headers.get("Place")];
                        final String locationName = values[headers.get("Location")];
                        house.find("sensors", "room=" + roomName + ",sensor=" + locationName, previouslyDefinedSensors -> {
                            if (previouslyDefinedSensors.length == 1) {
                                callback.on(previouslyDefinedSensors[0]);
                            } else {
                                Node sensorNode = graph.newNode(toResolveworld, toResolveTime);
                                sensorNode.setProperty("room", Type.STRING, roomName);
                                sensorNode.setProperty("sensor", Type.STRING, locationName);
                                house.index("rooms", sensorNode, "room,sensor", new Callback<Boolean>() {
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
                    importer.importToGraph(toImport, graph, 0, new Callback<Boolean>() {
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
