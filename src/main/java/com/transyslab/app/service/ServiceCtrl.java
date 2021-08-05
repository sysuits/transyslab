package com.transyslab.app.service;

import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.EtyGPS;
import com.transyslab.simcore.mlp.MLPEngine;

import java.util.ArrayList;
import java.util.List;

public class ServiceCtrl extends SimController {

    @Override
    public void loadSimulationFiles() {
        //delay loadingFiles
        //do nothing
    }

    @Override
    public void simulate(Task task) {
        //get engine
        if (! (getEngine() instanceof MLPEngine && task instanceof ServiceTask))
            return;
        MLPEngine mlpEngine = (MLPEngine) getEngine();
        ServiceTask sTask = (ServiceTask) task;

        //set time and porperties
        mlpEngine.setConfig("nodes2Construct", sTask.nodeList);
        mlpEngine.setConfig("timeStart", sTask.fTime);
        mlpEngine.setConfig("timeEnd",sTask.tTime);
        mlpEngine.loadFiles();
        List<EtyGPS> records = new ArrayList<>();
        while (mlpEngine.simulationLoop()>=0) {
            if(Math.floorMod(mlpEngine.getStepCount(),mlpEngine.getTrackStep())==0)
                records.addAll(mlpEngine.getNetwork().recordGPSData());
            if (sTask.needStop())
                mlpEngine.stop();
        }
        task.setAttribute("resultData",records);
        task.announceReady();
    }
}
