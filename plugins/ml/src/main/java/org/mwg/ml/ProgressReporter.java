package org.mwg.ml;

public interface ProgressReporter {
    void updateProgress(int value);
    boolean isCancelled();
    void updateInformation(String info);
}
