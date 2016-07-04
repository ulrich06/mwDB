package org.mwg.plugin;

public abstract class AbstractIterable {

    public abstract Object next();

    public abstract void close();

    public abstract int estimate();

}
