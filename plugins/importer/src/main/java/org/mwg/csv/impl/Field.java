package org.mwg.csv.impl;

import org.mwg.Type;
import org.mwg.csv.KField;

public class Field implements KField {

    public String name;
    public String rename;
    public byte type;
    public boolean ignored = false;
    public String ignoredValue = null;
    public KTransformFunction transform = null;

    @Override
    public String name() {
        return name;
    }

    @Override
    public KField rename(String newName) {
        rename = newName;
        return this;
    }

    @Override
    public KField isDouble() {
        type = Type.DOUBLE;
        return this;
    }

    @Override
    public KField isLong() {
        type = Type.LONG;
        return this;
    }

    @Override
    public KField isInt() {
        type = Type.INT;
        return this;
    }

    @Override
    public KField ignore() {
        ignored = true;
        return this;
    }

    @Override
    public KField ignoreIfValueEquals(String value) {
        ignoredValue = value;
        return this;
    }

    @Override
    public KField transformFunction(KTransformFunction transformFunction) {
        this.transform = transformFunction;
        return this;
    }
}
