/*
package com.transyslab.experiments;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.optimizer.DEAlgorithm;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

*/
/*

public class OptimizeWYM {

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();
		int pop = 20;
		double kjam = 0.1765, qmax = 0.4633, vfree = 21.7950, deltat = 0.2, vPhyLim = 120.0/3.6;
		double xcLower = MLPParameter.xcLower(kjam, qmax,deltat);
		double rupper = MLPParameter.rUpper(10,vfree,kjam,qmax);
		double[] plower = new double[]{xcLower+0.00001,1e-5,0.0,0.0};
		double[] pupper = new double[]{100.0, rupper-1e-5, 10.0, 10.0};//,180.0f,25,40,100};


		DEAlgorithm de = new DEAlgorithm();
		de.init(pop, plower.length,200,0.7f, 0.5f, plower, pupper);

		new SchedulerThread("ThreadManager", taskCenter) {
			@Override
			public void run() {
				double[] bestSpeed = null;
				List<Task> taskList = new ArrayList<>();
				TXTUtils txtWriter = new TXTUtils("src/main/resources/output/particle.csv");

				for (int i = 0; i < de.getMaxItrGeneration(); i++) {
					long tb = System.currentTimeMillis();
					for (int j = 0; j < de.getPopulation(); j++) {
						double[] parameters = new double[]{qmax,vfree,kjam, vPhyLim, 0.0, 0.0, 0.0, 0.0};//[Qm, Vfree, Kjam, VPhyLim]
						System.arraycopy(de.getNewPosition(j),0,parameters,4,de.getDim());
						taskList.add(dispatch(parameters, TaskWorker.ANY_WORKER));
					}
					for (int j = 0; j < de.getPopulation(); j++) {
						double[] tmpResults = taskList.get(j).getObjectiveValues();
						String tmpStr = Arrays.toString(de.getPosition(j)).replace(" ","");
						txtWriter.write(tmpStr.substring(1,tmpStr.length()-1) + "," + tmpResults[0] + "," + (i+1) + "\r\n");
						if(de.evoluteIndividual(j,tmpResults[0])){
							bestSpeed = new double[tmpResults.length - 1];
							System.arraycopy(tmpResults,1,bestSpeed,0,bestSpeed.length);
						}
					}
					System.out.println("Gbest : " + de.getGbestFitness());
					System.out.println("Position : " + de.showGBestPos());
					System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
					txtWriter.flushBuffer();
					taskList.clear();
				}
				System.out.println(Arrays.toString(bestSpeed));
				dismissAllWorkingThreads();//stop eng线程。
			}

		}.start();


		for (int i = 0; i < pop; i++) {

			new EngThread("Eng" + i, "src/main/resources/demo_neihuan/scenario2/optwym.properties", taskCenter) {
				@Override
				public double[] worksWith(Task task) {
					MLPEngine mlpEngine = (MLPEngine) getEngine();

					//仿真过程
					if(mlpEngine.runWithPara(task.getInputVariables()) == Constants.STATE_ERROR_QUIT){
						return new double[]{Double.MAX_VALUE};
					}

					//获取特定结果
					List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
					double[] simSpeeds = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();


					//评价结果
					int n = simSpeeds.length;
					double[] result = new double[n + 1];

					result[0] = FitnessFunction.evaRNSE(simSpeeds,mlpEngine.getEmpData());
					System.arraycopy(simSpeeds,0,result,1,n);
					return result;
				}
			}.start();
		}
	}
}
*/
