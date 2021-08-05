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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class JointLane {
	protected int jlNum;
	protected List<MLPLane> lanesCompose;
	public double stPtDsp;
	public double endPtDsp;

	public JointLane(){
		jlNum = 0;
		lanesCompose = new ArrayList<MLPLane>();
	}

	public JointLane(int num){
		jlNum = num;
		lanesCompose = new ArrayList<MLPLane>();
	}

	public boolean hasNoVeh(boolean virtualCount){
		ListIterator<MLPLane> iterator = lanesCompose.listIterator();
		while (iterator.hasNext()) {
			MLPLane LN = iterator.next();
			if (!LN.vehsOnLn.isEmpty()){
				if (!virtualCount) {
					ListIterator<MLPVehicle> VehIterator = LN.vehsOnLn.listIterator();
					while (VehIterator.hasNext()) {
						if (VehIterator.next().virtualType == 0) {
							return false;
						}
					}
				}
				else
					return false;
			}
		}
		return true;
	}

	public MLPVehicle getFirstVeh() {
		ListIterator<MLPLane> iterator = lanesCompose.listIterator();
		while (iterator.hasNext()) {
			MLPLane lane = iterator.next();
			if (!lane.vehsOnLn.isEmpty()){
				return lane.getHead();
			}
		}
		return null;
	}

	public double getJointLaneEndDSP() {
		return lanesCompose.get(0).getSegment().endDSP;
	}

	public double getJointLaneStartDSP() {
		return lanesCompose.get(lanesCompose.size()-1).getSegment().endDSP;
	}
}
