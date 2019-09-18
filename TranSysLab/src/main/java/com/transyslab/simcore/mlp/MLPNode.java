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

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.*;

import java.util.*;
import java.util.stream.Collectors;

public class MLPNode extends Node{
	public static double NODE_ALPHA = 1.0;
	public static double NODE_BETA = 1.0;
	public static double NODE_PASS_SPD = 80.0/3.6;
	private LinkedList<MLPVehicle> statedVehs;
	protected double passSpd = 40.0/3.6;
	public HashMap<String, List<double[]>> signalTable;
	public HashMap<String, List<String>> confilctDirs;
	public int stopCount;
	public HashMap<String, List<MLPConnector>> turningMap;
	public MLPNode() {
		statedVehs = new LinkedList<>();
		stopCount = 0;
		turningMap = new HashMap<>();
		signalTable = new HashMap<>();
		confilctDirs = new HashMap<>();
	}
	public int serve(MLPVehicle veh) {
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		double currentTime = veh.link.getNetwork().getSimClock().getCurrentTime();

		//if this is an intersection, deal with inner movements
		if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {
			//trip finished?
			if (veh.getNextLink() == null)
				return dump(veh);
			if (intersectionPass(currentTime,veh.getLink().getId(),veh.getNextLink().getId())) {
				//innermovement
				List<MLPConnector> conns = lane_.connsToDnLink( veh.getNextLink().getId());
				if (conns.size()<=0){
//					System.out.println("warning: veh no. " + veh.getId() + " stop to wait for lane changing at time " + currentTime);
					veh.holdAtDnEnd();
					return Constants.VEHICLE_NOT_RECYCLE;
				}
				conns.sort(new Comparator<MLPConnector>() {
					@Override
					public int compare(MLPConnector o1, MLPConnector o2) {
						double d1 = Math.abs(o1.dnLane.getLnPosNum()-o1.upLane.getLnPosNum());
						double d2 = Math.abs(o2.dnLane.getLnPosNum()-o2.upLane.getLnPosNum());
						return Double.compare(d1,d2);
//						double delta = o1.getLength() - o2.getLength();
//						return delta < 0 ?
//								-1 :
//								delta > 0 ? 1 : 0;
					}
				});
				for (int i = 0; i < conns.size(); i++) {
					MLPConnector theConn = conns.get(i);
					MLPLane nextLane = theConn.dnLane;
					if (theConn.checkVolume(veh)){
						//todo 逻辑存在漏洞 时间步长过大,多车辆穿越时需重新设计
						stashVeh(veh,nextLane,theConn);
						veh.conn = theConn;
						theConn.vehsOnConn.offer(veh);
						return Constants.VEHICLE_NOT_RECYCLE;
					}
				}
			}
			return holdTheVeh(veh);
		}

		//deal with non-intersection nodes
		if((!ExpSwitch.CAP_CTRL) || lane_.checkPass()){
			//passed output capacity checking
			//trip finished?
			if (veh.getNextLink() == null)
				return dump(veh);
//			List<MLPLane> candidates = lane.selectDnLane(veh.getNextLink().getStartSegment());//不以successiveDnLane运行
			MLPLane nextLane = lane_.successiveDnLaneInLink((MLPLink) veh.getNextLink());
			if (nextLane != null) {// at least one topology available down lane
				if (nextLane.checkVolum(veh.getLength(),0.0)) {//check every down lane' volume //before: checkVolum(veh)
					//no priority control for now
					boolean canpass = true;
//						for (int j = 0; j < nextLane.nUpLanes() && canpass; j++) { //不以successiveDnLane运行
//							MLPLane confLane = (MLPLane) nextLane.upLane(j); //不以successiveDnLane运行
					for (int j = 0; j < nextLane.successiveUpLanes.size() && canpass; j++) {
						MLPLane confLane = nextLane.successiveUpLanes.get(j);
						canpass &= confLane.getId() == lane_.getId() ||
								confLane.vehsOnLn.isEmpty() ||
								// lane.priority > confLane.priority || //路权较大时可以直接通过
								!need2Giveway(veh, confLane.vehsOnLn.get(0)) ||
								reachFirst(veh, confLane.vehsOnLn.get(0));//同等路权下先到先得，加入路权后替换成下面的代码
						//(lane.priority == confLane.priority && reachFirst(veh, confLane.vehsOnLn.get(0))
					}
					if (canpass && !checkPlaceTaken(veh, nextLane)) {//pass to this very nexlane
						double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 故为+
						stashVeh(veh,nextLane,null);
						veh.shownUpLane = nextLane;
						return Constants.VEHICLE_NOT_RECYCLE;
					}
//					else
//						System.out.println("BUG Can NOT pass or has been taken place");
				}
//				else
//					System.out.println("BUG Failed checking volume");
			}
//			else
//				System.out.println("BUG Error Next Lane is null");
		}
		else //can not pass capacity ctrl
			stopCount += 1;
		return holdTheVeh(veh);
	}
	private int holdTheVeh(MLPVehicle veh){
		veh.holdAtDnEnd();
		return Constants.VEHICLE_NOT_RECYCLE;
	}
	private int dump(MLPVehicle veh){
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		double currentTime = veh.link.getNetwork().getSimClock().getCurrentTime();
		lane_.scheduleNextEmitTime();//passed upstream lane
		//arrived destination no constrain
		//record linkTravelTime
		link_.tripTime.add(new double[] {veh.timeEntersLink(), veh.dspLinkEntrance, currentTime + veh.newDis/veh.newSpeed, veh.rvId});
		lane_.removeVeh(veh, true);
		return Constants.VEHICLE_RECYCLE;
	}
	private void transfer(MLPVehicle veh, double time){
		veh.initLinkEntrance(time, 0.0);
		veh.onRouteChoosePath(veh.link.getDnNode(),veh.link.getNetwork());
	}
	private boolean need2Giveway(MLPVehicle vehPass, MLPVehicle vehCheck) {		
		double dis_headway = vehCheck.getDistance() - vehPass.newDis - vehCheck.getCurrentSpeed();
		double crSpeed;
		double followerLen;
		if (dis_headway > 0) {
			crSpeed = vehPass.newSpeed;
			followerLen = vehCheck.getLength();
		}
		else {
			dis_headway *= -1;
			crSpeed = vehCheck.getCurrentSpeed();
			followerLen = vehPass.getLength();
		}
		MLPParameter mlpParameter = (MLPParameter) vehPass.link.getNetwork().getSimParameter();
		return dis_headway - followerLen < mlpParameter.minGap(crSpeed);
	}
	private boolean reachFirst(MLPVehicle vehPass, MLPVehicle vehCheck) {
		SimulationClock simClock = vehPass.link.getNetwork().getSimClock();
		double dis_headway = vehCheck.getDistance() - vehPass.newDis - vehCheck.getCurrentSpeed()*simClock.getStepSize();
		return dis_headway >= 0;
		//加入了checkPlaceTaken机制，可以不判断边界
//		if (dis_headway != 0) {
//			return dis_headway > 0;
//		}
//		else {
//			//边界问题，若相等，VID较大的等待 (或使用其他的随机规则；要求冲突两次查询结果一致)
//			return vehPass.getCode() < vehCheck.getCode();
//		}
		
	}
	private void stashVeh(MLPVehicle veh, MLPLane nextLane, MLPConnector conn) {
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		veh.addMileage(link_.length());
		SimulationClock simClock = veh.link.getNetwork().getSimClock();
		double currentTime = simClock.getCurrentTime();
		lane_.scheduleNextEmitTime();//passed upstream lane
		lane_.removeVeh(veh, false);
		//processingVeh.lane/seg/link setting
		veh.lane = nextLane;
		veh.segment = nextLane.getSegment();
		veh.link = (MLPLink) nextLane.getLink();
		double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 故为+
		if (conn!=null) {//交叉口运动
			veh.newDis += conn.getLength();
			if (veh.newDis < 0.0) {
				veh.newDis = 0.0;//每次最多经过一个link
				veh.newSpeed = (veh.getDistance() + conn.getLength()) / simClock.getStepSize();
				timeAtPoint = (veh.getDistance() + conn.getLength()) / veh.newSpeed;
			}
		}
		else {//节点运动，此处仅更新link离开时间和速度修正
			if (veh.newDis + nextLane.getLength() < 0.0) {
				veh.newDis =  - nextLane.getLength();
				veh.newSpeed = (veh.getDistance() + conn.getLength()) / simClock.getStepSize();
				timeAtPoint = currentTime + veh.newDis/veh.newSpeed;
			}
		}
		link_.tripTime.add(new double[] {veh.timeEntersLink(), veh.dspLinkEntrance, timeAtPoint, veh.rvId});//record linkTravelTime of old link
		statedVehs.add(veh);
	}
	protected void dispatchStatedVeh() {
		if (statedVehs.size() <= 0)
			return;
		SimulationClock simClock = getDnLink(0).getNetwork().getSimClock();
		double now = simClock.getCurrentTime();
		for (int i = 0; i < statedVehs.size(); i++) {
			MLPVehicle veh = statedVehs.get(i);
			if (veh.newDis<0.0) {
				double maxSpare = veh.lane.spareDis(veh.getLength(),veh.newSpeed);
				if (maxSpare < 0.0) {//等价于check volume
					veh.holdAtDnEnd();
					continue;
				}
				double timeAtPoint = now + veh.newDis/veh.newSpeed;//newDis<0 故为+
				veh.newDis += veh.getLane().getLength();
				double minAllowedNewDis = veh.getLane().getLength() - maxSpare;
				if (veh.newDis < minAllowedNewDis) {
					veh.newDis = minAllowedNewDis;//每次最多经过一个link，且不能碰撞lane上的尾车
					veh.newSpeed = (veh.getDistance() + maxSpare) / simClock.getStepSize();
					timeAtPoint = now - maxSpare / veh.newSpeed;
				}
				veh.initLinkEntrance(timeAtPoint, 0.0);
				veh.onRouteChoosePath(veh.link.getDnNode(),veh.link.getNetwork());
				//进入新link，更新强制换道值di
				veh.updateDi();
				veh.lane.appendVeh(veh);
				//todo: 危险 提前更新状态 为避免多车辆加载到下游道路时发生冲突 暂时处理
				veh.updateDynamics();

				if (veh.conn!=null)
					veh.conn.vehsOnConn.remove(veh);
				statedVehs.get(i).conn = null;
				statedVehs.remove(i);
				i -= 1;
			}
		}
	}
	protected boolean checkPlaceTaken(MLPVehicle veh, MLPLane nextLane){
		boolean ans = false;
		for (int i=0; i<statedVehs.size() && (!ans); i++){
			MLPVehicle v = statedVehs.get(i);
			if (v.lane.getId() == nextLane.getId()){
				double gap = veh.newDis + nextLane.getLength() - v.newDis - v.getLength();
				MLPParameter mlpParameter = (MLPParameter) veh.link.getNetwork().getSimParameter();
				ans |= ( gap < mlpParameter.minGap(veh.newSpeed) );
			}
		}
		return ans;
	}
	protected void clearStatedVehs() {
		statedVehs.clear();
		stopCount = 0;
	}
	protected void reset(){
		clearStatedVehs();
		resetSignalPlan();
	}
	public double getPassSpd() {
		return this.NODE_PASS_SPD;
	}
	private boolean intersectionPass(double currentTime, long fLinkID, long tLinkID) {
		return type(Constants.NODE_TYPE_SIGNALIZED_INTERSECTION)==0 ||
				check(currentTime, fLinkID, tLinkID);
	}
	public boolean check(double currentTime, long fLinkID, long tLinkID){
		SignalPlan thePlan = findPlan(currentTime);
		if (thePlan!=null)
			return thePlan.check(currentTime, fLinkID, tLinkID);
		List<double[]> table =  signalTable.get(fLinkID+"_"+tLinkID);
		if (table!=null)
			return table.stream().anyMatch(d->currentTime>=d[0]&&currentTime<=d[1]);
		return false;
	}
	public float[] getColor(double currentTime, String ftLinkID){
		List<double[]> table =  signalTable.get(ftLinkID);
		if (table==null)
			return Constants.COLOR_RED;
		else {
			double[] t = table.stream().filter(d->currentTime>=d[0]&&currentTime<=d[1]).findFirst().orElse(null);
			if (t==null)
				return Constants.COLOR_RED;
			else if (t[1]-currentTime>3.0)
				return Constants.COLOR_GREEN;
			else
				return Constants.COLOR_AMBER;
		}
	}
	protected MLPNode update() {
		if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {
			if (this.upLinks.size()<=0)
				return this;
			RoadNetwork rn = upLinks.get(0).getNetwork();
			SimulationClock clock = rn.getSimClock();
			double stepSize = clock.getStepSize();
			double currentTime = clock.getCurrentTime();
			HashMap<String, Double> vMap = calChannelSpds();
			turningMap.forEach((dir,conns)->{
				double v = vMap.get(dir);
				conns.forEach(c->{
					for (int i = 0; i < c.vehsOnConn.size(); i++) {
						MLPVehicle veh = c.vehsOnConn.get(i);
						if (i==0)
							veh.newSpeed = getPassSpd() * VehicleType.getPowerRate(veh.getType());
						else {
							MLPVehicle leading = c.vehsOnConn.get(c.vehsOnConn.indexOf(veh)-1);
							double gap = veh.getDistance() - leading.getLength() - leading.getDistance();
							double maxSpd = ((MLPParameter) rn.getSimParameter()).maxSpeed(gap);
							veh.newSpeed = Math.min(v,maxSpd) * VehicleType.getPowerRate(veh.getType());
						}
						veh.newDis -= veh.newSpeed*stepSize;
					}
				});
			});
			/*for (MLPVehicle veh : statedVehs) {
				if (veh.conn!=null) {
					if (veh.conn.vehsOnConn.indexOf(veh)==0){
						//head use node speed
						veh.newSpeed = getPassSpd() * VehicleType.getPowerRate(veh.getType());
					}
					else {
						double passSpd = veh.conn.calSpd();
						LinkedList<MLPVehicle> connVehs = veh.conn.vehsOnConn;
						if (connVehs.getFirst().getId()!=veh.getId()) {
							MLPVehicle leading = connVehs.get(connVehs.indexOf(veh)-1);
							double gap = veh.getDistance() - leading.getLength() - leading.getDistance();
							double maxSpd = ((MLPParameter) rn.getSimParameter()).maxSpeed(gap);
							passSpd = Math.min(passSpd,maxSpd);
						}
						veh.newSpeed = passSpd * VehicleType.getPowerRate(veh.getType());
					}
					veh.newDis -= veh.newSpeed*stepSize;
				}
			}*/
		}
		return this;
	}
	protected MLPNode updateStatedVehs() {
		if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {
			statedVehs.forEach(veh -> {

			});
		}
		return this;
	}

	public void addLC(MLPConnector lc) {
		List<MLPConnector> conns = findTurningConn(lc.upLinkID(),lc.dnLinkID());
		if (conns==null){
			conns = new ArrayList<>();
			turningMap.put(lc.upLinkID()+"_"+lc.dnLinkID(),conns);
		}
		conns.add(lc);
	}

	public List<MLPConnector> findTurningConn(Long fLinkID, Long tLinkID){
		return turningMap.get(fLinkID+"_"+tLinkID);
	}

	public String findTurningString(Long fLinkID, Long tLinkID){
		List<MLPConnector> conns = findTurningConn(fLinkID,tLinkID);
		if (conns==null)
			return "?";
		else return conns.get(0).getTurningDir();
	}

	public boolean dnLinkExist(MLPLink dnLink){
		return dnLinks.contains(dnLink);
	}

	public void updateConflictDirs(){
		List<String> turningDirs = new ArrayList<>(turningMap.keySet());
		if (turningDirs.size()<=1)
			return;
		for (int i = 0; i < turningDirs.size()-1; i++) {
			String td1 = turningDirs.get(i);
			String[] td1_ = td1.split("_");
			List<GeoPoint> td1_list = getMidVec(td1);
			for (int j = i+1; j < turningDirs.size(); j++) {
				String td2 = turningDirs.get(j);
				String[] td2_ = td2.split("_");
				if (!td1_[0].equals(td2_[0])&&!td1_[1].equals(td2_[1])){
					List<GeoPoint> td2_list = getMidVec(td2);
					if (GeoUtil.isCross(td1_list,td2_list))
						addConflictDir(td1,td2);
				}
			}
		}
	}

	private void addConflictDir(String ftLinkID_1, String ftLinkID_2){
		addOneSideConflictDir(ftLinkID_1,ftLinkID_2);
		addOneSideConflictDir(ftLinkID_2,ftLinkID_1);
	}

	private void addOneSideConflictDir(String ftLinkID_1, String ftLinkID_2){
		List<String> conflictedList1 = confilctDirs.get(ftLinkID_1);
		if (conflictedList1==null){
			conflictedList1 = new ArrayList<>();
			confilctDirs.put(ftLinkID_1,conflictedList1);
		}
		conflictedList1.add(ftLinkID_2);
	}

	public List<GeoPoint> getMidVec(String ftLinkID){
		List<MLPConnector> conns = turningMap.get(ftLinkID);
		if (conns==null)
			return null;
		GeoPoint fMid = GeoPoint.center(conns.stream().map(c->c.upLane.getLastCtlPoint()).collect(Collectors.toList()));
		GeoPoint tMid = GeoPoint.center(conns.stream().map(c->c.dnLane.getFirstCtlPoint()).collect(Collectors.toList()));
		List<GeoPoint> ans = new ArrayList<>();
		ans.add(fMid);
		ans.add(tMid);
		return ans;
	}

	public double getChannelVehs(String ftLinkID){
		List<MLPConnector> conns = turningMap.get(ftLinkID);
		return conns==null ?
				0 :
				conns.stream().mapToDouble(c->c.vehsOnConn.size()).sum();
	}

	public MLPLink getUpLink(String ftLinkId){
		return (MLPLink) turningMap.get(ftLinkId).get(0).upLane.getLink();
	}

	public MLPLink getDnLink(String ftLinkId){
		return (MLPLink) turningMap.get(ftLinkId).get(0).dnLane.getLink();
	}

	public double getEffectiveNLanes(MLPConnector connector){
		double n1 = connector.getUpSeg().nLanes();
		double n2 = connector.getDnSeg().nLanes();
		return 0.5 * (n1+n2);
	}

	public HashMap<String, Double> calChannelSpds(){
		HashMap<String, Double> kMap = new HashMap<>();
		HashMap<String, Double> vMap = new HashMap<>();
		double kj = ((MLPLink)getUpLink(0)).dynaFun.linkCharacteristics[2];
		turningMap.forEach((dir,conns)->{
			double effectiveLaneCount = getEffectiveNLanes(conns.get(0));
			double meanLaneLength = conns.stream().mapToDouble(c->c.getLength()).average().getAsDouble();
			double nVehs = conns.stream().mapToDouble(c->MLPVehicles.calPCU(c.vehsOnConn)).sum();
			double k = nVehs / meanLaneLength / effectiveLaneCount;
			kMap.put(dir,k);
			vMap.put(dir,getPassSpd()*kTerm(k,kj));//getPassSpd()*kTerm(k,kj)
		});
		confilctDirs.forEach((dir,conflictDirs)->{
			for (int i = 0; i < conflictDirs.size(); i++) {
				double k = kMap.get(conflictDirs.get(i));
				vMap.put(dir,vMap.get(dir)*kTerm(k,kj));
			}
		});
		return vMap;
	}

	private double kTerm(double k, double kj){
		if (k>=kj)
			return 0;
		return Math.pow(1.0-Math.pow(k/kj,MLPNode.NODE_ALPHA),MLPNode.NODE_BETA);
	}

	public boolean vehArrival(String ftLinkId){
		List<MLPConnector> conns = turningMap.get(ftLinkId);
		for (int i = 0; i < conns.size(); i++) {
			MLPVehicle veh = conns.get(i).upLane.getHead();
			if (veh!=null)//&& veh.getDistance()<10
				return true;
		}
		return false;
	}
}
