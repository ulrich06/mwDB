package org.mwdb;

import org.junit.Test;
import org.mwdb.csv.CSV;
import org.mwdb.csv.DefaultTransform;
import org.mwdb.task.NoopScheduler;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class CSVTest {

    @Test
    public void test() {


        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                CSV importer = new CSV();
                importer.setSeparator(";");
                DefaultTransform transform = new DefaultTransform() {
                    private SimpleDateFormat df = new SimpleDateFormat("d/MM/yyyy|HH:mm:ss");

                    @Override
                    protected long extractTime(String[] headers, String[] lines) {
                        try {
                            return df.parse(lines[0] + "|" + lines[1]).getTime();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                };
                transform
                        .ignore("Date")
                        .ignore("Time")
                        .ignoreValue("?");

                transform.asDouble("Global_active_power");
                transform.asDouble("Global_reactive_power");
                transform.asDouble("Voltage");
                transform.asDouble("Global_intensity");
                transform.asDouble("Sub_metering_1");
                transform.asDouble("Sub_metering_2");
                transform.asDouble("Sub_metering_3");
                importer.usingTransform(transform);


                KNode house = graph.newNode(0, 0);
                try {
                    File toImport = new File(this.getClass().getClassLoader().getResource("household_power_consumption.txt").getPath());
                    importer.singleNodeImport(toImport, house, new KCallback<Boolean>() {
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
