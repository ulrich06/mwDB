package org.mwg.core.task;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mwg.*;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionConditional;


public class RobustnessTests {
    private Graph _graph;

    @Before
    public void initGraph() {
        _graph = new GraphBuilder().build();
        _graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                Node root = _graph.newNode(0, 0);
                root.set("name", "root");

                Node n1 = _graph.newNode(0, 0);
                n1.set("name", "n1");

                Node n2 = _graph.newNode(0, 0);
                n2.set("name", "n2");

                Node n3 = _graph.newNode(0, 0);
                n3.set("name", "n3");

                root.add("child", n1);
                root.add("child", n2);
                root.add("child", n3);

                _graph.index("rootIndex", root, "name", new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                    }
                });
            }
        });

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
        catch (RuntimeException npe){
            exceptionCaught = true;
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
        } catch (RuntimeException npe) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessFrom() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().from(null).execute();
        } catch (RuntimeException npe) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //query null
        exceptionCaught = false;
        try {
            _graph.newTask().fromIndex("rootIndex",null).execute();
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessSelectWithout() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().selectWithout("child",null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessSelect() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().select(null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    //@Test
    public void robustnessSelectWhere() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().selectWhere(null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessTraverseIndex() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().traverseIndex(null,"name=root").execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessWait() {
        boolean exceptionCaught = false;
        try {
            _graph.newTask().executeSubTask(null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //subTask null
        exceptionCaught = false;
        try {
            _graph.newTask().ifThen(new TaskFunctionConditional() {
                @Override
                public boolean eval(TaskContext context) {
                    return true;
                }
            }, null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
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
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //subTask null
        exceptionCaught = false;
        try {
            _graph.newTask().action("",null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessNodeSet(){
        //propertyName
        boolean exceptionCaught = false;
        try {
            _graph.newTask().set(null,"").execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //propertyValue
        exceptionCaught = false;
        try {
            _graph.newTask().set("",null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessNodeSetProperty(){
        //propertyName
        boolean exceptionCaught = false;
        try {
            _graph.newTask().setProperty(null,Type.STRING,"").execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //propertyValue
        exceptionCaught = false;
        try {
            _graph.newTask().setProperty("",Type.STRING,null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessNodeRemoveProperty(){
        boolean exceptionCaught = false;
        try {
            _graph.newTask().removeProperty(null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessNodeAdd(){
        //relationName
        boolean exceptionCaught = false;
        try {
            _graph.newTask().from(_graph.newNode(0,0)).asVar("x").add(null,"x").execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //relatedNode
        exceptionCaught = false;
        try {
            _graph.newTask().add("",null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }

    @Test
    public void robustnessNodeRemove(){
        //relationName
        boolean exceptionCaught = false;
        try {
            _graph.newTask().from(_graph.newNode(0,0)).asVar("x").remove(null,"x").execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);

        //relatedNode
        exceptionCaught = false;
        try {
            _graph.newTask().remove("",null).execute();
        } catch (RuntimeException e) {
            exceptionCaught = true;
        } catch (Exception e) {
            Assert.fail("Unexpected exception thrown");
        }
        Assert.assertEquals(true,exceptionCaught);
    }





}
