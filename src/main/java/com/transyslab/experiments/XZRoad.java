package com.transyslab.experiments;

import com.google.inject.AbstractModule;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.BoundarySimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPNode;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.Arrays;

public class XZRoad extends BoundarySimProblem {

    @Override
    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimulationEngine.class).to(XZEngine.class);
                bind(SimController.class).to(XZ_SHORT_CTRL.class);
            }
        };
    }
}

class XZEngine extends MLPEngine{//can not crush too many times
    int crushCount = 0;

    @Override
    public void handle(String msg) {
        super.handle(msg);
        if (msg.contains("car crush") && ++crushCount>10){
            crushCount = 0;
            stop();
        }
    }
}

class XZ_SHORT_CTRL extends SimController{
    double repeatTimes = 10;
    int runtimes;
    double res;

    public double evaluateFlow(){
        double[] sim3 = engine.getSimMap().get("det1").stream().mapToDouble(MacroCharacter::getHourFlow).toArray();
        double[] emp3 = engine.getEmpMap().get("det1").stream().mapToDouble(MacroCharacter::getHourFlow).toArray();
        return FitnessFunction.evaRMSE(sim3,emp3);
    }

    @Override
    public void modifyEngineBeforeStart(Task task) {
        runtimes = 0;
        res = 0;

        SimSolution simSolution = (SimSolution) task;
        MLPNode.NODE_ALPHA = simSolution.getVariableValue(0);
        MLPNode.NODE_BETA = simSolution.getVariableValue(1);
        MLPNode.NODE_PASS_SPD = simSolution.getVariableValue(2);
        MLPEngine engine = (MLPEngine) getEngine();
        engine.setParameter("gamma1",simSolution.getVariableValue(3));
        engine.setParameter("gamma2",simSolution.getVariableValue(4));
    }

    @Override
    public double[] evaluateFitness() {
        return new double[]{res/repeatTimes};
    }

    @Override
    public boolean needRerun() {
        res += singleEvaluate();
        return ++runtimes<repeatTimes;
    }

    public double singleEvaluate(){
        if (engine.getState()== Constants.STATE_QUIT)
            return Double.POSITIVE_INFINITY;
        MLPEngine engine = (MLPEngine) getEngine();
        double[] sim1 = engine.getSimMap().get("link73074").stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
        double[] emp1 = engine.getEmpMap().get("link73074").stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
        double[] sim2 = engine.getSimMap().get("link73072").stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
        double[] emp2 = engine.getEmpMap().get("link73072").stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
        System.out.println("sim1: " + Arrays.toString(sim1));
        System.out.println("emp1: " + Arrays.toString(emp1));
        System.out.println("sim2: " + Arrays.toString(sim2));
        System.out.println("emp2: " + Arrays.toString(emp2));
        double res = 0.5 * (FitnessFunction.evaRMSE(sim1,emp1) + FitnessFunction.evaRMSE(sim2,emp2));
        return res;
    }
}