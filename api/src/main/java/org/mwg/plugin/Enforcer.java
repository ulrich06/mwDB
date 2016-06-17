package org.mwg.plugin;

import org.mwg.Type;

import java.util.HashMap;
import java.util.Map;

public class Enforcer {

    private final Map<String, EnforcerChecker> checkers = new HashMap<String, EnforcerChecker>();

    public Enforcer asBool(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                if (input != null && inputType != Type.BOOL) {
                    throw new RuntimeException("Property " + propertyName + " should be Boolean value, currently " + input);
                }
            }
        });
    }

    public Enforcer asString(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                if (input != null && inputType != Type.STRING) {
                    throw new RuntimeException("Property " + propertyName + " should be String value, currently " + input);
                }
            }
        });
    }

    public Enforcer asLong(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                if (input != null && inputType != Type.LONG && inputType != Type.INT) {
                    throw new RuntimeException("Property " + propertyName + " should be long value, currently " + input);
                }
            }
        });
    }

    public Enforcer asLongWithin(String propertyName, long min, long max) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                long inputDouble = (long) input;
                if (input != null && ((inputType != Type.LONG && inputType != Type.INT) || inputDouble < min || inputDouble > max)) {
                    throw new RuntimeException("Property " + propertyName + " should be long value [" + min + "," + max + "], currently " + input);
                }
            }
        });
    }

    public Enforcer asDouble(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                if (input != null && (inputType != Type.DOUBLE && inputType != Type.INT && inputType != Type.LONG)) {
                    throw new RuntimeException("Property " + propertyName + " should be double value, currently " + input);
                }
            }
        });
    }

    public Enforcer asDoubleWithin(String propertyName, double min, double max) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                double inputDouble = (double) input;
                if (input != null && ((inputType != Type.DOUBLE && inputType != Type.INT && inputType != Type.LONG) || inputDouble < min || inputDouble > max)) {
                    throw new RuntimeException("Property " + propertyName + " should be double value [" + min + "," + max + "], currently " + input);
                }
            }
        });
    }

    public Enforcer asInt(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                if (input != null && inputType != Type.INT && inputType != Type.LONG) {
                    throw new RuntimeException("Property " + propertyName + " should be integer value, currently " + input);
                }
            }
        });
    }

    public Enforcer asIntWithin(String propertyName, int min, int max) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                int inputInt = (int) input;
                if (input != null && ((inputType != Type.INT && inputType != Type.LONG) || inputInt < min || inputInt > max)) {
                    throw new RuntimeException("Property " + propertyName + " should be integer value [" + min + "," + max + "], currently " + input);
                }
            }
        });
    }

    public Enforcer asDoubleArray(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                if (input != null && inputType != Type.DOUBLE_ARRAY) {
                    throw new RuntimeException("Property " + propertyName + " should be doubleArray value, currently " + input);
                }
            }
        });
    }

    public Enforcer asPositiveInt(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                int inputInt = (int) input;
                if (input != null && inputType != Type.INT && inputInt<=0) {
                    throw new RuntimeException("Property " + propertyName + " should be a positive integer, currently " + input);
                }
            }
        });
    }

    public Enforcer asPositiveDouble(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                double inputDouble = (double) input;
                if (input != null && ((inputType != Type.DOUBLE && inputType != Type.INT && inputType != Type.LONG) || inputDouble<=0)) {
                    throw new RuntimeException("Property " + propertyName + " should be a positive double, currently " + input);
                }
            }
        });
    }

    public Enforcer asPositiveLong(String propertyName) {
        return declare(propertyName, new EnforcerChecker() {
            @Override
            public void check(byte inputType, Object input) throws RuntimeException {
                long inputLong=(long) input;
                if (input != null && ((inputType != Type.LONG && inputType != Type.INT)  || inputLong<=0)) {
                    throw new RuntimeException("Property " + propertyName + " should be a positive long, currently " + input);
                }
            }
        });
    }

    public Enforcer declare(String propertyName, EnforcerChecker checker) {
        this.checkers.put(propertyName, checker);
        return this;
    }

    public void check(String propertyName, byte propertyType, Object propertyValue) {
        EnforcerChecker checker = checkers.get(propertyName);
        if (checker != null) {
            checker.check(propertyType, propertyValue);
        }
    }

}
