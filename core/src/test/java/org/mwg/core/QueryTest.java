package org.mwg.core;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.utility.DataHasher;
import org.mwg.plugin.Job;
import org.mwg.plugin.NodeState;
import org.mwg.plugin.Resolver;

public class QueryTest implements Resolver {

    @Test
    public void test() {
        CoreQuery query = new CoreQuery(this);
        query.add("name", "Hello");
        Assert.assertEquals(query.hash(), 3074775135214424L);

        CoreQuery query2 = new CoreQuery(this);
        query2.add("id", "Hello");
        Assert.assertEquals(query2.hash(), 1115038540081133L);

        CoreQuery query3 = new CoreQuery(this);
        query3.add("id", "Hello2");
        Assert.assertEquals(query3.hash(), 8950810462547208L);

        Assert.assertTrue(query3.hash() != query.hash());
        Assert.assertTrue(query2.hash() != query.hash());
        Assert.assertTrue(query3.hash() != query2.hash());

        CoreQuery query4 = new CoreQuery(this);
        query4.add("id", "Hello2");
        Assert.assertEquals(query4.hash(), 8950810462547208L);
        Assert.assertEquals(query3.hash(), query4.hash());

    }


    @Override
    public void init(Graph graph) {

    }

    @Override
    public void initNode(Node node, long typeCode) {

    }

    @Override
    public long markNodeAndGetType(Node node) {
        return 0;
    }

    @Override
    public void initWorld(long parentWorld, long childWorld) {

    }

    @Override
    public void freeNode(Node node) {

    }
    
    @Override
    public <A extends Node> void lookup(long world, long time, long id, Callback<A> callback) {

    }

    @Override
    public NodeState resolveState(Node node, boolean allowDephasing) {
        return null;
    }

    @Override
    public NodeState newState(Node node, long world, long time) {
        return null;
    }

    @Override
    public void resolveTimepoints(Node node, long beginningOfSearch, long endOfSearch, Callback<long[]> callback) {

    }

    @Override
    public long stringToHash(String name, boolean insertIfNotExists) {
        return DataHasher.hash(name);
    }

    @Override
    public String hashToString(long key) {
        return null;
    }
}
