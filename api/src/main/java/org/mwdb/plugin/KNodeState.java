package org.mwdb.plugin;

public interface KNodeState {

    void set(long index, int elemType, Object elem);

    Object get(long index);

    Object init(long index, int elemType);

}
