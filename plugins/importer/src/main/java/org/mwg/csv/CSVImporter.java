package org.mwg.csv;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.csv.impl.CSVElement;
import org.mwg.csv.impl.Field;
import org.mwg.csv.impl.Mapper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public class CSVImporter {

    private final String COMMA = ",";
    private String SEP;
    private int groupSize = 1;
    private Mapper mapper;

    private boolean verbose;

    public CSVImporter() {
        SEP = COMMA;
        mapper = new Mapper();
    }

    public void setSeparator(String newSeparator) {
        SEP = newSeparator;
    }

    public void setVerbose() {
        verbose = true;
    }

    public void groupBy(int p_groupSize) {
        this.groupSize = p_groupSize;
    }

    public KMapper mapper() {
        return mapper;
    }

    public void importToNode(File csvFile, Node targetNode, Callback<Boolean> callback) throws IOException {
        mapper.nodeResolver(new KNodeResolver() {
            @Override
            public void resolve(Graph graph, Map<String, Integer> headers, String[] values, long toResolveWorld, long toResolveTime, Callback<Node> callback) {
                graph.lookup(targetNode.world(), toResolveTime, targetNode.id(), callback);
            }
        });
        importToGraph(csvFile, targetNode.graph(), targetNode.world(), callback);
    }

    public void importToGraph(File csvFile, Graph targetGraph, long targetWorld, Callback<Boolean> callback) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(csvFile));

        String header = reader.readLine();
        if (header == null) {
            if (callback != null) {
                callback.on(false);
            }
        } else {

            long startingTimestamp = System.currentTimeMillis();

            AtomicLong counter = new AtomicLong(0);
            AtomicLong waiter = new AtomicLong(0);
            AtomicBoolean isfinished = new AtomicBoolean(false);
            Callback injectCallback = new Callback() {
                @Override
                public void on(Object result) {
                    long current = counter.incrementAndGet();
                    if (isfinished.get() && waiter.get() == current) {
                        if (callback != null) {
                            System.out.println("Import finished in " + (System.currentTimeMillis() - startingTimestamp) + " ms");
                            callback.on(true);
                        }
                    }
                }
            };

            StringTokenizer st = new StringTokenizer(header, SEP);
            String[] headers = new String[st.countTokens()];
            Map<String, Integer> headersMap = new HashMap<String, Integer>();

            if (verbose) {
                System.out.println("<Headers>");
            }

            for (int i = 0; i < headers.length; i++) {
                headers[i] = st.nextToken();
                headersMap.put(headers[i], i);
                if (verbose) {
                    System.out.println("\t#" + i + ":<" + headers[i] + ">");
                }
            }
            if (verbose) {
                System.out.println("</Headers>");
            }

            String[][] values = new String[groupSize][];
            int valueIndex = 0;
            String valueLine = reader.readLine();
            while (valueLine != null) {
                if (valueIndex == values.length) {
                    //process result
                    waiter.incrementAndGet();
                    inject(targetGraph, targetWorld, headersMap, values, transform(headersMap, values), injectCallback);

                    values = new String[groupSize][];
                    valueIndex = 0;
                }
                StringTokenizer loopSt = new StringTokenizer(valueLine, SEP);
                String[] loopValue = new String[loopSt.countTokens()];
                for (int i = 0; i < loopValue.length; i++) {
                    loopValue[i] = loopSt.nextToken();
                }
                values[valueIndex] = loopValue;
                valueIndex++;
                valueLine = reader.readLine();
            }
            reader.close();
            isfinished.set(true);
            //process old result
            if (valueIndex != 0) {
                waiter.incrementAndGet();
                inject(targetGraph, targetWorld, headersMap, values, transform(headersMap, values), injectCallback);
            }
        }
    }

    private void inject(Graph graph, long toResolveWorld, Map<String, Integer> headers, String[][] lineElements, CSVElement[] elements, Callback callback) {
        for (int i = 0; i < elements.length; i++) {
            final CSVElement current = elements[i];
            mapper.nodeResolver.resolve(graph, headers, lineElements[i], toResolveWorld, elements[i].time, new Callback<Node>() {
                @Override
                public void on(Node result) {
                    if (result != null) {
                        current.inject(result, verbose);
                        result.free();
                        callback.on(null);
                    } else {
                        System.err.println("Node not found !!!");
                    }
                }
            });
        }
    }

    private CSVElement[] transform(Map<String, Integer> headers, String[][] lines) {
        CSVElement[] result = new CSVElement[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = new CSVElement();
            final String[] lineElements = lines[i];
            result[i].time = mapper.extractTime(headers, lineElements);
            final int finalI = i;
            headers.forEach(new BiConsumer<String, Integer>() {
                @Override
                public void accept(String name, Integer index) {
                    if (index < lineElements.length) {
                        Field field = (Field) mapper.getField(name);
                        if (field != null && !field.ignored) {
                            String fieldValue = lineElements[index];
                            if (mapper.globallyIgnoredValue == null || !mapper.globallyIgnoredValue.equals(fieldValue)) {
                                if (field.ignoredValue == null || !field.ignoredValue.equals(fieldValue)) {
                                    Object converted = null;
                                    if (field.transform != null) {
                                        converted = field.transform.transform(fieldValue);
                                    } else {
                                        try {
                                            switch (field.type) {
                                                case Type.DOUBLE:
                                                    converted = Double.parseDouble(fieldValue);
                                                    break;
                                                case Type.LONG:
                                                    converted = Long.parseLong(fieldValue);
                                                    break;
                                                case Type.INT:
                                                    converted = Integer.parseInt(fieldValue);
                                                    break;
                                                default:
                                                    converted = fieldValue;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (converted != null) {
                                        if (field.rename == null) {
                                            result[finalI].add(name, converted, field.type);
                                        } else {
                                            result[finalI].add(field.rename, converted, field.type);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });

            if (verbose) {
                System.out.print("<RawLine>");
                for (int j = 0; j < lineElements.length; j++) {
                    if (j != 0) {
                        System.out.print("|");
                    }
                    System.out.print(lineElements[j]);
                }
                System.out.println("</RawLine>");
                if (result[i] != null) {
                    result[i].verbosePrint();
                }
            }
        }
        return result;
    }

}
