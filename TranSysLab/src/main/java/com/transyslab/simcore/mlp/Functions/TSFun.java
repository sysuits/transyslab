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

public class TSFun extends Function{
	public TSFun(){//QM,VF,KJ,VP,DetaT
		paras = new double[5];
	}
	public void setParas(double[] obsParas, double detaT, double VP){
		for(int i=0;i<obsParas.length;i++){
			this.paras[i] = obsParas[i];
		}
		paras[3] = VP;
		paras[4] = detaT;
	}
	@Override
	public double cal(double[] inputs) {//XC
		if(inputs.length!=1)
			return Double.NaN;
		if(0<inputs[0] && inputs[0]<=paras[1]/paras[0])
			return 1/paras[0]-1/(paras[0]*paras[1])-paras[4];
		else if(paras[1]/paras[0]<inputs[0] && inputs[0]<=paras[3]/paras[0])
			return 1/paras[0]-1/(paras[0]*paras[2]*inputs[0])-paras[4];
		else 
			return Double.NaN;
			
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}
}
