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
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;

import static java.util.stream.Collectors.toList;

public class MLPSegment extends Segment{
	public double startDSP;//在当前link中的起点里程
	public double endDSP;//当前link中的segment终点里程
	
	public MLPSegment() {
		startDSP = 0;
		endDSP = 0;
		lanes = new ArrayList<>();
		//laneIdxs_ = new int[nLanes];//不能在此实例化laneIdxsx此时nLanes_的值未被计算
	}
	
	@Override
	public MLPSegment getUpSegment() {
		return (MLPSegment) super.getUpSegment();
	}
	
	@Override
	public MLPSegment getDnSegment(){
		return (MLPSegment) super.getDnSegment();
	}
	
	public boolean isEndSeg() {
		return link.getEndSegment().equals(this);
	}

	public boolean isStartSeg() {
		return link.getStartSegment().equals(this);
	}

	public void setSucessiveLanes() {
		MLPSegment dnSeg = getDnSegment();
		if (dnSeg != null) {
			dealSuccessive(dnSeg);
			return;
		}
		int ndnLinks = link.nDnLinks();
		if (ndnLinks > 0) {
			for (int i = 0; i < ndnLinks; i++) {
				MLPSegment startSeg = (MLPSegment) link.dnLink(i).getStartSegment();
				dealSuccessive(startSeg);
			}
		}
	}

	private boolean checkConnected(MLPSegment dnSeg) {
		boolean ans = false;
		for (int i = 0; i < nLanes() && (!ans); i++) {
			for (int j = 0; j < dnSeg.nLanes() && (!ans); j++) {
				ans |= getLane(i).successiveDnLanes.contains(dnSeg.getLane(j));
			}
		}
		return ans;
	}

	private void dealSuccessive(MLPSegment dnSeg) {
		//若xml中有此信息，则初始化过程已添加，不需要进行推断。要求将该seg所有lane关于dnSeg的successiveDnLane全部指定好。
		if (checkConnected(dnSeg))
			return;
		int nLanes = nLanes();
		if (nLanes == dnSeg.nLanes()) {
			for (int i = 0; i < nLanes; i++) {
				getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
				dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
			}
		}
		else {
			int m = Math.min(nLanes, dnSeg.nLanes());
			double sumLF = 0.0, sumLFSquared = 0.0, sumRT = 0.0, sumRTSquared = 0.0;
			for (int i = 0; i < m; i++) {
				/*
				double tmpLF = getLane(i).getEndPnt().distanceSquared(dnSeg.getLane(i).getStartPnt());
				double tmpRT = getLane(nLanes -1-i).getEndPnt().distanceSquared(dnSeg.getLane(dnSeg.nLanes()-1-i).getStartPnt());
				sumLFSquared += tmpLF;
				sumLF += Math.sqrt(tmpLF);
				sumRTSquared += tmpRT;
				sumRT += Math.sqrt(tmpRT);*/
			}
//			double coefVarLF = Math.sqrt(sumLFSquared/m - Math.pow(sumLF/m, 2))/(sumLF/m);
//			double coefVarRT = Math.sqrt(sumRTSquared/m - Math.pow(sumRT/m, 2))/(sumRT/m);
//			if (coefVarLF <= coefVarRT) {
			//采用新的successiveDnlanes推断方法
			if (sumLF <= sumRT) {
				for (int i = 0; i < m; i++) {
					getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
					dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
				}
			}
			else {
				for (int i = 0; i < m; i++) {
					getLane(nLanes -1-i).successiveDnLanes.add(dnSeg.getLane(dnSeg.nLanes()-1-i));
					dnSeg.getLane(dnSeg.nLanes()-1-i).successiveUpLanes.add(getLane(nLanes -1-i));
				}
			}
		}
	}

	@Override
	public MLPLane getLane(int index) {
		return (MLPLane) super.getLane(index);
	}

	protected List<Lane> getValidLanes(MLPVehicle veh){
		/*List<MLPLane> ans = new ArrayList<>();
		for(Lane LN: lanes){
			if (((MLPLane) LN).enterAllowed){
				ans.add((MLPLane) LN);
			}
		}
		return ans;*/
		//TODO 考虑不同车种的通行条件，例如公交专用道、HOV
		return lanes.stream().filter(l -> ((MLPLane) l).enterAllowed).collect(toList());
	}
}
