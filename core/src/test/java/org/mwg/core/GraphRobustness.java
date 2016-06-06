package org.mwg.core;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mwg.*;

import java.util.concurrent.CountDownLatch;

public class GraphRobustness {

    private Graph _graph;

    @Before
    public void initGraph() {
        _graph = GraphBuilder.builder().build();
        CountDownLatch latch = new CountDownLatch(1);
        _graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void deleteGraph() {
        _graph.disconnect(null);
    }

    @Test
    public void robustnessNewTypedNode() {
        boolean exceptionCaught = false;
        try {
            Node n = _graph.newTypedNode(0,0,null);
            n.free();
        } catch (NullPointerException e) {
            Assert.assertEquals("nodeType should not be null",e.getMessage());
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessCloneNode() {
        boolean exceptionCaught = false;
        try {
            Node n = _graph.cloneNode(null);
            n.free();
        } catch (NullPointerException e) {
            Assert.assertEquals("origin node should not be null",e.getMessage());
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessLookup() {
        boolean exceptionCaught = false;
        try {
            Node n1 = _graph.newNode(0,0);
            _graph.lookup(0,0,n1.id(),null);
            n1.free();
        } catch (Exception e) {
            exceptionCaught = true;
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }
        Assert.assertEquals(false,exceptionCaught);
    }

    @Test
    public void robustnessSave() {
        boolean exceptionCaught = false;
        try {
            Node n1 = _graph.newNode(0,0);
            _graph.save(null);
            n1.free();
        } catch (Exception e) {
            exceptionCaught = true;
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }
        Assert.assertEquals(false,exceptionCaught);
    }

    @Test
    public void robustnessConnect() {
        boolean[] exceptionCaught = new boolean[1];
        CountDownLatch latch = new CountDownLatch(1);
        _graph.disconnect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                try {
                    _graph.connect(null);
                    latch.countDown();
                } catch (Exception e) {
                    Assert.fail("Unexpected exception thrown: " + e.getMessage());
                    exceptionCaught[0] = true;
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(false, exceptionCaught[0]);
    }

    @Test
    public void robustnessDisconnect() {
        boolean exceptionCaught = false;
        try {
            _graph.disconnect(null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
            exceptionCaught = true;
        }

        Assert.assertEquals(false, exceptionCaught);
    }

    @Test
    public void robustnessIndex() {
        Node node = _graph.newNode(0,0);
        node.set("name","n1");

        //indexName null
        try {
            _graph.index(null, node, "name", new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //nodeToIndex null
        try {
            _graph.index("indexName", null, "name", new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("toIndexNode should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //flatKeyAttributes null
        try {
            _graph.index("name", node, null, new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {

                }
            });
        } catch(NullPointerException e) {
            Assert.assertEquals("flatKeyAttributes should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //callback null
        try{
            _graph.index("name",node,"name",null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        node.free();
    }

    @Test
    public void robustnessUnindex() {
        Node node = _graph.newNode(0,0);
        node.set("name","n1");

        //indexName null
        try {
            _graph.unindex(null, node, "name", new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //nodeToIndex null
        try {
            _graph.unindex("indexName", null, "name", new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("toIndexNode should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //flatKeyAttributes null
        try {
            _graph.unindex("name", node, null, new Callback<Boolean>() {
                @Override
                public void on(Boolean result) {

                }
            });
        } catch(NullPointerException e) {
            Assert.assertEquals("flatKeyAttributes should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //callback null
        try{
            _graph.unindex("name",node,"name",null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        node.free();
    }

    @Test
    public void robustnessFind() {
        Node node = _graph.newNode(0,0);
        node.set("name","root");
        _graph.index("indexName",node,"name=root",null);

        //indexName
        try{
            _graph.find(0, 0, null, "name=root", new Callback<Node[]>() {
                @Override
                public void on(Node[] result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }


        //query
        try {
            _graph.find(0, 0, "indexName", null, new Callback<Node[]>() {
                @Override
                public void on(Node[] result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("query should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        //callback
        try {
            _graph.find(0, 0, "indexName", "name=root", null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        node.free();
    }

    @Test
    public void robustnessFindQuery() {
        try {
            _graph.findQuery(null, new Callback<Node[]>() {
                @Override
                public void on(Node[] result) {

                }
            });
        } catch (NullPointerException npe) {
            Assert.assertEquals("query should not be null",npe.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        try {
            Query q = _graph.newQuery();
            q.setWorld(0);
            q.setTime(0);
            q.setIndexName("indexName");
            _graph.findQuery(q,null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void robustnessAll() {
        Node node = _graph.newNode(0,0);
        node.set("name","root");
        _graph.index("indexName",node,"name=root",null);

        //indexName
        try{
            _graph.all(0, 0, null, new Callback<Node[]>() {
                @Override
                public void on(Node[] result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }


        //callback
        try {
            _graph.all(0, 0, "indexName", null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        node.free();
    }

    @Test
    public void robustnessNamedIndex() {
        Node node = _graph.newNode(0,0);
        node.set("name","root");
        _graph.index("indexName",node,"name=root",null);

        //indexName
        try{
            _graph.namedIndex(0, 0, null, new Callback<Node>() {
                @Override
                public void on(Node result) {

                }
            });
        } catch (NullPointerException e) {
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }


        //callback
        try {
            _graph.namedIndex(0, 0, "indexName", null);
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown: " + e.getMessage());
        }

        node.free();
    }


}
