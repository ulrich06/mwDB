package org.mwg.importer;

import org.mwg.task.Task;

import static org.mwg.task.Actions.newTask;

public class ImporterActions {

    public static Task readLines(String path) {
        return newTask().action(ActionReadLines.READLINE_NAME, path);
    }

}
