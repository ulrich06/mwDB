package org.mwg.ml;

import org.mwg.Node;

/**
 * Created by assaad on 04/05/16.
 */
public interface ClassificationNode extends Node{
    void learn(int expectedClass);
    int classify();
}
