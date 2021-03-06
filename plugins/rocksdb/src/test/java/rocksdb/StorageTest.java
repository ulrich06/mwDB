package rocksdb;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.Job;

import java.io.File;
import java.io.IOException;

public class StorageTest {

    @Test
    public void offHeapTest() throws IOException {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        test("offheap ", new GraphBuilder().withStorage(new RocksDBStorage("data")).withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(100000).saveEvery(10000).build());
    }

    final int valuesToInsert = 300000;
    final long timeOrigin = 1000;

    private void test(final String name, final Graph graph) throws IOException {
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final long before = System.currentTimeMillis();
                Node node = graph.newNode(0, 0);
                final DeferCounter counter = graph.newCounter(valuesToInsert);
                for (long i = 0; i < valuesToInsert; i++) {

                    if (i % 1000000 == 0) {
                        System.out.println("<insert til " + i + " in " + (System.currentTimeMillis() - before) / 1000 + "s");
                    }

                    final double value = i * 0.3;
                    final long time = timeOrigin + i;
                    graph.lookup(0, time, node.id(), new Callback<Node>() {
                        @Override
                        public void on(Node timedNode) {
                            timedNode.setProperty("value", Type.DOUBLE, value);
                            counter.count();
                            timedNode.free();//free the node, for cache management
                        }
                    });
                }
                node.free();

                counter.then(new Job() {
                    @Override
                    public void run() {

                        long beforeRead = System.currentTimeMillis();

                        System.out.println("<end insert phase>" + " " + (System.currentTimeMillis() - before) / 1000 + "s");
                        System.out.println(name + " result: " + (valuesToInsert / ((System.currentTimeMillis() - before) / 1000) / 1000) + "kv/s");

                        graph.disconnect(new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                System.out.println("Graph disconnected");

                                Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
                                Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
                                Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
                                Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
                            }
                        });
                    }
                });

            }
        });
        File data = new File("data");
        if (data.exists()) {
            delete(data);
        }
    }

    private static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            //directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();
            } else {
                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }
                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                }
            }

        } else {
            //if file, then delete it
            file.delete();
        }
    }


}
