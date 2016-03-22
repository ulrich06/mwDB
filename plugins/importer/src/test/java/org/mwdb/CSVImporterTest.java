package org.mwdb;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.task.NoopScheduler;

import java.io.File;
import java.net.URISyntaxException;

public class CSVImporterTest {

    @Test
    public void test() {


        KGraph graph = GraphBuilder.builder().withScheduler(new NoopScheduler()).buildGraph();
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {

                CSVImporter importer = new CSVImporter();
                importer.setSeparator(";");

                KNode house = graph.newNode(0, 0);
                try {
                    File toImport = new File(this.getClass().getClassLoader().getResource("household_power_consumption.txt").getPath());
                    importer.singleNodeImport(toImport, house, new KCallback<Boolean>() {
                        @Override
                        public void on(Boolean result) {

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
