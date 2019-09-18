package com.transyslab.commons.tools.mutitask;

import com.google.inject.Inject;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;

public abstract class SimController {
    protected SimulationEngine engine;

    public SimController(){}

    public SimController(SimulationEngine engine){
        setEngine(engine);
    }

    @Inject
    public void setEngine(SimulationEngine engine){
        this.engine = engine;
    }

    public SimController config(String masterFileDir){
        this.engine.config(masterFileDir);
        return this;
    }

    public double[] simulate(Task task){
        //��������
        modifyEngineBeforeStart(task);

//        if (engine instanceof MLPEngine)
//            ((MLPEngine) engine).fileOutTag = Arrays.toString(task.getInputVariables());

        //�������
        if (!violateConstraints()) {
            do {
                engine.repeatRun();
            }
            while (needRerun());
        }

        //�������
        double[] fitness = evaluateFitness();

        //�޸�solution������
        modifySolutionBeforeEnd(task);

        return fitness;
    }

    public abstract void modifyEngineBeforeStart(Task task);

    public boolean violateConstraints(){return false;}

    public boolean needRerun(){ return false;}

    public abstract double[] evaluateFitness();

    public void modifySolutionBeforeEnd(Task task){}

    public int countRunTimes(){
        return engine.countRunTimes();
    }

    public void init(){
        engine.loadFiles();
    }

    public void close(){
        engine.close();
    }

    protected SimulationEngine getEngine(){
        return engine;
    }

    public void initEngine(String modelType, String masterFileName){
        switch (modelType) {
            case "MesoTS":
                //TODO dir��Ҫȥ���ļ�����׺
                engine = new MesoEngine(0,null);
                break;
            case "MLP":
                engine = new MLPEngine(masterFileName);
                break;
            default:
                System.err.println("Unsupported model name");
        }
    }
}
