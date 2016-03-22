package org.mwdb.csv.impl;

import org.mwdb.csv.KField;
import org.mwdb.csv.KMapper;

import java.util.HashMap;
import java.util.Map;

public class Mapper implements KMapper {

    private Map<String, KField> fields = new HashMap<String, KField>();

    @Override
    public KField field(String name) {
        if (!fields.containsKey(name)) {
            Field f = new Field();
            f.name = name;
            fields.put(name, f);
            return f;
        } else {
            return fields.get(name);
        }
    }

    @Override
    public void timestamp(String[] fields, String[] pattern) {

    }

}
