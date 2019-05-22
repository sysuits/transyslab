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

package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class QSDMax extends Function{
	
	public QSDMax() {
		paras = new double[2];//[0]VF [1]Kj
	}
	
	public QSDMax(double VF, double Kj) {
		paras = new double[] {VF, Kj};
	}
	
	public void setParas(double VF, double Kj) {
		paras[0] = VF;
		paras[1] = Kj;
	}

	@Override
	public double cal(double[] inputs) {//[0]alpha; [1]beta
		if (inputs.length!=2) 
			return Double.NaN;
		double tmp = inputs[0]*inputs[1];
		return ( paras[0] * paras[1] * Math.pow(tmp/(1+tmp), inputs[1]) ) / Math.pow(1+tmp, 1/inputs[0]);
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}

}
