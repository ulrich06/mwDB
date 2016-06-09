package leveldb;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.core.chunk.offheap.*;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.utility.Unsafe;

public class StorageTest {

    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        test("offheap ", GraphBuilder.builder().withStorage(new LevelDBStorage("data")).withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(100_000).saveEvery(10_000).build());
    }

    final int valuesToInsert = 300_000;
    final long timeOrigin = 1000;

    private void test(String name, Graph graph) {
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                final long before = System.currentTimeMillis();
                Node node = graph.newNode(0, 0);
                final DeferCounter counter = graph.counter(valuesToInsert);
                for (long i = 0; i < valuesToInsert; i++) {

                    if (i % 1_000_000 == 0) {
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

                counter.then(new Callback() {
                    @Override
                    public void on(Object result) {

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
    }
    
    @AfterClass
    public static void oneTimeTearDown() throws IOException {
        File data = new File("data");
        if(data.exists()){
        	System.out.println("Cleanup data directory");
        	Path directory = Paths.get("data");
        	   Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
        		   @Override
        		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        			   Files.delete(file);
        			   return FileVisitResult.CONTINUE;
        		   }

        		   @Override
        		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        			   Files.delete(dir);
        			   return FileVisitResult.CONTINUE;
        		   }

        	   });
        }
    }
    
}
