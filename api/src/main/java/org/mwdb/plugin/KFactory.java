package org.mwdb.plugin;

import org.mwdb.KGraph;
import org.mwdb.KNode;

/**
 * KFactory plugin allows to propose alternative implementations for {@link KNode}.<br>
 * This specialization allows ot inject particular behavior into {@link KNode} such as machine learning, extrapolation function.
 */
public interface KFactory {

    /**
     * @return unique identifier for the factory plugin (usually the fully qualified name of the extended KNode)
     */
    String name();

    /**
     * Create a new KNode
     *
     * @param world             current world
     * @param time              current time
     * @param id                current node id
     * @param graph             current graph
     * @param initialResolution current resolved world/superTime/time and associated magics
     * @return newly created KNode object
     */
    KNode create(long world, long time, long id, KGraph graph, long[] initialResolution);

}
