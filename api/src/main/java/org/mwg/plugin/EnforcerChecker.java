package org.mwg.plugin;

public interface EnforcerChecker {

    void check(byte inputType, Object input) throws RuntimeException;

}
