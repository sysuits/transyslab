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
import java.util.Random;

import com.transyslab.roadnetwork.Constants;


public class Particle {
	protected float[] pos_;// 粒子的位置，求解问题多少维，则此数组为多少维
	protected float[] v_;// 粒子的速度，维数同位置
	protected double fitness_;// 粒子的适应度
	protected float[] pbest_;// 粒子自身的历史最好位置
	protected float[] pbestToLearn_;// 粒子需要学习的最好位置
	protected int flag_;// pbest保持不变的代数，即已有几代没找到更好位置
	protected int[] iToLearn_; // 粒子每一维需要学习的其它粒子的索引
	// protected static MesoRandom rnd_;
	protected boolean feasible_;
	protected double pbest_fitness_;// 历史最优解
	protected float pc_;
	protected static int dims_;
	protected float[] mv_;

	public Particle(int dims) {
		iToLearn_ = new int[dims];
		dims_ = dims;
		pos_ = new float[dims];
		v_ = new float[dims];
		pbest_ = new float[dims];
		pbestToLearn_ = new float[dims];
		mv_ = new float[dims];
	}
	/*
	 * public static void updateGbest(float[] pos){ if(pos.length == dims_){ for
	 * (int i = 0; i < dims_; ++i) { gbest_[i] = pos[i]; } } else{
	 * System.out.println("error: check the dimension of the array"); }
	 *
	 * }
	 */
	public boolean isFeasible() {
		return feasible_;
	}
	public double getFitness() {
		return fitness_;
	}
	public float[] getPos() {
		return pos_;
	}
	public float getAlpha() {
		// return pos_[2];
		return pos_[0];
	}
	public float getBeta() {
		// return pos_[3];
		return pos_[1];
	}
	public float getDLower() {
		return pos_[0];
	}
	public float getDUpper() {
		return pos_[1];
	}
	public float getPC() {
		return pc_;
	}
	// 生成初始的随机位置，随机速度
	public void init(float[] pl, float[] pu, float[] vl, float[] vu) {
		Random rnd = new Random();
		// 初始为无穷大
		fitness_ = Constants.FLT_INF;
		pbest_fitness_ = Constants.FLT_INF;
		flag_ = 0;
		feasible_ = true;
		// pc_ = rnd.nextFloat();
		for (int i = 0; i < dims_; i++) {
			pos_[i] = pl[i] + rnd.nextFloat() * (pu[i] - pl[i]);
			pbest_[i] = pos_[i];
			v_[i] = vl[i] + 2 * rnd.nextFloat() * vu[i];
			mv_[i] = (float) (0.2 * (pu[i] - pl[i]));
		}
	}

	/**
	 * 评估函数值,同时记录历史最优位置
	 */

	public void evaSingleLinkFitness(int[] simflow, double[] simavgtime, int[] realflow, double[] realavgtime,
			float w) {
		if (simflow.length != simavgtime.length && simavgtime.length != realflow.length
				&& realflow.length != realavgtime.length) {
			System.out.println("Check the length of the input array");
		}
		else {
			double sumOfFlowError = 0;
			double sumOfTimeError = 0;
			// 时间间隔个数
			int n = simflow.length;
			for (int i = 0; i < n; i++) {
				sumOfFlowError = sumOfFlowError + Math
						.sqrt(((realflow[i] - simflow[i]) / realflow[i]) * ((realflow[i] - simflow[i]) / realflow[i]));
				sumOfTimeError = sumOfTimeError + Math.sqrt(((realavgtime[i] - simavgtime[i]) / realavgtime[i])
						* ((realavgtime[i] - simavgtime[i]) / realavgtime[i]));
			}
			// 一条link的误差函数
			fitness_ = w * sumOfFlowError + (1 - w) * sumOfTimeError;
			// 找到误差更小的解
			if (fitness_ < pbest_fitness_) {
				pbest_fitness_ = fitness_;
				for (int i = 0; i < dims_; ++i) {
					pbest_[i] = pos_[i];
				}
			}
		}

	}
	public void evaTotalLinkFitness(int[][] simflow, float[][] simavgtime, int[][] realflow, float[][] realavgtime,
			float w) {
		// 列为不同时间，行为不同link
		int col = simflow[0].length;
		int row = simflow.length;
		double sumOfLinkFlowError;
		double sumOfLinkTimeError;
		double sumError = 0;
		for (int j = 0; j < col; j++) {
			sumOfLinkFlowError = 0;
			sumOfLinkTimeError = 0;
			for (int i = 0; i < row; i++) {
				if (realflow[i][j] == 0)
					realflow[i][j] = 1;
				sumOfLinkFlowError = sumOfLinkFlowError + ((realflow[i][j] - simflow[i][j]) / realflow[i][j])
						* ((realflow[i][j] - simflow[i][j]) / realflow[i][j]);
				sumOfLinkTimeError = sumOfLinkTimeError + ((realavgtime[i][j] - simavgtime[i][j]) / realavgtime[i][j])
						* ((realavgtime[i][j] - simavgtime[i][j]) / realavgtime[i][j]);
			}
			sumError = sumError + w * Math.sqrt(sumOfLinkFlowError) + (1 - w) * Math.sqrt(sumOfLinkTimeError);
		}
		fitness_ = sumError / (Math.sqrt(row));
		// 找到误差更小的解
		if (fitness_ < pbest_fitness_) {
			// 更新粒子的历史最优
			pbest_fitness_ = fitness_;
			for (int i = 0; i < dims_; ++i) {
				pbest_[i] = pos_[i];
			}
		}
	}
	public void evaMRE(int[][] simflow, double[][] simavgtime, int[][] realflow, double[][] realavgtime, double w) {
		// 列为不同时间，行为不同link
		int col = simflow[0].length;
		int row = simflow.length;
		double sumOfLinkFlowError;
		double sumOfLinkTimeError;
		double sumError = 0;
		for (int j = 0; j < col; j++) {
			sumOfLinkFlowError = 0;
			sumOfLinkTimeError = 0;
			for (int i = 0; i < row; i++) {
				if (realflow[i][j] == 0)
					realflow[i][j] = 1;
				sumOfLinkFlowError = sumOfLinkFlowError + (Math.abs(realflow[i][j] - simflow[i][j])) / realflow[i][j];
				sumOfLinkTimeError = sumOfLinkTimeError
						+ (Math.abs(realavgtime[i][j] - simavgtime[i][j])) / realavgtime[i][j];
			}
			sumError = sumError + w * (sumOfLinkFlowError) + (1 - w) * (sumOfLinkTimeError);
		}
		fitness_ = sumError / col;
		// 找到误差更小的解
		if (fitness_ < pbest_fitness_) {
			// 更新粒子的历史最优
			pbest_fitness_ = fitness_;
			flag_ = 0;
			for (int i = 0; i < dims_; ++i) {
				pbest_[i] = pos_[i];
			}
		}
		else {
			flag_++;
		}
	}

	/**
	 * 更新速度和位置
	 */
	public void updateVel(float w, float c1, float[] pl, float[] pu, float[] vl, float[] vu) {
		Random rnd = new Random();
		int isdimfeasible = 0;
		for (int i = 0; i < dims_; i++) {
			v_[i] = w * v_[i] + c1 * rnd.nextFloat() * (pbestToLearn_[i] - pos_[i]);
			if (v_[i] > mv_[i]) {
				v_[i] = mv_[i];
			}
			if (v_[i] < -mv_[i]) {
				v_[i] = -mv_[i];
			}
			pos_[i] = pos_[i] + v_[i];
			if (pos_[i] > pu[i] || pos_[i] < pl[i]) {
				isdimfeasible++;
			}
		}
		if (isdimfeasible > 0)
			feasible_ = false;
		else
			feasible_ = true;
	}
}
