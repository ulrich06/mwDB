package org.mwg.task;

import org.mwg.Node;

@FunctionalInterface
public interface TaskFunctionFlatMap {
    Object flatMap(Node[] nodes);
}
