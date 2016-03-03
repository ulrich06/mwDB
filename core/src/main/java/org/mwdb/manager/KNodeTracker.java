package org.mwdb.manager;

import org.mwdb.KNode;

public interface KNodeTracker {

    void monitor(KNode node);

    void monitorAll(KNode[] objects);

}
