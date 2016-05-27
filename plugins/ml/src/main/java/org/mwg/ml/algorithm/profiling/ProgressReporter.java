package org.mwg.ml.algorithm.profiling;

/**
 * Created by assaad on 24/05/16.
 */
public interface ProgressReporter {
    void updateProgress(int value);
    boolean isCancelled();
}
