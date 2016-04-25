package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;

public class QueryTest {

    @Test
    public void test() {
        Query query = new Query();
        query.add(2, "Hello");
        query.compute();
        Assert.assertTrue(query.hash() == 126867952L);

        Query query2 = new Query();
        query2.add(3, "Hello");
        query2.compute();
        Assert.assertTrue(query2.hash() == 155497103L);

        Query query3 = new Query();
        query3.add(3, "Hello2");
        query3.compute();
        Assert.assertTrue(query3.hash() == 4820410243L);

        Assert.assertTrue(query3.hash() != query.hash());
        Assert.assertTrue(query2.hash() != query.hash());
        Assert.assertTrue(query3.hash() != query2.hash());

        Query query4 = new Query();
        query4.add(3, "Hello2");
        query4.compute();
        Assert.assertTrue(query4.hash() == 4820410243L);
        Assert.assertTrue(query3.hash() == query4.hash());

    }

}
