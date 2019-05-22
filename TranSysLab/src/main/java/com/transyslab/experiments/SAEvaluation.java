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

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.*;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.*;


public class SAEvaluation {
	public static void main(String[] args) throws IOException {

		// 读取参数
		CSVPrinter printer = CSVUtils.getCSVWriter("src/main/resources/SAResult3.csv",null,false);
		// 运行结果
		List<CSVRecord> inputRecords = CSVUtils.readCSV("src/main/resources/sarun3.csv",null);
		double[][] paramSets = new double[inputRecords.size()][inputRecords.get(0).size()];
		for(int i=0;i<paramSets.length;i++){
			for(int j=0;j<paramSets[0].length;j++){
				paramSets[i][j] = Double.parseDouble(inputRecords.get(i).get(j));
			}
		}
		// 初始化引擎
		ToExternalModel runner = new ToExternalModel("src/main/resources/optmksidvd.properties") {
			// 重写engine计算内容
			public EngThread createEngThread(String name, String masterFileDir) {
				return new EngThread(name, masterFileDir) {

					public double[] worksWith(Task task) {

						MLPEngine engine = (MLPEngine) getEngine();
						double[] var = task.getInputVariables();

						engine.setShortTermParas(Arrays.copyOfRange(var, 0, 4));
						engine.getSimParameter().setLCDStepSize(0.0);
						engine.getSimParameter().setLCBuffTime(var[4]);
						engine.getSimParameter().setLCSensitivity(var[5]);
						engine.seedFixed = true;
						engine.runningSeed = (long)var[6];
						// 运行仿真
						engine.repeatRun();
						// 计算分布差异
						MLPNetwork mlpNetwork = engine.getNetwork();
						LinkedList<double[]> simIdvdMap = new LinkedList<>();
						List<Double> simHeadway = new ArrayList<>();
						for (int j = 0; j < mlpNetwork.nSensors(); j++){
							MLPLoop tmpLoop = (MLPLoop) mlpNetwork.getSensor(j);
							if (tmpLoop.getName().equals("det2")) {
								//遍历车道
								for(double[] data:tmpLoop.getRecords()){
									simIdvdMap.add(data);
								}
								simHeadway.addAll(tmpLoop.getPeriodHeadway(0,8100));
							}
						}
						Map<String, List<MicroCharacter>> empMicroMap = engine.getEmpMicroMap();
						List<Double> resultList = new ArrayList<>();

						if (simIdvdMap != null && empMicroMap != null ) {

							List<MicroCharacter> empRecords = empMicroMap.get("det2");
							if (empRecords == null || empRecords.isEmpty()) {
								System.out.println("Error: Can not find \"det2\"");
								return new double[]{Double.POSITIVE_INFINITY};//,Double.POSITIVE_INFINITY};
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

								// TODO 仿真时长/horizon
								int numOfDistr = 12;
								double wksSpeeds[] = new double[numOfDistr];
								double mksSpeeds[] = new double[numOfDistr];
								double wksHeadways[] = new double[numOfDistr];
								double mksHeadways[] = new double[numOfDistr];
								for (int i = 0; i < numOfDistr; i++) {
									final int periodId = i;
									// 车速
									double[] tmpSimSpeed = simIdvdMap.stream().filter(l -> l[0] >= shifting + periodId * horizon && l[0] <= shifting + (periodId + 1) * horizon).mapToDouble(e -> e[1]).toArray();
									double[] tmpEmpSpeed = empRecords.stream().filter(l -> l.getDetTime() >= shifting + periodId * horizon && l.getDetTime() <= shifting + (periodId + 1) * horizon).mapToDouble(e -> e.getSpeed()).toArray();
									// 车头时距
									double[] tmpEmpHeadway = empRecords.stream().filter(l -> l.getDetTime() >= shifting + periodId * horizon && l.getDetTime() <= shifting + (periodId + 1)* horizon).mapToDouble(e->e.getHeadway()).toArray();
									double[] tmpSimHeadway = simHeadway.stream().filter(l->l.doubleValue()>=shifting + periodId * horizon && l.doubleValue()<= shifting + (periodId + 1)* horizon).mapToDouble(e-> e.doubleValue()).toArray();
									double nVhc = tmpEmpSpeed.length;

									if (tmpSimSpeed.length > 0 & tmpEmpSpeed.length > 0){
										mksSpeeds[i] = FitnessFunction.evaKSDistance(tmpSimSpeed, tmpEmpSpeed);
										wksSpeeds[i] = nVhc / sumEmpFlow * mksSpeeds[i];
										mksHeadways[i] =  FitnessFunction.evaKSDistance(tmpSimHeadway, tmpEmpHeadway);
										wksHeadways[i] = nVhc / sumEmpFlow * mksHeadways[i];
									}
									else {
										//System.out.println("过车数为0");
										mksSpeeds[i] = 1.0;
										wksSpeeds[i] = nVhc / sumEmpFlow * 1.0;
										mksHeadways[i] = 1.0;
										wksHeadways[i] = nVhc / sumEmpFlow * 1.0;

									}
								}
								//double avgKSDist = Arrays.stream(ksDists).average().getAsDouble();
								double wksSpeed = Arrays.stream(wksSpeeds).sum();
								double wksHeadway = Arrays.stream(wksHeadways).sum();
								double mksSpeed = Arrays.stream(mksSpeeds).average().getAsDouble();
								double mksHeadway = Arrays.stream(mksHeadways).average().getAsDouble();
								resultList.add(wksHeadway);
								resultList.add(mksHeadway);
								resultList.add(wksSpeed);
								resultList.add(mksSpeed);

							}
							/* RMSE
							 * */
							Map<String, List<MacroCharacter>> simMap = engine.getSimMap();
							Map<String, List<MacroCharacter>> empMap = engine.getEmpMap();
							if (simMap != null && empMap != null ) {
								List<MacroCharacter> empRecords2 = empMap.get("det2");
								if (empRecords2 == null || empRecords2.isEmpty()) {
									System.out.println("Error: Can not find \"det2\"");
									return new double[]{Double.POSITIVE_INFINITY};
								}
								List<MacroCharacter> simRecords = simMap.get("det2");
								if (simRecords != null && !simRecords.isEmpty()) {
									double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
									double[] empSpeed = MacroCharacter.select(empRecords2, MacroCharacter.SELECT_SPEED);
									double[] simFlow = MacroCharacter.select(simRecords,MacroCharacter.SELECT_FLOW);
									double[] empFlow = MacroCharacter.select(empRecords2,MacroCharacter.SELECT_FLOW);
									resultList.add(FitnessFunction.evaRMSE(simFlow,empFlow));
									resultList.add(FitnessFunction.evaRMSE(simSpeed, empSpeed));
								}
							}
							double[] results = resultList.stream().mapToDouble(Double::doubleValue).toArray();
							String sr = Arrays.toString(results);
							String[] singleResults = sr.substring(1, sr.length() - 1).split(",");
							task.setAttribute("RandomResults",singleResults);
							}
						return new double[]{1};
					}
				};
			}
		};
		// ??????????
		runner.startSimEngines();
		// ?ù??????????????·???
		int numOfParamSets = paramSets.length;
		for(int i = 0;i<numOfParamSets;i++){
			runner.dispatchTask(paramSets[i],i%20);
			if((i+1)%20 == 0){
				for(int j=0;j<20;j++) {

					String[] result2Write = (String[])runner.getTaskAttribution(j,"RandomResults");
					printer.printRecord(result2Write);
				}
				runner.clearTaskList();
				printer.flush();
			}
		}
		printer.close();
		runner.closeSimEngines();
	}
}
