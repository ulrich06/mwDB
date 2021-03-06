package org.mwg.task;

import org.mwg.Node;

/**
 * Task closure function to transform nodes for next action
 */
@FunctionalInterface
public interface TaskFunctionMap {
    /**
     * Convert a node to any kind of object for next action
     *
     * @param node current node to convert
     * @return converted results, can be any object
     */
    Object map(Node node);

}
