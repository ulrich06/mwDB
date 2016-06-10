package org.mwg;

public interface Query {

    Query parse(String flatQuery);

    Query add(String attributeName, Object value);

    Query setWorld(long initialWorld);

    long world();

    Query setTime(long initialTime);

    long time();

    Query setIndexName(String indexName);

    String indexName();

    long hash();

    long[] attributes();

    Object[] values();

}



