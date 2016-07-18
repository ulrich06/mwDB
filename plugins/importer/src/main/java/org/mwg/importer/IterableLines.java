package org.mwg.importer;

import org.mwg.task.TaskResult;
import org.mwg.task.TaskResultIterator;

import java.io.*;

class IterableLines implements TaskResult<String> {

    private final String _path;

    IterableLines(String p_path) {
        this._path = p_path;
    }

    @Override
    public TaskResultIterator iterator() {
        BufferedReader _buffer = null;
        try {
            Reader reader;
            File file = new File(_path);
            if (file.exists()) {
                reader = new FileReader(file);
            } else {
                reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(_path));
            }
            _buffer = new BufferedReader(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final BufferedReader final_buffer = _buffer;
        return new TaskResultIterator() {
            @Override
            public Object next() {
                try {
                    String line = final_buffer.readLine();
                    if (line == null) {
                        final_buffer.close();
                    }
                    return line;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public String get(int index) {
        return null;
    }

    @Override
    public void set(int index, String input) {

    }

    @Override
    public void allocate(int index) {

    }

    @Override
    public void add(String input) {

    }

    @Override
    public TaskResult<String> clone() {
        return null;
    }

    @Override
    public void free() {

    }

    @Override
    public int size() {
        return -1;
    }

}
