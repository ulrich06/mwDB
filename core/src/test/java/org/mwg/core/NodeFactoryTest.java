package org.mwg.core;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.*;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.plugin.NodeFactory;
import org.mwg.core.utility.Unsafe;

public class NodeFactoryTest implements NodeFactory {

    @Override
    public String name() {
        return "MathNode";
    }

    @Override
    public org.mwg.Node create(long world, long time, long id, org.mwg.Graph graph, long[] currentResolution) {
        return new org.mwg.plugin.AbstractNode(world, time, id, graph, currentResolution) {
            @Override
            public Object get(String propertyName) {
                if (propertyName.equals("hello")) {
                    return "world";
                }
                return super.get(propertyName);
            }

            @Override
            public void index(String indexName, org.mwg.Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {

            }

            @Override
            public void unindex(String indexName, org.mwg.Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {

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

    private void test(Graph graph) {
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node specializedNode = graph.newNode(0, 0, "MathNode");

                String hw = (String) specializedNode.get("hello");
                Assert.assertTrue(hw.equals("world"));

                specializedNode.free();
                graph.disconnect(null);
            }
        });
    }

}
