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

public class QSDFun extends Function{	
	
	public QSDFun() {
		paras = new double[4];//[0]VF, [1]Kj, [2]alpha, [3]beta
	}
	
	public void setParas(double VF, double Kj, double alpha, double beta) { 
		paras[0] = VF;
		paras[1] = Kj;
		paras[2] = alpha;
		paras[3] = beta;
	}
	
	public void setAB(double alpha, double beta) {
		paras[2] = alpha;
		paras[3] = beta;
	}

	@Override
	public double cal(double[] inputs) {
		if (inputs.length!=1) 
			return Double.NaN;
		return paras[0]*Math.pow(1-Math.pow(inputs[0]/paras[1], paras[2]), paras[3]);
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}
}
