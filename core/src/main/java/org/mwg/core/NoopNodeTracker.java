package org.mwg.core;

public class NoopNodeTracker implements NodeTracker {

    @Override
    public void monitor(org.mwg.Node node) {
        //noop
    }

}
