/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transyslab.experiments;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RMSEProblem extends MLPProblem {
    public RMSEProblem() { }
    public RMSEProblem(String masterFileDir){
        initProblem(masterFileDir);
    }

    @Override
    protected SimulationConductor createConductor() {
        try {
            return new SimulationConductor() {

                @Override
                public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
                    double[] var = simSolution.getInputVariables();
                    ((MLPEngine)engine).setShortTermParas(Arrays.copyOfRange(var,0,4));
                    ((MLPEngine) engine).getSimParameter().setLCDStepSize(0.0);
                    ((MLPEngine) engine).getSimParameter().setLCBuffTime(var[4]);
                    ((MLPEngine) engine).getSimParameter().setLCSensitivity(var[5]);
                }

                @Override
                public double[] evaluateFitness(SimulationEngine engine) {
                    MLPEngine mlpEngine = (MLPEngine) engine;
                    if(mlpEngine.getStatus() == Constants.STATE_ERROR_QUIT)
                        return new double[]{Double.POSITIVE_INFINITY};//,Double.POSITIVE_INFINITY};
                    Map<String, List<MacroCharacter>> simMap = engine.getSimMap();
                    Map<String, List<MacroCharacter>> empMap = engine.getEmpMap();


                    // 检验是否通过
                    if (simMap != null && empMap != null ){
                        List<Double> resultList = new ArrayList<>();
                        List<MacroCharacter> empRecords = empMap.get("det2");
                        if(empRecords == null || empRecords.isEmpty()) {
                            System.out.println("Error: Can not find \"det2\"");
                            return new double[]{Double.POSITIVE_INFINITY};//,Double.POSITIVE_INFINITY};
                        }
                        List<MacroCharacter> simRecords = simMap.get("det2");
                        if (simRecords != null && !simRecords.isEmpty() ) {

                            double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
                            double[] empSpeed = MacroCharacter.select(empRecords, MacroCharacter.SELECT_SPEED);

                            double fitness = FitnessFunction.evaRMSE(simSpeed,empSpeed);
//                            MLPLink tmpLink = (MLPLink)engine.getNetwork().findLink(111);
//                            double vhcPropotion = tmpLink.countHoldingInflow()/(double)(tmpLink.getEmitNum()+tmpLink.countHoldingInflow());
                            resultList.add(fitness);
                            // 发车数量
//                            resultList.add(vhcPropotion);

                        }
                        double[] results = resultList.stream().mapToDouble(Double::doubleValue).toArray();
                        return results;
                    }
                    return new double[] {Double.POSITIVE_INFINITY};//, Double.POSITIVE_INFINITY};//,Double.POSITIVE_INFINITY};
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}

