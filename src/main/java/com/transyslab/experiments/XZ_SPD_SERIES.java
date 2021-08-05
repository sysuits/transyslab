package com.transyslab.experiments;

import com.google.inject.AbstractModule;
import com.transyslab.commons.tools.adapter.BoundarySimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;

public class XZ_SPD_SERIES extends BoundarySimProblem {
    @Override
    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimulationEngine.class).to(XZEngine.class);
                bind(SimController.class).to(XZ_LONG_CTRL.class);
            }
        };
    }
}

class XZ_LONG_CTRL extends XZ_SHORT_CTRL {
    @Override
    public void modifyEngineBeforeStart(Task task) {
        super.modifyEngineBeforeStart(task);
        SimSolution simSolution = (SimSolution) task;
        ((MLPEngine)engine).setFDParas(
                simSolution.getVariableValue(5),
                simSolution.getVariableValue(6),
                simSolution.getVariableValue(7),
                simSolution.getVariableValue(8),
                simSolution.getVariableValue(9));
    }
}