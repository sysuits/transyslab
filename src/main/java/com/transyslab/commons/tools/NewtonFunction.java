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

package com.transyslab.commons.tools;


public interface NewtonFunction {
	double calculate(double input, double[] paras);
	default double findRoot(double start, double[] paras){
		double epsilon = 1e-12;
		double maxIteration = 1e6;
		double x = start;
		double f = this.calculate(x,paras);
		int iterTimes = 0;
		while (Math.abs(f)>epsilon) {
			if (iterTimes>maxIteration) {
				System.err.println("方程不收敛");
				return Double.NaN;
			}
			x = x - f*epsilon/(this.calculate(x+epsilon,paras)-f);//迭代x(k+1)
			f = this.calculate(x,paras);
			iterTimes += 1;
		}
		if (x < 0 || Double.isNaN(f)) {
			return findRoot(start*10,paras);
		}
		return x;
	}
}