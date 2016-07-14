package org.mwg.utility;


public interface ResultSet {
    GenericIterator iterator();
    Object get();
    ResultSet clone();
    int estimate();
    boolean isArray();
    void clean();
}
