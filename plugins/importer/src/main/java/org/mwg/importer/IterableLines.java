package org.mwg.importer;

import org.mwg.plugin.AbstractIterable;

import java.io.*;

class IterableLines extends AbstractIterable {

    private final BufferedReader _buffer;

    IterableLines(String path) throws FileNotFoundException {
        File file = new File(path);
        Reader reader;
        if (file.exists()) {
            reader = new FileReader(file);
        } else {
            reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path));
        }
        _buffer = new BufferedReader(reader);
    }

    @Override
    public Object next() {
        try {
            return _buffer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            _buffer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int estimate() {
        return -1;
    }

}
