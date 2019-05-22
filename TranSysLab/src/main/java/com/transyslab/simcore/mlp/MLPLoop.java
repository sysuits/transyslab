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

import java.util.*;
import java.util.stream.Collectors;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Loop;
import com.transyslab.roadnetwork.Sensor;

public class MLPLoop extends Loop{
	public static final int SPEED = 1;
	public static final int PASSING_TIME = 2;
	public static final int HEADWAY = 3;
	String detName;
	MLPSegment segment;
	MLPLink link;
	double displacement;
	double distance;
	HashMap<Integer, Double> enterMap;
	LinkedList<double[]> records;
	
	public MLPLoop(){
		enterMap = new HashMap<>();
		records = new LinkedList<>();
	}
	public MLPLoop(MLPLane LN, MLPSegment Seg, MLPLink LNK, String name, double dsp, double present){
		this.lane = LN;
		lane = LN;
		segment = Seg;
		link = LNK;
		displacement = dsp;
		position = present;
		detName = name;
		distance = Seg.endDSP - dsp;
		enterMap = new HashMap<>();
		records = new LinkedList<>();
		this.createSurface();
	}
	public String detect(double timeNow){//当处于seg边界上存在漏洞(已修复)
		String str = "";
		String timeStr = String.format("%.1f", timeNow);
		//更新车头进入记录
		for (MLPVehicle veh : ((MLPLane) lane).vehsOnLn) {
			if (veh.virtualType == 0 &&
					veh.Displacement() < displacement &&
					veh.segment.endDSP - veh.newDis >= displacement) {
				enterMap.put(veh.getId(),timeNow);
			}
		}
		//更新车尾离开记录
		for (MLPVehicle veh : ((MLPLane) lane).vehsOnLn) {
			if (veh.virtualType == 0 &&
					veh.Displacement() - veh.getLength() < displacement &&
					veh.segment.endDSP - veh.newDis - veh.getLength() >= displacement) {
				Double timeEnter = enterMap.get(veh.getId());
				if (timeEnter == null) {
					List<Sensor> loops = getLink().getNetwork().getSurvStations();
					for (Sensor l : loops) {
						if (l.getName().equals(this.getName()))
							timeEnter = ((MLPLoop)l).enterMap.get(veh.getId());
						if (timeEnter!=null) {
							((MLPLoop)l).enterMap.remove(veh.getId());
							break;
						}
					}
					if (timeEnter==null) {
//						System.out.println("Error: no enter record.");
						continue;
					}
				}
				else
					enterMap.remove(veh.getId());
				//双线圈
				double recordSpd = ExpSwitch.DOUBLE_LOOP ?
										(timeNow == timeEnter ? veh.newSpeed : veh.getLength() / (timeNow-timeEnter)) :
										veh.newSpeed;
				records.add(new double[] {timeNow, recordSpd});
				str +=  detName + "," +
						timeStr + "," +
						veh.getId() + "," +
						veh.virtualType + "," +
						recordSpd + "," +
						((MLPLane) lane).getLnPosNum() + "," +
						link.getId() + "," +
						displacement + "\r\n";
			}
		}
		return str;
	}
/*	public double harmmeanNClear() {
		if (detectedSpds.isEmpty()) {
			return 0.0;
		}
		else {
			double n = detectedSpds.size();
			double sum = 0.0;
			while (!detectedSpds.isEmpty()) {
				sum += 1.0 / detectedSpds.poll();
			}
			return (n/sum);
		}
	}*/
	public double calPeriodAvgSpd(double ftime, double ttime){
		double sum = 0.0, count = 0.0;
		for (double[] line : records) {
			if (line[0]>ftime && line[0]<=ttime) {
				count += 1;
				sum += line[1];
			}
		}
		if (count>0)
			return sum/count;
		else
			return 0.0;
	}
	public List<Double> getPeriodSpds(double ftime, double ttime, boolean dumpAfter){
		List<Double> ans = new ArrayList<>();
		records.stream().filter(l -> l[0]>ftime && l[0]<=ttime).forEach(r -> ans.add(r[1]));
		if (dumpAfter)
			records.removeAll(ans);
		return ans;
	}
	public List<Double> getPeriodPassingTime(double fTime, double tTime) {
		List<Double> ans = new ArrayList<>();
		records.stream().filter(l -> l[0]>fTime && l[0]<=tTime)
				.sorted(new Comparator<double[]>() {
					@Override
					public int compare(double[] o1, double[] o2) {
						return o1[0]>o2[0] ? 1 : (o1[0]<o2[0] ? -1 : 0);
					}
				})
				.forEach(r -> ans.add(r[0]));
		return ans;
	}
	public List<Double> getPeriodHeadway(double fTime, double tTime) {
		List<Double> ans = new ArrayList<>();
		List<Double> candidates = getPeriodPassingTime(fTime, tTime);
		if (candidates.size()>1) {
			for (int i = 0; i < candidates.size(); i++) {
				ans.add(candidates.get(i+1) - candidates.get(i));
			}
		}
		return ans;
	}
	public List<Double> getPeriod(double fTime, double tTime, int type) {
		switch (type) {
			case SPEED : return getPeriodSpds(fTime,tTime,false);
			case PASSING_TIME : return getPeriodPassingTime(fTime,tTime);
			case HEADWAY :return getPeriodHeadway(fTime,tTime);
			default: return null;
		}
	}
	public double countPeriodFlow(double ftime, double ttime){
		/*double sum = 0.0;
		for (double[] line : records) {
			if (line[0]>ftime && line[0]<=ttime) {
				sum += 1.0;
			}
		}
		return sum;*/
		return records.stream().filter(l -> l[0]>ftime && l[0]<=ttime).count();
	}
	protected void clearRecords() {
		enterMap.clear();
		records.clear();
	}
	public String getName(){
		return detName;
	}
	public LinkedList<double[]> getRecords(){
		return records;
	}
}
