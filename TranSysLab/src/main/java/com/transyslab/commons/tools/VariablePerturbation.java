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

import org.apache.commons.math3.distribution.BinomialDistribution;


public class VariablePerturbation {
	public static double[] pertubate(double[] lower, double[] upper, double step,double[] variable){
		if(lower.length!=upper.length || lower.length!=variable.length){
			System.out.println("Pertubate variables fail, check the length of input arrays");
			return null;
		}
		int dim = variable.length;
		BinomialDistribution bd = new BinomialDistribution(1,0.5);
		int[] bdsamples = bd.sample(dim);
		double[] result = new double[dim];
		for(int i=0;i<dim;i++){
			if(bdsamples[i] == 0)
				bdsamples[i] = -1;
			result[i] = variable[i] + bdsamples[i]* step * variable[i];
		}
		return result;
	}
}
