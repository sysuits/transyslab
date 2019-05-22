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

public class FunsCombination1 extends Function{

	QSDFun_Eq qsdFun_eq;
	KSDM_Eq ksdM_eq;
	
	public FunsCombination1() {
		paras = new double[5];//[0]k [1]Qm_ob [2]VF [3]Kj [4]Kstar
		qsdFun_eq = new QSDFun_Eq();
		ksdM_eq = new KSDM_Eq();
	}
	
	public FunsCombination1(double k, double Qm_ob, double VF, double Kj, double Kstar) {
		paras = new double[] {k, Qm_ob, VF, Kj, Kstar};
		qsdFun_eq = new QSDFun_Eq(k, Qm_ob, VF, Kj);
		ksdM_eq = new KSDM_Eq(Kj, Kstar);
	}
	
	public void setParas(double k, double Qm_ob, double VF, double Kj, double Kstar) {
		paras[0] = k;
		paras[1] = Qm_ob;
		paras[2] = VF;
		paras[3] = Kj;
		paras[4] = Kstar;
		qsdFun_eq.setParas(k, Qm_ob, VF, Kj);
		ksdM_eq.setParas(Kj, Kstar);
	}
	
	public void setCondition (double k, double Qm_ob, double Kstar) {
		paras[0] = k;
		paras[1] = Qm_ob;
		paras[4] = Kstar;
		qsdFun_eq.setPt(k, Qm_ob);
		ksdM_eq.setKstar(Kstar);
	}

	@Override
	public double[] cals(double[] inputs) {//inputs: [0]alpha; [1]beta
		return new double[] {qsdFun_eq.cal(inputs), ksdM_eq.cal(inputs)};
	}

	@Override
	public double cal(double[] inputs) {
		// TODO Auto-generated method stub
		return  Math.pow(qsdFun_eq.cal(inputs), 2)+Math.pow(ksdM_eq.cal(inputs), 2);
	}
	
}
