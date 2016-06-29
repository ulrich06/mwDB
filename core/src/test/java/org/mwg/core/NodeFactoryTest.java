package org.mwg.core;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;
import org.mwg.core.chunk.offheap.OffHeapByteArray;
import org.mwg.core.chunk.offheap.OffHeapDoubleArray;
import org.mwg.core.chunk.offheap.OffHeapLongArray;
import org.mwg.core.chunk.offheap.OffHeapStringArray;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.core.utility.Unsafe;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.AbstractPlugin;
import org.mwg.plugin.NodeFactory;

public class NodeFactoryTest {

    private static final String NAME = "HelloWorldNode";

    interface ExNode extends org.mwg.Node {
        String sayHello();
    }

    class ExNodeImpl extends AbstractNode implements ExNode {

        public ExNodeImpl(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
            super(p_world, p_time, p_id, p_graph, currentResolution);
        }

        @Override
        public Object get(String propertyName) {
            if (propertyName.equals("hello")) {
                return "world";
            }
            return super.get(propertyName);
        }

        @Override
        public String sayHello() {
            return "HelloWorld";
        }
    }

    @Test
    public void heapTest() {
        test(new GraphBuilder().withScheduler(new NoopScheduler()).withPlugin(new AbstractPlugin().declareNodeType(NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new ExNodeImpl(world, time, id, graph, initialResolution);
            }
        })).build());
    }

    /**
     * @ignore ts
     */
    @Test
    public void offHeapTest() {
        OffHeapByteArray.alloc_counter = 0;
        OffHeapDoubleArray.alloc_counter = 0;
        OffHeapLongArray.alloc_counter = 0;
        OffHeapStringArray.alloc_counter = 0;

        Unsafe.DEBUG_MODE = true;

        test(new GraphBuilder().withScheduler(new NoopScheduler()).withOffHeapMemory().withMemorySize(10000).saveEvery(20).withPlugin(new AbstractPlugin().declareNodeType(NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new ExNodeImpl(world, time, id, graph, initialResolution);
            }
        })).build());

        Assert.assertTrue(OffHeapByteArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapDoubleArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapLongArray.alloc_counter == 0);
        Assert.assertTrue(OffHeapStringArray.alloc_counter == 0);
    }

    private void test(final Graph graph) {

        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node specializedNode = graph.newTypedNode(0, 0, NAME);

                String hw = (String) specializedNode.get("hello");
                Assert.assertTrue(hw.equals("world"));

                Node parent = graph.newNode(0, 0);
                parent.add("children", specializedNode);
                parent.rel("children", new Callback<Node[]>() {
                    @Override
                    public void on(Node[] result) {
                        Assert.assertEquals("HelloWorld", ((ExNode) result[0]).sayHello());
                    }
                });

                specializedNode.free();
                graph.disconnect(null);
            }
        });
    }

}
