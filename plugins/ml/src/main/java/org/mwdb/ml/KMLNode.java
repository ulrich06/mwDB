package org.mwdb.ml;

import org.mwdb.KCallback;
import org.mwdb.KNode;

/**
 * Created by assaad on 25/03/16.
 */
public interface KMLNode<A extends KNode> extends KNode  {

    void jump(long world, long time, KCallback<A> callback);

}
