package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskFunctionSelectObject;

import static org.mwg.task.Actions.newTask;

/**
@ignore ts
 */
public class ActionSelectObjectTest extends AbstractActionTest{

    @Test
    public void testSelectOneObject() {
        initGraph();
        newTask().inject(new Integer(55))
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return false;
                    }
                })
                .then(context -> {
                    Assert.assertNull(context.result());
                })
                .execute(graph,null);

        newTask().inject(new Integer(55))
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return true;
                    }
                })
                .then(context -> {
                    Assert.assertNotNull(context.result());
                    Assert.assertEquals(55,context.result());
                })
                .execute(graph,null);

        newTask().inject(new Integer(55))
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return (Integer) object == 55;
                    }
                })
                .then(context -> {
                    Assert.assertNotNull(context.result());
                    Assert.assertEquals(55,context.result());
                })
                .execute(graph,null);


        removeGraph();
    }

    @Test
    public void testSelectObjectArray() {
        Integer[] toInject = new Integer[]{1,2,3,4,5,6,7,8,9,10};

        initGraph();
        newTask().inject(toInject)
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return false;
                    }
                })
                .then(context -> {
                    Object[] objects = (Object[]) context.result();
                    Assert.assertNotNull(context.result());
                    Assert.assertEquals(0,objects.length);
                })
                .execute(graph,null);

        newTask().inject(toInject)
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return true;
                    }
                })
                .then(context -> {
                    Assert.assertNotNull(context.result());
                    Assert.assertArrayEquals(toInject, (Object[]) context.result());
                })
                .execute(graph,null);

        newTask().inject(toInject)
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return (Integer) object % 2 == 0;
                    }
                })
                .then(context -> {
                    Assert.assertNotNull(context.result());
                    Integer[] evenNumbers = new Integer[]{2,4,6,8,10};
                    Assert.assertArrayEquals(evenNumbers, (Object[]) context.result());
                })
                .execute(graph,null);
        removeGraph();
    }

    @Test
    public void testSelectComplexObjectArray() {
        initGraph();

        final Object[] innerTab = new Object[3];
        innerTab[0] = 55;
        final Node n1 = graph.newNode(0,0);
        final Node n2 = graph.newNode(0,0);
        innerTab[1] = n1;
        innerTab[2] = n2;

        final Object[] toInject = new Object[3];
        final Node n3 = graph.newNode(0,0);
        toInject[0] = n3;
        toInject[1] = innerTab;
        toInject[2] = "A simple String";

        newTask().inject(toInject)
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return false;
                    }
                })
                .then(context -> {
                    Object[] objects = (Object[]) context.result();
                    Assert.assertNotNull(context.result());
                    Assert.assertEquals(0,objects.length);
                })
                .execute(graph,null);

        newTask().inject(toInject)
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return true;
                    }
                })
                .then(context -> {
                    compareArray(toInject, (Object[]) context.result());
                })
                .execute(graph,null);


        newTask().inject(toInject)
                .selectObject(new TaskFunctionSelectObject() {
                    @Override
                    public boolean select(Object object) {
                        return object instanceof AbstractNode;
                    }
                })
                .then(context -> {
                    Object[] result = new Object[2];
                    result[0] = n3;
                    Object[] insideResult = new Object[2];
                    insideResult[0] = n1;
                    insideResult[1] = n2;
                    result[1] = insideResult;

                    compareArray(result, (Object[]) context.result());

                })
                .execute(graph,null);

        n1.free();
        n2.free();
        n3.free();

        removeGraph();
    }

    private void compareArray(Object[] expected,Object[] result) {
        Assert.assertNotNull(result);

        Assert.assertEquals(expected.length,result.length);

        for(int i=0;i<expected.length;i++) {
            if(expected[i] instanceof Object[]) {
                compareArray((Object[])expected[i],(Object[])result[i]);
            } else if(expected[i] instanceof AbstractNode) {
                Assert.assertEquals(((Node)expected[i]).id(),((Node)result[i]).id());
            } else {
                Assert.assertEquals(expected[i],result[i]);
            }
        }
    }

}
