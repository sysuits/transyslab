/*
package com.transyslab.experiments;

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.optimizer.DEAlgorithm;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

*/
/*

public class MaximizeEmittedVehicle {
	protected List<Task> taskList;
	protected DEAlgorithm de;
	protected double[] bestSpeed;
	public MaximizeEmittedVehicle() {
		this.de = new DEAlgorithm();
		this.taskList = new ArrayList<>();
	}

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();
		int pop = 20;
		int repeatedTimes = 1;
		double kjam = 0.1765, qmax = 0.4633, vfree = 21.7950,deltat = 0.2;
		double xcLower = MLPParameter.xcLower(kjam, qmax,deltat);
		double rupper = MLPParameter.rUpper(10,vfree,kjam,qmax);
		double[] plower = new double[]{xcLower+0.00001,1e-5,0.0,0.0};
		double[] pupper = new double[]{100.0, rupper-1e-5, 10.0, 10.0};//,180.0f,25,40,100};

		MaximizeEmittedVehicle exp = new MaximizeEmittedVehicle();
		exp.de.init(pop, plower.length,200,0.7f, 0.5f, plower, pupper);
		new SchedulerThread("ThreadManager", taskCenter) {
			@Override
			public void run() {
				exp.run(this);
				dismissAllWorkingThreads();//stop eng线程。
			}

		}.start();
		for (int i = 0; i < pop; i++) {
			new EngThread("Eng" + i, "src/main/resources/demo_neihuan/scenario2/kscalibration.properties", taskCenter) {
				@Override
				public double[] worksWith(Task task) {
					MLPEngine mlpEngine = (MLPEngine) getEngine();
					mlpEngine.getSimParameter().setLCDStepSize(2.0);
					int[] vhcCount = new int[repeatedTimes];
					for(int i = 0;i<repeatedTimes;i++){
						//仿真过程
						if(mlpEngine.runWithPara(task.getInputVariables()) == Constants.STATE_ERROR_QUIT){
							return new double[]{Integer.MAX_VALUE};
						}
						//获取特定结果
						*/
/*List<MacroCharacter> records = mlpEngine.getMlpNetwork().getSecStatRecords("det2");
						simSpeeds[i] = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();*//*

						vhcCount[i] = mlpEngine.countOnHoldVeh();
					}
					//评价结果
					double[] result = new double[1];
					//统计多次仿真的平均剩余发车数
					double sum = 0;
					for(int i=0;i<repeatedTimes;i++){
						sum += vhcCount[i];
					}
					result[0] = sum/repeatedTimes;
					return result;
				}
			}.start();
		}
	}
	public void run(SchedulerThread manager){

		for (int i = 0; i < de.getMaxItrGeneration(); i++) {
			long tb = System.currentTimeMillis();
			for (int j = 0; j < de.getPopulation(); j++) {
				double[] parameters = new double[]{0.4633,21.7950,0.1765, 120.0/3.6,0.0, 0.0, 0.0, 0.0};//[Qm, Vfree, Kjam, VPhyLim]
				System.arraycopy(de.getNewPosition(j),0,parameters,4,de.getDim());
				taskList.add(manager.dispatch(parameters, TaskWorker.ANY_WORKER));
			}
			for (int j = 0; j < de.getPopulation(); j++) {
				double[] tmpResults = taskList.get(j).getObjectiveValues();
				if(de.evoluteIndividual(j,tmpResults[0])){

				}
			}
			System.out.println("Gbest : " + de.getGbestFitness());
			System.out.println("Position : " + de.showGBestPos());
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
			taskList.clear();
		}
		System.out.println(Arrays.toString(bestSpeed));
	}
}
*/
