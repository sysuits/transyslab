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

package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.ArrayList;
import java.util.List;

//同步扰动随机逼近算法
public class SPSA extends SchedulerThread {
	private int dims_;
	//需要标定的参数，已做归一化操作
	private float[] parameters_;
	//两组不同方向扰动的参数,不做归一化
	private float[][] newPara;
	private float[] pLower_;
	private float[] pUpper_;
	private double a_;
	private double A_;
	private double alpha_;
	private double gradientStep_;
	private double[] gradient_;
	private double c_;
	private double gamma_;
	private double perturbationStep_;
	private int[] perturbationValue_;
	private int iterationLim;
	private double fitness;
	//二项分布
	private BinomialDistribution bd_;
	
	public SPSA(){
		super("Unknown", null);//不作为SchedulerThread使用
		//伯努利分布
		bd_ = new BinomialDistribution(1,0.5);		
	}
	public SPSA(int dims){
		super("Unknown", null);//不作为SchedulerThread使用
		dims_ = dims;
		parameters_ = new float[dims];
		newPara = new float[3][dims];
		bd_ = new BinomialDistribution(1,0.5);
		gradient_ = new double[dims];
		perturbationValue_ = new int[dims];
	}
	public SPSA(int dims,String threadName, TaskCenter tc ){
		super(threadName,tc);
		dims_ = dims;
		parameters_ = new float[dims];
		newPara = new float[3][dims];
		bd_ = new BinomialDistribution(1,0.5);
		gradient_ = new double[dims];
		perturbationValue_ = new int[dims];
		
		fitness = Double.MAX_VALUE;
	}
	public int getDimention(){
		return dims_;
	}
	public float[] getParameters(){
		return parameters_;
	}
	public void setAlgParameters(double a, double A, double alpha,double c,double gamma){
		a_ = a;
		A_ = A;
		alpha_ = alpha;
		c_ = c;
		gamma_ = gamma;
	}
	public void setBounderies(float[] plower, float[] pupper){
		pLower_ = plower;
		pUpper_ = pupper;
	}
	public void setParameters(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters_[i] = parameters[i];
			//归一化
			parameters_[i] = (parameters_[i] - pLower_[i]) / (pUpper_[i]- pLower_[i]);
		}
	}
	//反归一化
	public void inverseNomalization(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters[i] = parameters[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
	}
	public float[] inverseNomal(float[] parameters){
		float[] tmp = new float[parameters.length];
		for(int i=0;i<parameters.length;i++){
			tmp[i] = parameters[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
		return tmp;
	}
	public void getInverseNomal(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters[i] = parameters_[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
//		return parameters;
	}
	//同时扰动参数
	public void perturbation(int k, float[] parameters1, float[] parameters2){
		perturbationStep_ = c_ / (Math.pow((k+1),gamma_));
		//重设时间种子
//		bd_.reseedRandomGenerator(System.currentTimeMillis());
		perturbationValue_ = bd_.sample(dims_);
		for(int i=0;i<dims_;i++){
			if(perturbationValue_[i] == 0)
				perturbationValue_[i] = -1;
			parameters1[i] = (float) (parameters_[i] + perturbationStep_ * perturbationValue_[i]); 
			parameters2[i] = (float) (parameters_[i] - perturbationStep_ * perturbationValue_[i]); 
		}
		//反归一化，用于仿真计算
		inverseNomalization(parameters1);
		inverseNomalization(parameters2);
//		System.out.println("");
	}
	//估计第k次迭代的梯度方向
	public void estimateGradient(double psim, double nsim){
		for(int i=0;i<dims_;i++){
			gradient_[i] = (psim-nsim)/(2 * perturbationStep_ * perturbationValue_[i]);
		}		
	}
	//梯度下降
	public void updateParameters(int k, float[] parameters){
		//更新第k次迭代的梯度下降步长
		gradientStep_ = a_/(Math.pow((A_+k+1), alpha_));
		for(int i=0;i<dims_;i++){
			parameters_[i] = (float) (parameters_[i] - gradientStep_ * gradient_[i]);
			parameters[i] = parameters_[i];
		}
		inverseNomalization(parameters);
	}
	public void updateParameters(int k){
		//更新第k次迭代的梯度下降步长
		gradientStep_ = a_/(Math.pow((A_+k+1), alpha_));
		for(int i=0;i<dims_;i++){
			parameters_[i] = (float) (parameters_[i] - gradientStep_ * gradient_[i]);
		}
	}
	public String showGBestPos() {
		String s = "";		
		for (int i = 0; i < newPara[2].length; i++) {
			s += String.valueOf( newPara[2][i]) + " ";
		}
		return s;
	}
	public void setMaxGeneration(int arg) {
		iterationLim = arg;
	}
	
	@Override
	public void run(){
		List<Task> taskList = new ArrayList<>();
		for (int i = 0; i < iterationLim; i++) {
			taskList.clear();

			//扰动参数，更新第一、二个engine的参数
			perturbation(i, newPara[0], newPara[1]);
			newPara[2] = inverseNomal(parameters_);
			long tb = System.currentTimeMillis();
			for (int j = 0; j < 3; j++) {
				taskList.add(dispatch(newPara[j], TaskWorker.ANY_WORKER));
			}
			double tmp2 = taskList.get(2).getObjectiveValues()[0];
			if(tmp2<fitness)
				fitness = tmp2;
			
			System.out.println("Gbest : " + fitness);
			System.out.println("Position : " + showGBestPos());
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
			if(i!=iterationLim-1){
				//梯度逼近
				double tmp0 = taskList.get(2).getObjectiveValues()[0];
				double tmp1 = taskList.get(2).getObjectiveValues()[0];
				estimateGradient(tmp0, tmp1);
				//更新spsa里面的parameter（属于[0,1]区间），同时更新第三个engine的参数
				updateParameters(i);
			}
		}
		dismissAllWorkingThreads();//stop eng线程。
	}
//	public static void main(String[] args) {
//		int maxGeneration = 2000;
//		int maxTasks = 100;
//		TaskCenter tc = new TaskCenter(maxTasks);
//		float[] plower = new float[]{18.0f,0.15f,1.0f,5.0f,25,85};
//		float[] pupper = new float[]{23.0f,0.17f,4.0f,8.0f,35,95};//,180.0f,25,40,100};
//		float[] pinit = new float[]{21.95f,0.156f,1.61f,6.31f,30.48f,91.44f};
//        SPSA spsa = new SPSA(6,"SPSA",tc);
//        //Spall建议20，100，0.602，1.9，0.101
//        spsa.setAlgParameters(0.5, 50, 0.602, 0.1, 0.101);
//        spsa.setBounderies(plower, pupper);
//        spsa.setParameters(pinit);
//		spsa.setMaxGeneration(maxGeneration);
//		spsa.start();
//		MLPEngThread mlp_eng_thread;
//		for (int i = 0; i < 3; i++) {
//			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
//			mlp_eng_thread.setMode(3);
////			((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
//			mlp_eng_thread.start();
//		}
//	}
}
