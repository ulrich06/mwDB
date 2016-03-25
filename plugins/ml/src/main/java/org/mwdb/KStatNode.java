package org.mwdb;

public interface KStatNode extends KNode {

    void learn(double value);

    double avg();

    double min();

    double max();

}