package com.transyslab.simcore.mlp;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.transyslab.commons.tools.adapter.BoundarySimProblem;
import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.simcore.SimulationEngine;

public class MLPProblem extends BoundarySimProblem {
    @Override
    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimulationEngine.class).to(MLPEngine.class);
                bind(SimController.class).to(MLPDefaultController.class);
            }
        };
    }
}

class MLPDefaultController extends SimController{

    @Override
    public void modifyEngineBeforeStart(Task task) {

    }

    @Override
    public double[] evaluateFitness() {
        return new double[0];
    }
}