package org.mwg.utility;


public interface GenericIterator {
    Object next();
    boolean hasNext();
    void close();
}
