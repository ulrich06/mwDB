package org.mwg.task;

import org.mwg.Node;

/**
 * Task closure function to select nodes for next action
 */
public interface TaskFunctionSelect {

    /**
     * Selection function, called for each
     *
     * @param node node to select or not for next action
     * @return true to keep this node for the next action
     */
    boolean select(Node node);
}
