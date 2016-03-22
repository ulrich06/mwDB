package org.mwdb.csv.impl;

import org.mwdb.csv.KField;
import org.mwdb.csv.KMapper;
import org.mwdb.csv.KNodeResolver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Mapper implements KMapper {

    private Map<String, KField> fields = new HashMap<String, KField>();
    private String timeFields;
    private SimpleDateFormat dateFormat;
    public String globallyIgnoredValue = null;
    public KNodeResolver nodeResolver = null;

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

    public KField getField(String name) {
        return fields.get(name);
    }

    @Override
    public void extractTime(String fields, String patterns) {
        this.timeFields = fields;
        this.dateFormat = new SimpleDateFormat(patterns);
    }


    @Override
    public KMapper globallyIgnoreIfValueEquals(String value) {
        globallyIgnoredValue = value;
        return this;
    }

    @Override
    public void nodeResolver(KNodeResolver resolver) {
        nodeResolver = resolver;
    }

    public long extractTime(Map<String, Integer> headers, String[] lineElements) {
        String query = timeFields;
        for (String name : headers.keySet()) {
            int headerIndex = headers.get(name);
            if (headerIndex < lineElements.length) {
                query = query.replace("{" + name + "}", lineElements[headers.get(name)]);
            }
        }
        try {
            return dateFormat.parse(query).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
