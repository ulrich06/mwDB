package org.mwg.core.task;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.GraphBuilder;
import org.mwg.Node;

import java.util.concurrent.CountDownLatch;

public class RobustnessTests {
    Graph _graph;

    @Before
    public void initGraph() {
        CountDownLatch latch = new CountDownLatch(1);

        _graph = GraphBuilder.builder().build();
        _graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node root = _graph.newNode(0,0);
                root.set("name","root");

                Node n1 = _graph.newNode(0,0);
                n1.set("name","n1");

                Node n2 = _graph.newNode(0,0);
                n2.set("name","n2");

                Node n3 = _graph.newNode(0,0);
                n3.set("name","n3");

                root.add("child",n1);
                root.add("child",n2);
                root.add("child",n3);

                _graph.index("rootIndex", root, "name", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        latch.countDown();
                    }
                });
        }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void deleteGrap() {
        _graph.find(0, 0, "rootIndex", "name=root", new Callback<Node[]>() {
            @Override
            public void on(Node[] result) {
                for(Node r : result) {
                    final Node rr = r;
                    r.rel("child", new Callback<Node[]>() {
                        @Override
                        public void on(Node[] result) {
                            for (Node n : result) {
                                n.free();
                            }
                            rr.free();
                        }
                    });
                }
            }
        });
    }

    @Test
    public void robustnessAsVar() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().from(1).asVar(null).execute();
        }
        catch (NullPointerException npe){
            exceptionCaught = true;
            Assert.assertEquals("variableName should not be null",npe.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessFromVar() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().fromVar(null).execute();
        } catch (NullPointerException npe) {
            exceptionCaught = true;
            Assert.assertEquals("variableName should not be null",npe.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessFrom() {
        boolean exceptionCaught = true;
        try {
            _graph.newTask().from(null).execute();
        } catch (NullPointerException npe) {
            exceptionCaught = true;
            Assert.assertEquals("variableName should not be null",npe.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessFromIndex() {
        //indexName null
        boolean exceptionCaught = false;
        try {
            _graph.newTask().fromIndex(null,"name=root").execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //query null
        exceptionCaught = false;
        try {
            _graph.newTask().fromIndex("rootIndex",null).execute();
        } catch (NullPointerException e) {
            Assert.assertEquals("query should not be null",e.getMessage());
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessFromIndexAll() {
        //indexName null
        boolean exceptionCaught = false;
        try {
            _graph.newTask().fromIndexAll(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessSelectWith() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().selectWith("child",null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("pattern should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
            e.printStackTrace();
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessSelectWithout() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().selectWithout("child",null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("pattern should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
            e.printStackTrace();
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessSelect() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().select(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("filter should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
            e.printStackTrace();
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    //@Test
    public void robustnessSelectWhere() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().selectWhere(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("filter should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
            e.printStackTrace();
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessTraverseIndex() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().traverseIndex(null,"name=root").execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessTraverseIndexAll() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().traverseIndexAll(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("indexName should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessMap() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().map(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("mapFunction should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessForeach() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().foreach(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("subTask should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessForeachPar() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().foreachPar(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("subTask should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessForeachThen() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().foreachThen(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("action should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessWait() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().wait(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("subTask should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessIfThen() {
        //condition null
        boolean exceptionCaught = false;
        try {
            _graph.newTask().ifThen(null,_graph.newTask()).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("condition should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //subTask null
        exceptionCaught = false;
        try {
            _graph.newTask().ifThen(context -> true,null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("subTask should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessThen() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().then(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("action should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessThenAsync() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().thenAsync(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("action should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessParse() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().parse(null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("flat should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessAction() {
        //condition null
        boolean exceptionCaught = false;
        try {
            _graph.newTask().action(null,"").execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("name should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //subTask null
        exceptionCaught = false;
        try {
            _graph.newTask().action("",null).execute();
        } catch (NullPointerException e) {
            exceptionCaught = true;
            Assert.assertEquals("flatParams should not be null",e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }





}
