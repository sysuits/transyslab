package com.transyslab.experiments;

import com.google.inject.AbstractModule;
import com.transyslab.commons.tools.adapter.BoundarySimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;

public class XZ_SPD_FLOW_SERIES extends BoundarySimProblem {
    @Override
    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimulationEngine.class).to(XZEngineFLow.class);
                bind(SimController.class).to(XZ_SPD_FLOW_LONG.class);
            }
        };
    }
}

class XZ_SPD_FLOW_LONG extends XZ_LONG_CTRL {

    @Override
    public double singleEvaluate() {
        if (engine.getState()== Constants.STATE_QUIT)
            return Double.POSITIVE_INFINITY;
        double out = super.singleEvaluate();
        return (out*2.0+evaluateFlow())/3.0;
    }
}