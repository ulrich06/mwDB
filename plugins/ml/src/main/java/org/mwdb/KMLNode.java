package org.mwdb;

public interface KMLNode<A extends KNode> extends KNode {

    void jump(long world, long time, KCallback<A> callback);

}
