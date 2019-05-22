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

import java.util.*;

public class KSIdvdProblem extends MLPProblem {
    public KSIdvdProblem(){ }
    public KSIdvdProblem(String masterFileDir){
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
                    // TODO 父类重新设计
                    MLPNetwork mlpNetwork = mlpEngine.getNetwork();
                    LinkedList<double[]> simIdvdMap = new LinkedList<>();
                    for (int j = 0; j < mlpNetwork.nSensors(); j++){
                        MLPLoop tmpLoop = (MLPLoop) mlpNetwork.getSensor(j);
                        if (tmpLoop.getName().equals("det2")) {//按需输出记录
                            for(double[] data:tmpLoop.getRecords()){
                                simIdvdMap.add(data);
                            }
                        }
                    }
                    Map<String, List<MicroCharacter>> empMicroMap = mlpEngine.getEmpMicroMap();
                    // 检验是否通过
                    if (simIdvdMap != null && empMicroMap != null ){
                        List<Double> resultList = new ArrayList<>();
                        List<MicroCharacter> empRecords = empMicroMap.get("det2");
                        if(empRecords == null || empRecords.isEmpty()) {
                            System.out.println("Error: Can not find \"det2\"");
                            return new double[]{Double.POSITIVE_INFINITY};
                        }
                        if (!simIdvdMap.isEmpty() ) {
                            // 车速数据已按时间排序
                            //double[] simSpeed = simIdvdMap.stream().mapToDouble(e->e[1]).toArray();
                            //double[] empSpeed = MicroCharacter.select(empRecords, MicroCharacter.SELECT_SPEED);
                            double sumEmpFlow = empRecords.size();
                            //double sumSimFlow = Arrays.stream(empSpeed).count();

                            // 计算所有15min内车速分布的ks距离
                            int horizon = 10 * 60;
                            // 前15min预热
                            int shifting = 900;
                            //int statStepSize = (int)((MLPEngine)engine).getSimParameter().getStatStepSize();
                            //int numOfDataInDistr = horizon/statStepSize;
                            // TODO 仿真时长/horizon
                            int numOfDistr = 12;
                            double ksDists[] = new double[numOfDistr];

                            for(int i=0;i<numOfDistr;i++){
                                final int periodId = i;
                                double[] tmpSimSpeed = simIdvdMap.stream().filter(l -> l[0] >= shifting + periodId * horizon && l[0] <= shifting + (periodId + 1) * horizon).mapToDouble(e -> e[1]).toArray();
                                double[] tmpEmpSpeed = empRecords.stream().filter(l -> l.getDetTime() >= shifting + periodId * horizon && l.getDetTime() <= shifting + (periodId + 1) * horizon).mapToDouble(e -> e.getSpeed()).toArray();
                                double nVhc = tmpEmpSpeed.length;
                                //double[] tmpSimFlow = Arrays.copyOfRange(simFlow,i*numOfDataInDistr,(i+1)*numOfDataInDistr+1);
                                //double tSumSimFlow = Arrays.stream(tmpSimFlow).sum();
                                // ksDists[i] = (tSumEmpFlow - tSumSimFlow)/(sumEmpFlow-sumSimFlow) * FitnessFunction.evaKSDistance(tmpSimSpeed,tmpEmpSpeed);
                                if(tmpSimSpeed.length>0 & tmpEmpSpeed.length>0)
                                    ksDists[i] = nVhc / sumEmpFlow *FitnessFunction.evaKSDistance(tmpSimSpeed, tmpEmpSpeed);
                                else {
                                    //System.out.println("过车数为0");
                                    ksDists[i] = nVhc / sumEmpFlow * 1.0;
                                }
                                //}
                            }
                            //double avgKSDist = Arrays.stream(ksDists).average().getAsDouble();
                            double avgKSDist = Arrays.stream(ksDists).sum();
                            // 平均分布差异
                            resultList.add(avgKSDist);

//                            MLPLink tmpLink = (MLPLink)engine.getNetwork().findLink(111);
//                            double vhcPropotion = tmpLink.countHoldingInflow()/(double)(tmpLink.getEmitNum()+tmpLink.countHoldingInflow());
//                            // 发车数量
//                            resultList.add(vhcPropotion);
                        }
                        double[] results = resultList.stream().mapToDouble(Double::doubleValue).toArray();
                        return results;
                    }
                    return new double[] {Double.POSITIVE_INFINITY};//, Double.POSITIVE_INFINITY};//,Double.POSITIVE_INFINITY};
                }
                @Override
                public void modifySolutionBeforeEnd(SimulationEngine engine, SimSolution simSolution) {
                    simSolution.setAttribute("SimSeed",((MLPEngine)engine).runningSeed);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
