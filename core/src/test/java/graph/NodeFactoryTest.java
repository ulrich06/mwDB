package graph;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.*;
import org.mwdb.chunk.offheap.OffHeapByteArray;
import org.mwdb.chunk.offheap.OffHeapDoubleArray;
import org.mwdb.chunk.offheap.OffHeapLongArray;
import org.mwdb.chunk.offheap.OffHeapStringArray;
import org.mwdb.plugin.KFactory;
import org.mwdb.task.NoopScheduler;
import org.mwdb.utility.Unsafe;

public class NodeFactoryTest implements KFactory {

    @Override
    public String name() {
        return "MathNode";
    }

    @Override
    public KNode create(long world, long time, long id, KGraph graph, long[] currentResolution) {
        return new Node(world, time, id, graph, currentResolution) {
            @Override
            public Object att(String attributeName) {
                if (attributeName.equals("hello")) {
                    return "world";
                }
                return super.att(attributeName);
            }
        };
    }

    @Test
    public void heapTest() {
        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withFactory(this).build());
    }

    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        test(GraphBuilder.builder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10_000).withAutoSave(20).withFactory(this).build());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(KGraph graph) {
        graph.connect(new KCallback<Boolean>() {
            @Override
            public void on(Boolean result) {
                KNode specializedNode = graph.newNode(0, 0, "MathNode");

                String hw = (String) specializedNode.att("hello");
                Assert.assertTrue(hw.equals("world"));

                specializedNode.free();
                graph.disconnect(null);
            }
        });
    }

}
