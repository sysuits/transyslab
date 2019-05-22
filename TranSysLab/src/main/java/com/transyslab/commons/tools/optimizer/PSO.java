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


public class PSO {
	/**
	 * 粒子群
	 */
	protected Particle[] pars_;
	protected float gbest_fitness_;// 全局最优解
	protected int pcount_;// 粒子的数量
	protected int index_;
	protected int convergence_;
	protected float[] plower_;// 粒子位置的最小值
	protected float[] pupper_;// 粒子位置的最大值
	protected float[] vlower_;// 粒子搜索方向的最小值
	protected float[] vupper_;// 粒子搜索方向的最大值
	protected float[] gbest_;// 所有粒子找到的最好位置
	protected Random rnd_;
	protected int dims_;
	protected float w_; // 惯性权重
	protected float c1_; // 个体学习因子
	protected float c2_; // 全局学习因子
	protected int maxiter_; // 运行代数
	protected boolean[] firstTime_;

	public int getDim() {
		return dims_;
	}
	public int getPcount() {
		return pcount_;
	}
	public float getParaW() {
		return w_;
	}
	public float getParaC1() {
		return c1_;
	}
	public float[] getParaPl() {
		return plower_;
	}
	public float[] getParaPu() {
		return pupper_;
	}
	public float[] getParaVl() {
		return vlower_;
	}
	public float[] getParaVu() {
		return vupper_;
	}
	public void updateGbest(float[] pos) {
		for (int i = 0; i < dims_; i++) {
			gbest_[i] = pos[i];
		}
	}
	public Particle getParticle(int i) {
		return pars_[i];
	}
	public float getGbestFn() {
		return gbest_fitness_;
	}
	public void setGbestFn(float gf) {
		gbest_fitness_ = gf;
	}
	public boolean isConvergent(int times) {
		if (convergence_ <= times)
			return true;
		else
			return false;
	}
	public void updateConvergence(int i) {
		if (i != 0)
			convergence_++;
		else
			convergence_ = i;
	}
	public int getConvergence() {
		return convergence_;
	}
	public void updateParameter(int k) {
		w_ = (float) (0.1 * Math.cos(k * Math.PI / maxiter_) + 0.7);
		c1_ = (float) (0.75 * Math.cos(k * Math.PI / maxiter_) + 2);
		c2_ = (float) (-0.875 * Math.cos(k * Math.PI / maxiter_) + 1.375);
	}
	public void updateParaW(float wl, float wu, int k) {
		w_ = wu - wl * (k) / (pcount_);
	}
	/*
	 * public void init(int num, int dim, float c1, float c2, float w, float[]
	 * pl, float[] pu, float[] vl, float[] vu, int iter) { convergence_ = 0;
	 * pcount_ = num; gbest_fitness_ = 1e6f; pars_ = new Particle[pcount_];
	 * Particle.initStaticInfo(dim, pl, pu, vl, vu, c1, c2, w,iter); for (int i
	 * = 0; i < pcount_; i++) { pars_[i] = new Particle();
	 * pars_[i].initParticle(); } }
	 */
	public void initParticles(int pnum, int dim, float c1, float c2, float[] pl, float[] pu, float w, int iter) {

		convergence_ = 0;
		pcount_ = pnum;
		gbest_fitness_ = 1e6f;
		dims_ = dim;

		// int indexInLinks = -1;
		pars_ = new Particle[pcount_];
		plower_ = pl;
		pupper_ = pu;
		vlower_ = new float[dim];
		vupper_ = new float[dim];
		gbest_ = new float[dim];
		firstTime_ = new boolean[pnum];
		for (int i = 0; i < dim; i++) {
			vupper_[i] = (float) (0.2 * (pupper_[i] - plower_[i]));
			vlower_[i] = -vupper_[i];
		}

		w_ = w;
		c1_ = c1;
		c2_ = c2;

		for (int i = 0; i < pcount_; i++) {
			firstTime_[i] = true;
			pars_[i] = new Particle(dim);
			pars_[i].init(plower_, pupper_, vlower_, vupper_);
		}
	}
	public void initPc() {
		for (int i = 0; i < pcount_; i++) {
			double tmp = (5 * (double) i / ((double) (pcount_) - 1));
			pars_[i].pc_ = (float) (0.00 + (0.5 * (Math.exp(tmp) - 1) / (Math.exp(5.0) - 1)));
		}
	}
	public void posToLearn(int pi) {
		Random rnd = new Random();
		int pi1, pi2, pi3;// 需要学习的粒子索引
		int tmp = rnd.nextInt(dims_);// 需要学习的粒子的随机维
		pi1 = rnd.nextInt(pcount_);
		pi2 = rnd.nextInt(pcount_);
		int counter = 0;
		if (pars_[pi].flag_ >= 5 || firstTime_[pi] == true) {
			firstTime_[pi] = false;
			for (int d = 0; d < dims_; d++) {

				if (rnd.nextFloat() > 1 - pars_[pi].pc_) {

					if (pars_[pi1].pbest_fitness_ > pars_[pi2].pbest_fitness_) {
						// 粒子pi第d维向粒子pi2第d维学习
						pars_[pi].pbestToLearn_[d] = pars_[pi2].pbest_[d];
					}
					else {
						pars_[pi].pbestToLearn_[d] = pars_[pi1].pbest_[d];
					}
					counter++;
				}
				if (d == (dims_ - 1) && counter == 0) {
					if (pars_[pi1].pbest_fitness_ >= pars_[pi2].pbest_fitness_) {
						// 粒子pi第d维向粒子pi2第d维学习
						pars_[pi].pbestToLearn_[tmp] = pars_[pi2].pbest_[tmp];
					}
					else {
						pars_[pi].pbestToLearn_[tmp] = pars_[pi1].pbest_[tmp];
					}
					/*
					 * if(tmp==d){ pi3 = rnd.nextInt(pcount_);
					 * pars_[pi].pbestToLearn_[d] = pars_[pi3].pbest_[d]; }
					 * else{ pars_[pi].pbestToLearn_[d] = pars_[pi].pbest_[d]; }
					 */
					// pars_[pi].pbestToLearn_[d] = pars_[pi].pbest_[d];

				}

			} /*
				 * else{
				 *
				 * if(tmp==d){ pi3 = rnd.nextInt(pcount_);
				 * pars_[pi].pbestToLearn_[d] = pars_[pi3].pbest_[d]; } else{
				 * pars_[pi].pbestToLearn_[d] = pars_[pi].pbest_[d]; }
				 * pars_[pi].pbestToLearn_[d] = pars_[pi].pbest_[d]; }
				 */
			pars_[pi].flag_ = 0;
		}
	}
	public void showresult() {
		System.out.println("程序求得的最优解是" + gbest_fitness_);
		System.out.println("每一维的值是");
		for (int i = 0; i < dims_; i++) {
			System.out.println(gbest_[i]);
		}
	}
}
