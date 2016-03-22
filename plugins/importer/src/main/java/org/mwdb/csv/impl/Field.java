package org.mwdb.csv.impl;

import org.mwdb.KType;
import org.mwdb.csv.KField;

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
        rename = name;
        return this;
    }

    @Override
    public KField isDouble() {
        type = KType.DOUBLE;
        return this;
    }

    @Override
    public KField isLong() {
        type = KType.LONG;
        return this;
    }

    @Override
    public KField isInt() {
        type = KType.INT;
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
