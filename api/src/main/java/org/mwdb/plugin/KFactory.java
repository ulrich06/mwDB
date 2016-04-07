package org.mwdb.plugin;

import org.mwdb.KGraph;
import org.mwdb.KNode;

/**
 * KFactory plugin allows to propose alternative implementations for {@link KNode}.<br>
 * This specialization allows ot inject particular behavior into {@link KNode} such as machine learning, extrapolation function.
 */
public interface KFactory {

    /**
     * @return unique identifier for the factory plugin
     */
    long hash();

    /**
     * Create a new KNode
     *
     * @param world                 current world
     * @param time                  current time
     * @param id                    current node id
     * @param graph                 current graph
     * @param resolver              current resolver
     * @param actualWorld           current resolved world
     * @param actualSuperTime       current resolved super time
     * @param actualTime            current resolved time
     * @param currentWorldMagic     current world magic
     * @param currentSuperTimeMagic current super time magic
     * @param currentTimeMagic      current time magic
     * @return newly created KNode object
     */
    KNode create(long world, long time, long id, KGraph graph, KResolver resolver, long actualWorld, long actualSuperTime, long actualTime, long currentWorldMagic, long currentSuperTimeMagic, long currentTimeMagic);

}
