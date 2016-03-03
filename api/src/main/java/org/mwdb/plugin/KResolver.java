package org.mwdb.plugin;

import org.mwdb.KCallback;
import org.mwdb.KNode;

public interface KResolver {

    void initNode(KNode node);

    KTask lookup(long world, long time, long id, KCallback<KNode> callback);

}
