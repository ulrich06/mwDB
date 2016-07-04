package org.mwg.importer;

import org.mwg.plugin.AbstractIterable;

import java.io.*;

public class IterableFile extends AbstractIterable {

    private final File _file;
    private final FileReader _reader;
    private final BufferedReader _buffer;

    public IterableFile(String path) throws FileNotFoundException {
        _file = new File(path);
        _reader = new FileReader(_file);
        _buffer = new BufferedReader(_reader);
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
            _reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
