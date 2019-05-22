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
import com.transyslab.simcore.mlp.*;

import java.util.*;


public class BaseEXP {
	public static void main(String[] args) {
		String fitnessType = "RMSE";
		for(int k=0;k<1;k++){
			MLPEngine mlpEngine = new MLPEngine("src/main/resources/validation.properties");
			mlpEngine.loadFiles();
			mlpEngine.repeatRun();
			MLPNetwork mlpNetwork = mlpEngine.getNetwork();
			if(fitnessType.equals("KS")){
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
				if (simIdvdMap != null && empMicroMap != null ) {
					List<Double> resultList = new ArrayList<>();
					List<MicroCharacter> empRecords = empMicroMap.get("det2");
					if (empRecords == null || empRecords.isEmpty()) {
						System.out.println("Error: Can not find \"det2\"");
					}
					//List<MicroCharacter> simRecords = simMap.get("det2");
					if (!simIdvdMap.isEmpty()) {
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
						for (int i = 0; i < numOfDistr; i++) {
							final int periodId = i ;
							double[] tmpSimSpeed = simIdvdMap.stream().filter(l -> l[0] >= shifting + periodId * horizon && l[0] <= shifting + (periodId + 1) * horizon).mapToDouble(e -> e[1]).toArray();
							double[] tmpEmpSpeed = empRecords.stream().filter(l -> l.getDetTime() >= shifting + periodId * horizon && l.getDetTime() <= shifting + (periodId + 1) * horizon).mapToDouble(e -> e.getSpeed()).toArray();
							double nVhc = tmpEmpSpeed.length;
							if (tmpSimSpeed.length > 0 & tmpEmpSpeed.length > 0)
								ksDists[i] = nVhc / sumEmpFlow * FitnessFunction.evaKSDistance(tmpSimSpeed, tmpEmpSpeed);
							else {
								ksDists[i] = nVhc / sumEmpFlow * 1.0;
							}
						}
						//double avgKSDist = Arrays.stream(ksDists).average().getAsDouble();
						double avgKSDist = Arrays.stream(ksDists).sum();
						System.out.println("fitness: " + avgKSDist);
					}

				}
			}
			else if(fitnessType.equals("RMSE")){
				Map<String, List<MacroCharacter>> simMap = mlpEngine.getSimMap();
				Map<String, List<MacroCharacter>> empMap = mlpEngine.getEmpMap();

				// 检验是否通过
				if (simMap != null && empMap != null ) {
					List<MacroCharacter> empRecords = empMap.get("det2");
					if (empRecords == null || empRecords.isEmpty()) {
						System.out.println("Error: Can not find \"det2\"");
					}
					List<MacroCharacter> simRecords = simMap.get("det2");
					if (simRecords != null && !simRecords.isEmpty()) {

						double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
						double[] empSpeed = MacroCharacter.select(empRecords, MacroCharacter.SELECT_SPEED);
						double fitness = FitnessFunction.evaRMSE(simSpeed, empSpeed);
						System.out.println("RMSE: " + fitness);
					}
				}
			}
			mlpEngine.close();
		}
	}
}
