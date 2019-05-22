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

package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Node;

public class Dynamics {
	protected MLPLink link;
	protected MLPParameter mlpParameter;
	public double [] linkCharacteristics;//[0]vf_SD; [1]VMin; [2]KJam; [3]Alpha; [4]Beta; [5]vf_CF
	//public double [] cfPara;//[0]Critical Gap; [1]dUpper;
	public Dynamics(MLPLink theLink){
		link = theLink;
		mlpParameter = (MLPParameter) theLink.getNetwork().getSimParameter();
		linkCharacteristics = new double[6];//((MLPParameter) theLink.getNetwork().getSimParameter()).getSDPara();
	}
	public Dynamics(MLPLink theLink, double [] SDParas) {
		link = theLink;
		mlpParameter = (MLPParameter) theLink.getNetwork().getSimParameter();
		linkCharacteristics = SDParas;
	}
	public double sdFun(double k) {
		if (k <= linkCharacteristics[2]){
			 //vMin + (vMax - vMin)*(1-(K/kJam).^a).^b;
			double ans = linkCharacteristics[1] + (linkCharacteristics[0]- linkCharacteristics[1]) * Math.pow(1.0-Math.pow(k/ linkCharacteristics[2], linkCharacteristics[3]), linkCharacteristics[4]);
//			if (Double.isNaN(ans)){
//				System.out.println("BUG SD函数输出异常");
//			}
			return ans;
		}
		else {
			return 0.0;
		}
	}
	public double cfFun(MLPVehicle theVeh){
		double gap = theVeh.leading.Displacement() - theVeh.leading.getLength() - theVeh.Displacement();
		if (gap <= 0)
			System.out.println("DEBUG: car crush");
		double vlead = (double) theVeh.leading.getCurrentSpeed();
		double upperGap = mlpParameter.CF_FAR;
		if(gap < mlpParameter.CF_NEAR) {
			return vlead;
		}
		else if (gap<upperGap) {
			double r = gap/upperGap;
			//return r * linkCharacteristics[0] + (1.0-r) * vlead;
			double ans = r * mlpParameter.maxSpeed(gap) + (1.0-r) * vlead;
//			if (Double.isNaN(ans))
//				System.out.println("BUG CF函数输出异常");
			return ans;
		}
		else if (ExpSwitch.CF_CURVE) {
			double l = ExpSwitch.CF_VT_END, vt = ExpSwitch.CF_VT;
			double r = Math.min((gap-upperGap)/l,1.0);
			return r*vt + (1.0-r)*linkCharacteristics[5];
		}
		else {
			return linkCharacteristics[5];
		}
	}
	public double updateHeadSpd(MLPVehicle headVeh){
		MLPLane nextLane = headVeh.lane.connectedDnLane;
		if (headVeh.getDistance() < MLPParameter.SEG_NEAR &&
				nextLane != null && (!nextLane.enterAllowed || !nextLane.checkVolum(headVeh))) {
			//过于接近seg末端 且 下游seg容量已满， 需停车等待
			return 0.0;
		}
		
		if (headVeh.leading != null) {
			return cfFun(headVeh);
		}
		else if (((MLPSegment)headVeh.link.getEndSegment()).endDSP - headVeh.Displacement() >
						mlpParameter.CELL_RSP_UPPER ||
				headVeh.segment.isTheEnd() != 0) {
			//距离终点较远 或 处于endSegment(无限制)
			return linkCharacteristics[5];
		}
		else {
			//准备处于Link Passing的头车
			Node dnNode = headVeh.getLink().getDnNode();
			if (ExpSwitch.APPROACH_CTRL || dnNode.type(Constants.NODE_TYPE_INTERSECTION)!=0) {
				//接近路口减速通过
				double passingSpd = ((MLPNode)dnNode).getPassSpd();
				double gap = ((MLPSegment)headVeh.link.getEndSegment()).endDSP - headVeh.Displacement();
				double upperGap = mlpParameter.CELL_RSP_UPPER;
				double r = gap / upperGap;
				return r * mlpParameter.maxSpeed(gap) + (1.0-r) * passingSpd;
			}
			else
				return linkCharacteristics[5];

		}
	}

	public void setPartialCharacteristics(double[] para, int mask) {
		int idx = 0;
		for (int i = 0; i < 6; i++) {
			if ((1<<i & mask) != 0) {
				if (idx < para.length) {
					linkCharacteristics[i] = para[idx];
					idx += 1;
				}
				else
					System.err.println("idx越界");
			}
		}
	}

	public double getFreeFlow() {
		return (linkCharacteristics[0] + linkCharacteristics[5])/2.0;
	}
}
