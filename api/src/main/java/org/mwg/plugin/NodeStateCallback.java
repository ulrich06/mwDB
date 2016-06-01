package org.mwg.plugin;

@FunctionalInterface
public interface NodeStateCallback {

    void on(long attributeKey, int elemType, Object elem);

}
