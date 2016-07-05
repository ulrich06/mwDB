package org.mwg.importer;

import org.mwg.task.Task;

import static org.mwg.task.Actions.newTask;

public class ImporterActions {

    public static final String READFILES = "readFiles";

    public static final String READLINES = "readLines";

    public static Task readLines(String path) {
        return newTask().action(READLINES, path);
    }

    public static Task readFiles(String pathOrVar) {
        return newTask().action(READFILES,pathOrVar);
    }

}
