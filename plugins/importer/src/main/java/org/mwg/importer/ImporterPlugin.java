package org.mwg.importer;

import org.mwg.plugin.AbstractPlugin;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskActionFactory;

public class ImporterPlugin extends AbstractPlugin {

    public ImporterPlugin() {
        declareTaskAction(ActionReadLines.READLINE_NAME, new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException(ActionReadLines.READLINE_NAME + " action need one parameter");
                }
                return new ActionReadLines(params[0]);
            }
        });
    }
}
