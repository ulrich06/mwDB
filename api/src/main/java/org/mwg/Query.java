package org.mwg;

public interface Query {

    Query parseString(String flatQuery);

    Query add(String attributeName, String value);

    Query setWorld(long initialWorld);

    long world();

    Query setTime(long initialTime);

    long time();

    Query setIndexName(String indexName);

    String indexName();

    long hash();

    long[] attributes();

    String[] values();

}



