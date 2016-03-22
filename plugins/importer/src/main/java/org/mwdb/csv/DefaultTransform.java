package org.mwdb.csv;

import org.mwdb.KType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultTransform implements CSVTransform {

    private Set<String> ignored = new HashSet<String>();
    private Set<String> ignoredValues = new HashSet<String>();
    private Map<String, Byte> types = new HashMap<String, Byte>();

    @Override
    public CSVElement[] transform(String[] headers, String[][] lines) {
        CSVElement[] result = new CSVElement[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = new CSVElement();
            result[i].time = extractTime(headers, lines[i]);
            for (int j = 0; j < lines[i].length; j++) {
                if (!ignored.contains(headers[j]) && !ignoredValues.contains(lines[i][j])) {
                    Object[] converted = convertField(headers[j], lines[i][j]);
                    result[i].add(headers[j], converted[0], (Byte) converted[1]);
                }
            }
        }
        return result;
    }

    protected long extractTime(String[] headers, String[] lines) {
        return 0;
    }

    protected Object[] convertField(String name, String value) {
        try {
            if (types.containsKey(name)) {
                switch (types.get(name)) {
                    case KType.DOUBLE:
                        return new Object[]{Double.parseDouble(value), KType.DOUBLE};
                    case KType.LONG:
                        return new Object[]{Long.parseLong(value), KType.LONG};
                    case KType.INT:
                        return new Object[]{Integer.parseInt(value), KType.INT};
                    default:
                        return new Object[]{value, KType.STRING};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[]{value, KType.STRING};
    }

    public DefaultTransform ignore(String name) {
        ignored.add(name);
        return this;
    }

    public DefaultTransform ignoreValue(String value) {
        ignoredValues.add(value);
        return this;
    }

    public DefaultTransform asDouble(String name) {
        types.put(name, KType.DOUBLE);
        return this;
    }

    public DefaultTransform asInt(String name) {
        types.put(name, KType.INT);
        return this;
    }

    public DefaultTransform asLong(String name) {
        types.put(name, KType.LONG);
        return this;
    }

}
