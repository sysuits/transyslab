package com.transyslab.experiments;

import com.google.inject.AbstractModule;
import com.transyslab.commons.tools.adapter.BoundarySimProblem;
import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;

public class XZ_FLOW_SERIES extends BoundarySimProblem {
    @Override
    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimulationEngine.class).to(XZEngineFLow.class);
                bind(SimController.class).to(FlowCompareCtrl.class);
            }
        };
    }
}

class XZEngineFLow extends XZEngine{
    @Override
    public void readSensors(String path) {
        mlpNetwork.createSensor("det1",73074L,0.9,5);
    }
}

class FlowCompareCtrl extends XZ_SHORT_CTRL {
    @Override
    public double singleEvaluate() {
        if (engine.getState()== Constants.STATE_QUIT)
            return Double.POSITIVE_INFINITY;
        return evaluateFlow();
    }
}
