package org.mwg.importer;

import org.mwg.plugin.AbstractPlugin;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskActionFactory;

public class ImporterPlugin extends AbstractPlugin {

    public ImporterPlugin() {
        declareTaskAction(ImporterActions.READLINES, new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException(ImporterActions.READLINES + " action need one parameter");
                }
                return new ActionReadLines(params[0]);
            }
        });

        declareTaskAction(ImporterActions.READFILES, new TaskActionFactory() {
            @Override
            public TaskAction create(String[] params) {
                if (params.length != 1) {
                    throw new RuntimeException(ImporterActions.READFILES + " action need one parameter");
                }
                return new ActionReadFiles(params[0]);
            }
        });
    }
}
