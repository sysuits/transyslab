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

public class KSDM_Eq extends Function{
	
	KSDMax ksd;
	
	public KSDM_Eq() {
		paras = new double [2];//[0]Kj; [1]Ksdmax_star
		ksd = new KSDMax();
	}
	
	public KSDM_Eq(double Kj, double Kstar) {
		paras = new double[] {Kj, Kstar};
		ksd = new KSDMax();
		ksd.setKj(Kj);
	}
	
	public void setKstar(double Kstar) {
		paras[1] = Kstar;
	}
	
	public void setParas(double Kj, double Kstar) {
		paras[0] = Kj;
		paras[1] = Kstar;
		ksd.setKj(Kj);
	}

	@Override
	public double cal(double[] inputs) {//[0]alpha; [1]beta
		return ksd.cal(inputs)-paras[1];
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}
}
