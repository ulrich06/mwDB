package org.mwdb.manager;

import org.mwdb.KNode;

public class NoopNodeTracker implements KNodeTracker {

    @Override
    public void monitor(KNode node) {
        //noop
    }

    @Override
    public void monitorAll(KNode[] objects) {
        //noop
    }
}
